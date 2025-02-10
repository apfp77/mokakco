package com.mokakco.platform.listener;

import com.mokakco.platform.attendance.AttendanceService;
import com.mokakco.platform.attendance.AttendanceSessions;
import com.mokakco.platform.attendance.AttendanceSessionsService;
import com.mokakco.platform.attendance.TimeSession;
import com.mokakco.platform.member.UserService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
public class CommandListener extends ListenerAdapter {

    @Value("${ATTENDANCE_SEARCH_CHANNEL_ID}")
    private Long attendanceChannelId;

    private final AttendanceService attendanceService;
    private final AttendanceSessionsService attendanceSessionsService;
    private final UserService userService;
    private final Clock clock;

    public CommandListener(AttendanceService attendanceService, AttendanceSessionsService attendanceSessionsService, UserService userService, Clock clock) {
        this.attendanceService = attendanceService;
        this.attendanceSessionsService = attendanceSessionsService;
        this.userService = userService;
        this.clock = clock;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        // Enum에서 해당 명령어 찾기
        CommandEnum commandEnum = CommandEnum.fromCommand(commandName);
        if (commandEnum == null) {
            event.reply("❌ 알 수 없는 명령어입니다.").queue();
            return;
        }

        // 명령어별로 동작 처리
        switch (commandEnum) {
            case ATTENDANCE_TIME:
                this.AttendanceTime(event);
                break;
            case ATTENDANCE_SESSION_TIME:
                this.AttendanceSessionTime(event);
                break;
        }
    }

    private void AttendanceTime(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        Long userId = userService.findUserIdByDiscordId(Objects.requireNonNull(member).getIdLong());
        Integer attendanceMinute = attendanceService.findTodayAttendanceTime(userId);
        String message = "금일 출석 시간: " + convertMinutesToHoursAndMinutes(attendanceMinute);

        if (event.getChannel().getId().equals(attendanceChannelId.toString())) {
            event.reply(message).queue();
            return;
        }
         // 출석 채널이 아닌 경우 본인만 확인 가능하도록 설정
        event.reply(message).setEphemeral(true).queue();
    }

    public String[][] convertAttendanceToMatrix(Map<LocalDate, Map<String, AttendanceSessions>> sessionMap) {
        List<String> timeSessionValues = TimeSession.getAllValues();

        // 날짜를 정렬 (최신 날짜부터)
        List<LocalDate> sortedDates = sessionMap.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .toList();

        // 최종 2D 배열 생성
        String[][] attendanceMatrix = new String[sortedDates.size()][timeSessionValues.size() + 1];

        int rowIndex = 0;
        for (LocalDate date : sortedDates) {
            attendanceMatrix[rowIndex][0] = date.format(DateTimeFormatter.ofPattern("MM-dd")); // 날짜 포맷

            Map<String, AttendanceSessions> sessionStatusMap = sessionMap.get(date);

            for (int colIndex = 0; colIndex < timeSessionValues.size(); colIndex++) {
                AttendanceSessions session = sessionStatusMap.get(timeSessionValues.get(colIndex));
                attendanceMatrix[rowIndex][colIndex + 1] = (session.getStayDurationMinutes() >= 60) ? "✅" : "❌";
            }
            rowIndex++;
        }

        return attendanceMatrix;
    }


    private void AttendanceSessionTime(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        CompletableFuture.runAsync(() -> {
            try {
                Member member = event.getMember();
                Long userId = userService.findUserIdByDiscordId(Objects.requireNonNull(member).getIdLong());
                LocalDate today = LocalDate.now(clock);

                Map<LocalDate, Map<String, AttendanceSessions>> sessionMap =
                        attendanceSessionsService.findSessionsByUserIdAndDateBetween(userId, today);

                String[][] attendanceMatrix = convertAttendanceToMatrix(sessionMap);

                // 📅 표 형식 (디스코드 코드 블록)
                StringBuilder table = new StringBuilder("```\n📅 출근 기록 (최근 1주일)\n");
                table.append("날짜   | 아침 | 점심 | 저녁 | 심야\n");
                table.append("------|-----|-----|-----|-----\n");
                for (String[] row : attendanceMatrix) {
                    table.append(String.format("%s | %s | %s | %s | %s\n", row[0], row[1], row[2], row[3], row[4]));
                }
                table.append("```");

                // ✅ 출석 채널 여부에 따라 응답 방식 변경
                boolean isAttendanceChannel = event.getChannel().getId().equals(attendanceChannelId.toString());
                event.getHook().sendMessage(table.toString()).setEphemeral(!isAttendanceChannel).queue();

            } catch (Exception e) {
                event.getHook().sendMessage("⚠️ 출석 기록을 불러오는 중 오류가 발생했습니다.").setEphemeral(true).queue();
            }
        });
    }

    // @Override
    // public void onMessageReceived(@NotNull MessageReceivedEvent event) {
    //     // 메시지를 보낸 사람이 봇인지 확인 (봇 메시지는 무시)
    //     if (event.getAuthor().isBot()) return;
    //
    //     MessageChannel isMessageChannel = event.getChannel();
    //     if (!isMessageChannel.getType().equals(ChannelType.TEXT)) {
    //         return; // 텍스트 채널이 아닌 경우 무시
    //     }
    //
    //     // 메시지와 채널 가져오기
    //     Message message = event.getMessage();
    //     String content = message.getContentRaw(); // 메시지 내용 (포맷팅 없는 텍스트)
    //     TextChannel channel = event.getChannel().asTextChannel();
    //
    //     // 특정 채널에서만 처리
    //     if (!channel.getId().equals(attendanceChannelId.toString())) {
    //         return; // 대상 채널이 아니라면 무시
    //     }
    //
    //     Member member = event.getMember();
    //     // 명령어 처리
    //     if (content.equalsIgnoreCase("/출석시간")) {
    //         Long userId = userService.findUserIdByDiscordId(Objects.requireNonNull(member).getIdLong());
    //         Integer attendanceMinute = attendanceService.findTodayAttendanceTime(userId);
    //         channel.sendMessage("오늘 출석 시간: " + convertMinutesToHoursAndMinutes(attendanceMinute)).queue();
    //     } else if (content.equalsIgnoreCase("/hello")) {
    //         channel.sendMessage("Hello there! " + member.getEffectiveName() + " 👋" ).queue(); // "!hello" 입력 시 인사 응답
    //     }
    // }

    private String convertMinutesToHoursAndMinutes(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("Minutes cannot be negative.");
        }

        int hours = minutes / 60; // 시 계산
        int remainingMinutes = minutes % 60; // 나머지 분 계산

        return String.format("%d시간 %d분 입니다.", hours, remainingMinutes);
    }
}
