package com.mokakco.platform.configure;

import com.mokakco.platform.listener.CommandListener;
import com.mokakco.platform.listener.GuildVoiceUpdate;
import com.mokakco.platform.listener.OnReady;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.EnumSet;

@Configuration
public class DiscordConfigure extends ListenerAdapter {

    @Value("${DISCORD_BOT_TOKEN}")
    private String token;

    @Value("${ATTENDANCE_CHANNEL_ID}")
    private Long attendanceChannelId;

    private JDA jda;

    private final GuildVoiceUpdate guildVoiceUpdate;
    private final OnReady onReady;
    private final CommandListener commandListener;

    public DiscordConfigure(GuildVoiceUpdate guildVoiceUpdate, OnReady onReady, CommandListener commandListener) {
        this.guildVoiceUpdate = guildVoiceUpdate;
        this.onReady = onReady;
        this.commandListener = commandListener;
    }


    // 서버 실행 시 discord 봇 연결
    @PostConstruct
    public void startBot() {
        jda = JDABuilder.createDefault(token)
                .enableIntents(EnumSet.allOf(GatewayIntent.class))
                .enableCache(EnumSet.allOf(CacheFlag.class))
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setAutoReconnect(true)
                // .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES)
                // .setMemberCachePolicy(MemberCachePolicy.ALL) // 모든 멤버 캐시
                // .enableCache(CacheFlag.ACTIVITY)
                .addEventListeners(this)
                .addEventListeners(guildVoiceUpdate)
                .addEventListeners(onReady)
                .addEventListeners(commandListener)
                .build();
    }

    public TextChannel getAttendanceChannel() {
        return jda.getTextChannelById(attendanceChannelId);
    }

    // 서버 종료 시 discord 봇 해제
    @PreDestroy
    public void stopBot() {
        if (jda != null) {
            jda.shutdown();
        }
    }
}
