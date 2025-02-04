package com.mokakco.platform.attendance;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class AttendanceSessions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @Column(nullable = false)
    private TimeSession timeSession;

    @Column(nullable = false)
    private Long userId;

    @Column
    private LocalDate date;

    @Column
    private Integer stayDurationMinutes;

    protected AttendanceSessions() {}

    protected AttendanceSessions(Long userId, LocalDateTime entryTime, Integer stayDurationMinutes, LocalDate date) {
        this.userId = userId;
        this.timeSession = new TimeSession(entryTime);
        this.date = date;
        this.stayDurationMinutes = stayDurationMinutes;
    }

    protected void updateStayDurationMinutes(Integer stayDurationMinutes) {
        this.stayDurationMinutes = stayDurationMinutes;
    }
}
