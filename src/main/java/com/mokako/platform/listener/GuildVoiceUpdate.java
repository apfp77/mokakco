package com.mokako.platform.listener;

import com.mokako.platform.attendance.AttendanceService;
import com.mokako.platform.configure.DiscordConfigure;
import com.mokako.platform.member.UserService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

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
                channel.sendMessage(member.getEffectiveName() + " 님이 출석하였습니다.").queue();
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
