package com.mokakco.platform.attendance;

import com.mokakco.platform.configure.DiscordConfigure;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AttendanceNotification {

    private LocalDateTime time;
    private final Map<String, Map<Long, Boolean>> notificationSent;
    private final DiscordConfigure discordConfigure;
    private final Clock clock;
    private final Logger logger = LoggerFactory.getLogger("errorLogger");

    public AttendanceNotification(@Lazy DiscordConfigure discordConfigure, Clock clock) {
        this.discordConfigure = discordConfigure;
        this.clock = clock;
        notificationSent = new HashMap<>();
        List<String> sessions = TimeSession.getAllValues();
        for (String session : sessions) {
            notificationSent.put(session, new HashMap<>());
        }
        time = LocalDateTime.now(clock);
    }

    private void initNotificationSent() {
        List<String> sessions = TimeSession.getAllValues();
        for (String session : sessions) {
            notificationSent.get(session).clear();
        }
        time = LocalDateTime.now(clock);
    }

    private void isPastMidnight() {
        if (!time.toLocalDate().equals(LocalDateTime.now(clock).toLocalDate())) {
            initNotificationSent();
        }
    }

    public void sendNotification(Long userId, Long channelId, String message) {
        TimeSession session = new TimeSession(LocalDateTime.now(clock));
        if (isNotificationSent(userId, session)) {
            return;
        }
        TextChannel channel = discordConfigure.getChannel(channelId);
        if (channel == null) {
            logger.error("채널을 찾을 수 없습니다.");
            return;
        }
        channel.sendMessage(message).queue();
        notificationSent.get(session.getSessionAsString()).put(userId, true);
    }

    private boolean isNotificationSent(Long userId, TimeSession session) {
        isPastMidnight();
        return notificationSent.get(session.getSessionAsString()).getOrDefault(userId, false);
    }
}
