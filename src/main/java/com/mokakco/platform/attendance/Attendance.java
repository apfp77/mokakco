package com.mokakco.platform.attendance;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime entryTime;

    @Column
    private LocalDateTime exitTime;

    @Column
    private Integer durationMinutes; // 출입 시간과 퇴실 시간 차이 (분 단위)

    protected Attendance(Long userId) {
        this.userId = userId;
    }

    protected Attendance(Long userId, LocalDateTime entryTime){
        this.userId = userId;
        this.entryTime = entryTime;
    }

    // entryTime이 작성될때 시간차이 계산
    @PreUpdate
    public void calculateDuration() {
        if (entryTime != null && exitTime != null) {
            this.durationMinutes = (int) Duration.between(entryTime, exitTime).toMinutes();
        }
    }

    protected void entry(){
        this.entryTime = LocalDateTime.now();
    }

    protected void exit(){
        this.exitTime = LocalDateTime.now();
    }

    // 하루를 넘어가는 경우 당일 퇴근시간을 23시 59분 59초로 설정
    protected void lastRecordedExit(){
        this.exitTime = this.entryTime
                .withHour(23)
                .withMinute(59)
                .withSecond(59);
    }
}
