package com.mokako.platform;

import com.mokako.platform.member.UserService;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateActivitiesEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.EnumSet;
import java.util.List;

@SpringBootApplication
public class DiscordBot extends ListenerAdapter {

    @Value("${DISCORD_BOT_TOKEN}")
    private String token;

    private JDA jda;

    private final UserService userService;

    private static final Logger botLogger = LoggerFactory.getLogger("discordBotLogger");


    public DiscordBot(UserService userService) {
        this.userService = userService;
    }

    public static void main(String[] args) {
        SpringApplication.run(DiscordBot.class, args);
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
                .build();

    }

    // 서버 종료 시 discord 봇 해제
    @PreDestroy
    public void stopBot() {
        if (jda != null) {
            jda.shutdown();
        }
    }

    @Override
    public void onUserActivityStart(@Nonnull UserActivityStartEvent event){
        System.out.println("onUserActivityStart triggered!");
        System.out.println("Activity start event received for: ");
    }

    @Override
    public void onUserUpdateActivities(@NotNull UserUpdateActivitiesEvent event) {
        System.out.println("onUserUpdateActivities triggered!");
        User user = event.getUser();
        System.out.println(event.getNewValue());
        System.out.println("Activity update event received for: " + user.getAsTag());

        System.out.println("Activity update event received for: " + user.getAsTag());
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        Member member = event.getMember();
        AudioChannel joinedChannel = event.getChannelJoined();
        AudioChannel leftChannel = event.getChannelLeft();

        if (joinedChannel != null) {
            botLogger.info("{} + '님이' + {} + '채널에 참여했습니다.'", member.getEffectiveName(), joinedChannel.getName());
        } else if (leftChannel != null) {
            botLogger.info("{} + '님이' + {} + '채널에 나갔습니다.'", member.getEffectiveName(), leftChannel.getName());
        }
        // GuildVoiceState voiceState = member.getVoiceState();
        //
        // System.out.println("voiceState: " + voiceState);
        //
        // if (voiceState != null) {
        //     // 화면 공유 여부 확인
        //
        //     boolean isStreaming = voiceState.isStream();
        //     System.out.println(member.getEffectiveName() + " is streaming: " + isStreaming);
        //
        //     // 비디오 활성화 여부 확인
        //     boolean isVideoing = voiceState.isSendingVideo();
        //     System.out.println(member.getEffectiveName() + " is videoing: " + isVideoing);
        // }
    }

    @Override
    public void onReady(ReadyEvent event) {
        for (Guild guild : event.getJDA().getGuilds()) {
            System.out.println("서버 이름: " + guild.getName());
            guild.loadMembers().onSuccess(members -> {
                // 사용자 동기화
                userService.registerNewMembers(members);
                for (Member member : members) {
                    long test = member.getIdLong();
                    System.out.println("사용자 이름: " + member.getUser().getName() + ", ID: " + member.getId());
                }
            }).onError(Throwable::printStackTrace);
        }
    }
}