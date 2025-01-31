package com.mokakco.platform.listener;

import com.mokakco.platform.attendance.AttendanceNotification;
import com.mokakco.platform.attendance.AttendanceService;
import com.mokakco.platform.attendance.TimeSession;
import com.mokakco.platform.member.UserService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class GuildVoiceUpdate extends ListenerAdapter {

    private static final Logger botLogger = LoggerFactory.getLogger("discordBotLogger");

    @Value("${ATTENDANCE_NOTIFICATION_CHANNEL_ID}")
    private Long attendanceNotificationChannelId;

    @Value("${ATTENDANCE_ENTRY_EXIT_CHANNEL_ID}")
    private Long attendanceEntryExitChannelId;

    private final UserService userService;
    private final AttendanceService attendanceService;
    private final AttendanceNotification notifyChannel;
    private final Clock clock;

    public GuildVoiceUpdate(UserService userService, AttendanceService attendanceService, AttendanceNotification notifyChannel, Clock clock) {
        this.userService = userService;
        this.attendanceService = attendanceService;
        this.notifyChannel = notifyChannel;
        this.clock = clock;
    }


    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        Member member = event.getMember();
        AudioChannel joinedChannel = event.getChannelJoined();
        AudioChannel leftChannel = event.getChannelLeft();

        // 채널 이동
        if (joinedChannel == null && leftChannel == null) {
            return;
        }
        if (joinedChannel != null) {
        // 채널 참여
            if (Objects.requireNonNull(event.getChannelJoined()).getIdLong() != attendanceEntryExitChannelId) {
                return;
            }
            Long userId = userService.findUserIdByDiscordId(member.getIdLong());
            attendanceService.entry(userId);
            
            notifyChannel
                    .sendNotification(
                            member.getIdLong(),
                            attendanceNotificationChannelId,
                            String.format("%s님이 %s에 출석하였습니다.",
                                    member.getEffectiveName(), TimeSession.findSessionByTime(LocalDateTime.now(clock)))
                    );
            botLogger.info("{} + '님이' + {} + '채널에 참여했습니다.'", member.getEffectiveName(), joinedChannel.getName());
        } else {
        // 채널 나감
            Long userId = userService.findUserIdByDiscordId(member.getIdLong());
            attendanceService.exit(userId);
            botLogger.info("{} + '님이' + {} + '채널에 나갔습니다.'", member.getEffectiveName(), leftChannel.getName());
        }
    }
}
