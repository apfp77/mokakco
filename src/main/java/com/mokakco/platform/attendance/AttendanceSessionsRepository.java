package com.mokakco.platform.attendance;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceSessionsRepository extends JpaRepository<AttendanceSessions, Long> {
}
