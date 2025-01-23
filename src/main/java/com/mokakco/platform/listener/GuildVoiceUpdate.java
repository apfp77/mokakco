package com.mokakco.platform.listener;

import com.mokakco.platform.attendance.AttendanceService;
import com.mokakco.platform.configure.DiscordConfigure;
import com.mokakco.platform.member.UserService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

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
    private final DiscordConfigure discordConfigure;

    public GuildVoiceUpdate(UserService userService, AttendanceService attendanceService, @Lazy DiscordConfigure discordConfigure) {
        this.userService = userService;
        this.attendanceService = attendanceService;
        this.discordConfigure = discordConfigure;
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
            TextChannel channel = discordConfigure.getChannel(attendanceNotificationChannelId);
                /**
                 * 오전(08시 ~ 12시)반, 오후(1시 ~ 6시)반, 저녁(8시 ~ 12시)반
                 * 각 시간때마다 한 시간 이상이면 출석을 완료했다는 메시지를 보냄
                 * 00시 ~ 08시 사이에는 알람을 보내지 않는다
                 */
                // 오전반
                if (LocalDateTime.now().getHour() >= 8 && LocalDateTime.now().getHour() < 12) {
                    if (attendanceService.findTodayAttendance(userId,
                            LocalDateTime.now().withHour(7)
                                    .withMinute(59)
                                    .withSecond(59), LocalDateTime.now().withHour(11)
                                    .withMinute(59)
                                    .withSecond(59))) {
                        channel.sendMessage(String.format("%s 님이 오전반에 출석하였습니다", member.getEffectiveName())).queue();
                    }
                }
                // 오후반
                if (LocalDateTime.now().getHour() >= 13 && LocalDateTime.now().getHour() < 18) {
                    if (attendanceService.findTodayAttendance(userId,
                            LocalDateTime.now().withHour(12)
                                    .withMinute(59)
                                    .withSecond(59), LocalDateTime.now().withHour(17)
                                    .withMinute(59)
                                    .withSecond(59))) {
                        channel.sendMessage(String.format("%s 님이 오후반에 출석하였습니다", member.getEffectiveName())).queue();
                    }
                }
                // 저녁반
                if (LocalDateTime.now().getHour() >= 20) {
                    if (attendanceService.findTodayAttendance(userId,
                            LocalDateTime.now().withHour(19)
                                    .withMinute(59)
                                    .withSecond(59), LocalDateTime.now().withHour(23)
                                    .withMinute(59)
                                    .withSecond(59))) {
                        channel.sendMessage(String.format("%s 님이 저녁반에 출석하였습니다", member.getEffectiveName())).queue();
                    }
                }
            botLogger.info("{} + '님이' + {} + '채널에 참여했습니다.'", member.getEffectiveName(), joinedChannel.getName());
        } else if (leftChannel != null) {
        // 채널 나감
            Long userId = userService.findUserIdByDiscordId(member.getIdLong());
            attendanceService.exit(userId);
            botLogger.info("{} + '님이' + {} + '채널에 나갔습니다.'", member.getEffectiveName(), leftChannel.getName());
        }
    }
}
