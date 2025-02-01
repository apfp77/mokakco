package com.mokakco.platform;

import com.mokakco.platform.attendance.AttendanceSchedule;
import com.mokakco.platform.attendance.AttendanceService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
public class AttendanceScheduleTest {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private AttendanceSchedule attendanceSchedule;

    @MockitoSpyBean
    private Clock clock;

    /**
     * 과거의 입실 처리 및 현재 시간으로의 입실 및 퇴실 처리 테스트
     * 정확한 테스트를 위해 테스트 채널에 접속해있어야함
     */
    @Test
    void testPastEntryAndSessionTransition() throws InterruptedException {
        Long userId = 1L;

        // 1. 과거의 특정 시간(현재사간 - 1일)으로 설정하여 입실 처리
        Instant pastInstant = LocalDateTime.now().minusDays(1)
                .atZone(ZoneId.of("Asia/Seoul")).toInstant();

        doReturn(ZoneId.of("Asia/Seoul")).when(clock).getZone();
        doReturn(pastInstant).when(clock).instant();

        attendanceService.entry(userId);

        // 2. 현재 시간으로 변경 후 퇴실 및 입실 처리 테스트
        Instant nowInstant = Instant.now();
        when(clock.instant()).thenReturn(nowInstant);

        attendanceSchedule.autoSessionTransition();
        Thread.sleep(5000); // 메시지가 올 때까지 5초 대기

    }
}
