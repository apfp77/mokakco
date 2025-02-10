package com.mokakco.platform.attendance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceSessionsRepository extends JpaRepository<AttendanceSessions, Long> {
    List<AttendanceSessions> findByUserIdAndDateAndTimeSession(Long userId, LocalDate date, TimeSession timeSession);

    List<AttendanceSessions> findByUserIdAndDateBetweenOrderByDateDesc(Long userId, LocalDate dateAfter, LocalDate dateBefore);

}
