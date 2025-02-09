package com.mokakco.platform.notification;

import com.mokakco.platform.configure.DiscordConfigure;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ErrorNotificationService {

    @Value("${ERROR_NOTIFICATION_CHANNEL_ID}")
    private Long errorNotificationChannelId;

    private TextChannel textChannel;

    private final DiscordConfigure discordConfigure;
    private final Logger logger = LoggerFactory.getLogger("errorLogger");

    public ErrorNotificationService(@Lazy DiscordConfigure discordConfigure) {
        this.discordConfigure = discordConfigure;
    }

    @PostConstruct
    private void init(){
        this.textChannel = discordConfigure.getChannel(errorNotificationChannelId);
    }

    public void sendErrorMessage(String message) {
        if (textChannel == null) {
            logger.error("에러 알림 채널을 찾을 수 없습니다.");
            return;
        }
        textChannel.sendMessage(message).queue();
    }
}