package com.mokakco.platform;

import com.mokakco.platform.attendance.AttendanceSchedule;
import com.mokakco.platform.attendance.AttendanceService;
import com.mokakco.platform.configure.DiscordConfigure;
import jakarta.transaction.Transactional;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
public class AttendanceScheduleTest {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private AttendanceSchedule attendanceSchedule;

    @Value("${ATTENDANCE_ENTRY_EXIT_CHANNEL_ID}")
    private Long attendanceEntryExitChannelId;

    @Value("${ATTENDANCE_NOTIFICATION_CHANNEL_ID}")
    private Long attendanceNotificationChannelId;

    @MockitoSpyBean
    private Clock clock;

    @MockitoSpyBean
    private DiscordConfigure discordConfigure;

    @Test
    void testPastEntryAndSessionTransition() {
        Long userId = 1L;

        // 1. 과거의 특정 시간(2024-01-01 08:00:00)으로 설정하여 입실 처리
        Instant pastInstant = LocalDateTime.of(2024, 1, 1, 8, 0)
                .atZone(ZoneId.of("Asia/Seoul")).toInstant();

        doReturn(ZoneId.of("Asia/Seoul")).when(clock).getZone();
        doReturn(pastInstant).when(clock).instant();

        VoiceChannel voiceChannel = mock(VoiceChannel.class);
        when(discordConfigure.getVoiceChannel(attendanceEntryExitChannelId)).thenReturn(voiceChannel);
        List<Member> members = List.of(mock(Member.class));
        // Member 하나를 추가
        Member member = mock(Member.class);
        members.add(member);
        when(voiceChannel.getMembers()).thenReturn(members);



        attendanceService.entry(userId);

        // 2. 현재 시간으로 변경 후 퇴실 및 입실 처리 테스트
        Instant nowInstant = Instant.now();
        when(clock.instant()).thenReturn(nowInstant);

        attendanceSchedule.autoSessionTransition();

    }
}
