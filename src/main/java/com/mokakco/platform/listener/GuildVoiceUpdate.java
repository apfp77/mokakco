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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GuildVoiceUpdate extends ListenerAdapter {

    private static final Logger botLogger = LoggerFactory.getLogger("discordBotLogger");

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
            Long userId = userService.findUserIdByDiscordId(member.getIdLong());
            attendanceService.entry(userId);
            if (attendanceService.findTodayAttendance(userId)){
                TextChannel channel = discordConfigure.getAttendanceChannel();
                /**
                 * 오전(08시 ~ 12시)반, 오후(1시 ~ 6시)반, 저녁(8시 ~ 12시)반
                 * 각 시간때마다 한 시간 이상이면 출석을 완료했다는 메시지를 보냄
                 * 00시 ~ 08시 사이에는 알람을 보내지 않는다
                 */
                if (LocalDateTime.now().getHour() >= 8){
                    String timeState;
                    if (LocalDateTime.now().getHour() < 12){
                        timeState = "오전반";
                    } else if (LocalDateTime.now().getHour() < 18){
                        timeState = "오후반";
                    } else {
                        timeState = "저녁반";
                    }
                    channel.sendMessage(String.format("%s 님이 %s에 출석하였습니다", member.getEffectiveName(), timeState)).queue();
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
