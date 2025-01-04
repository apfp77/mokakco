import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class DiscordBot extends ListenerAdapter {

    public static void main(String[] args) throws Exception {
        Dotenv dotenv = Dotenv.load();

        String token = dotenv.get("DISCORD_BOT_TOKEN");

        JDABuilder.createDefault(token) // 봇 토큰 입력
                .enableIntents(GatewayIntent.GUILD_MEMBERS) // GUILD_MEMBERS 인텐트 활성화
                .addEventListeners(new DiscordBot())
                .build();
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        Member member = event.getMember();
        AudioChannel joinedChannel = event.getChannelJoined();
        AudioChannel leftChannel = event.getChannelLeft();

        System.out.println("member: " + member);
        if (joinedChannel != null) {
            System.out.println(member.getEffectiveName() + "님이 " + joinedChannel.getName() + " 채널에 참여했습니다.");
        } else if (leftChannel != null) {
            System.out.println(member.getEffectiveName() + "님이 " + leftChannel.getName() + " 채널에서 나갔습니다.");
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        for (Guild guild : event.getJDA().getGuilds()) {
            System.out.println("서버 이름: " + guild.getName());
            guild.loadMembers().onSuccess(members -> {
                for (Member member : members) {
                    System.out.println("사용자 이름: " + member.getUser().getName() + ", ID: " + member.getId());
                }
            }).onError(Throwable::printStackTrace);
        }
    }
}
