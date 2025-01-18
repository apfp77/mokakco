package com.mokakco.platform.listener;

import com.mokakco.platform.member.UserService;
import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateActivitiesEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class OnReady extends ListenerAdapter {

    private final UserService userService;

    public OnReady(UserService userService) {
        this.userService = userService;
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
