package com.mokakco.platform;

import com.mokakco.platform.configure.DiscordConfigure;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ConnectChannelsTest {
    @Value("${ATTENDANCE_NOTIFICATION_CHANNEL_ID}")
    private Long attendanceNotificationChannelId;

    @Value("${ATTENDANCE_ENTRY_EXIT_CHANNEL_ID}")
    private Long attendanceEntryExitChannelId;

    @Value("${ATTENDANCE_SEARCH_CHANNEL_ID}")
    private Long attendanceSearchChannelId;

    @Autowired
    private DiscordConfigure discordConfigure;

    @Test
    @DisplayName("알림 채널 연결 테스트")
    public void connectNotifyChannel() {
        TextChannel notifyChannel = discordConfigure.getChannel(attendanceNotificationChannelId);

        assertNotNull(notifyChannel);
    }

    @Test
    @DisplayName("입퇴실 채널 연결 테스트")
    public void connectEntryExitChannel() {
        VoiceChannel entryExitChannel = discordConfigure.getVoiceChannel(attendanceEntryExitChannelId);

        assertNotNull(entryExitChannel);
    }

    @Test
    @DisplayName("검색 채널 연결 테스트")
    public void connectSearchChannel() {
        TextChannel searchChannel = discordConfigure.getChannel(attendanceSearchChannelId);

        assertNotNull(searchChannel);
    }
}
