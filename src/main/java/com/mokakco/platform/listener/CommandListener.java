package com.mokakco.platform.listener;

import com.mokakco.platform.attendance.AttendanceService;
import com.mokakco.platform.member.UserService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CommandListener extends ListenerAdapter {

    @Value("${ATTENDANCE_CHANNEL_ID}")
    private Long attendanceChannelId;

    private final AttendanceService attendanceService;
    private final UserService userService;

    public CommandListener(AttendanceService attendanceService, UserService userService) {
        this.attendanceService = attendanceService;
        this.userService = userService;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // 메시지를 보낸 사람이 봇인지 확인 (봇 메시지는 무시)
        if (event.getAuthor().isBot()) return;

        MessageChannel isMessageChannel = event.getChannel();
        if (!isMessageChannel.getType().equals(ChannelType.TEXT)) {
            return; // 텍스트 채널이 아닌 경우 무시
        }

        // 메시지와 채널 가져오기
        Message message = event.getMessage();
        String content = message.getContentRaw(); // 메시지 내용 (포맷팅 없는 텍스트)
        TextChannel channel = event.getChannel().asTextChannel();

        // 특정 채널에서만 처리
        if (!channel.getId().equals(attendanceChannelId.toString())) {
            return; // 대상 채널이 아니라면 무시
        }

        Member member = event.getMember();
        // 명령어 처리
        if (content.equalsIgnoreCase("/출석시간")) {
            Long userId = userService.findUserIdByDiscordId(Objects.requireNonNull(member).getIdLong());
            Integer attendanceMinute = attendanceService.findTodayAttendanceTime(userId);
            channel.sendMessage("오늘 출석 시간: " + convertMinutesToHoursAndMinutes(attendanceMinute)).queue();
        } else if (content.equalsIgnoreCase("/hello")) {
            channel.sendMessage("Hello there! " + member.getEffectiveName() + " 👋" ).queue(); // "!hello" 입력 시 인사 응답
        }
    }

    private String convertMinutesToHoursAndMinutes(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("Minutes cannot be negative.");
        }

        int hours = minutes / 60; // 시 계산
        int remainingMinutes = minutes % 60; // 나머지 분 계산

        return String.format("%d시간 %d분 입니다.", hours, remainingMinutes);
    }
}
