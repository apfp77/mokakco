package com.mokakco.platform.attendance;

import com.mokakco.platform.configure.DiscordConfigure;
import com.mokakco.platform.member.UserService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceSchedule {

    private final DiscordConfigure discordConfigure;
    private final AttendanceNotification attendanceNotification;
    private final AttendanceService attendanceService;
    private final UserService userService;
    private final Clock clock;

    private static final Logger botLogger = LoggerFactory.getLogger("discordBotLogger");


    @Value("${ATTENDANCE_ENTRY_EXIT_CHANNEL_ID}")
    private Long attendanceEntryExitChannelId;

    @Value("${ATTENDANCE_NOTIFICATION_CHANNEL_ID}")
    private Long attendanceNotificationChannelId;

    public AttendanceSchedule(@Lazy DiscordConfigure discordConfigure, AttendanceNotification attendanceNotification, AttendanceService attendanceService, UserService userService, Clock clock) {
        this.discordConfigure = discordConfigure;
        this.attendanceNotification = attendanceNotification;
        this.attendanceService = attendanceService;
        this.userService = userService;
        this.clock = clock;
    }

    @Scheduled(cron = "0 2 6,12,18,0 * * *")
    public void autoSessionTransitionSchedule() {
        autoSessionTransition();
    }

    public void autoSessionTransition() {
        VoiceChannel channel = discordConfigure.getVoiceChannel(attendanceEntryExitChannelId);
        botLogger.info("스케줄링 시작: " + LocalDateTime.now(clock));

        if (channel == null) {
            throw new IllegalArgumentException("출석 채널을 찾을 수 없습니다.");
        }

        List<Member> members = channel.getMembers();

        if (!members.isEmpty()) {
            for (Member member : members) {
                Long userId = userService.findUserIdByDiscordId(member.getIdLong());
                attendanceService.exit(userId);
                attendanceService.entry(userId);
                attendanceNotification
                    .sendNotification(
                            member.getIdLong(),
                            attendanceNotificationChannelId,
                            String.format("%s님이 %s에 출석하였습니다.",
                                    member.getEffectiveName(), TimeSession.findSessionByTime(LocalDateTime.now(clock)))
                    );
            }
        }

        botLogger.info("스케줄링 종료: " + LocalDateTime.now(clock));
    }
}
