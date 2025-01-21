package com.mokakco.platform.attendance;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;


    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    /**
     * 출근 시간 기록
     * 사용자의 식별값을 받아서 출근 시간을 기록한다.
     * @param userId 사용자 식별값
     */
    public void entry(Long userId){
        Attendance attendance = new Attendance(userId);
        attendance.entry();
        attendanceRepository.save(attendance);
    }

    /**
     * 퇴근 시간 기록
     * 사용자의 식별값을 받아서 퇴근 시간을 기록한다.
     * @param userId 사용자 식별값
     */
    public void exit(Long userId) {
        List<Attendance> attendance = attendanceRepository.findByUserIdAndExitTimeIsNull(userId);
        validatePendingAttendance(attendance, userId);

        Attendance currentAttendance = attendance.getFirst();
        LocalDateTime exitTime = LocalDateTime.now();
        LocalDateTime exitEndTime = getExitEndTime(currentAttendance.getEntryTime());

        if (exitTime.isAfter(exitEndTime)){
            handleCrossDayExit(userId, currentAttendance, exitTime, exitEndTime);
        } else {
            recordExitTime(currentAttendance);
        }
    }

    /**
     * 사용자의 출퇴근 기록을 검증
     * 하나의 출근 기록만 존재해야 한다.
     */
    private void validatePendingAttendance(List<Attendance> attendance, Long userId) {
        if (attendance.isEmpty()) {
            throw new AttendanceException(AttendanceException.Reason.NO_PENDING_EXIT, userId.toString());
        }
        if (attendance.size() > 1) {
            throw new AttendanceException(AttendanceException.Reason.MULTIPLE_PENDING_EXIT, userId.toString());
        }
    }

    private LocalDateTime getExitEndTime(LocalDateTime entryTime) {
        return entryTime.withHour(23).withMinute(59).withSecond(59);
    }


    private void handleCrossDayExit(Long userId, Attendance currentAttendance, LocalDateTime exitTime, LocalDateTime exitEndTime) {
        List<Attendance> newAttendances = new ArrayList<>();

        recordFinalExitTime(currentAttendance);
        exitEndTime = exitEndTime.plusDays(1);

        while (exitTime.isAfter(exitEndTime)) {
            Attendance nextDayAttendance = createNextDayAttendance(userId, exitEndTime);
            newAttendances.add(nextDayAttendance);
            exitEndTime = exitEndTime.plusDays(1);
        }

        Attendance finalAttendance = createNextDayAttendance(userId, exitEndTime);
        finalAttendance.exit();
        newAttendances.add(finalAttendance);

        attendanceRepository.saveAll(newAttendances);
    }


    private void recordExitTime(Attendance attendance) {
        attendance.exit();
        attendanceRepository.save(attendance);
    }

    private void recordFinalExitTime(Attendance attendance) {
        attendance.lastRecordedExit();
        attendanceRepository.save(attendance);
    }


    private Attendance createNextDayAttendance(Long userId, LocalDateTime exitEndTime) {
        Attendance nextDayAttendance = new Attendance(userId, exitEndTime.withHour(0).withMinute(0).withSecond(0));
        nextDayAttendance.lastRecordedExit();
        nextDayAttendance.calculateDuration();
        return nextDayAttendance;
    }

    /**
     * 당일의 출근 기록 조회
     * Attendance 테이블에서 특정 시간대에 데이터가 있는지 확인한다.
     * @param userId 사용자 식별값
     * @param entryTime 출근 시간
     * @param exitTime 퇴근 시간
     */
    public boolean findTodayAttendance(Long userId, LocalDateTime entryTime, LocalDateTime exitTime) {
        return attendanceRepository
                .findByUserIdAndEntryTimeBetween(userId, entryTime, exitTime).size() == 1;
    }

    /**
     * 당일 출근시간 조회
     */
    public Integer findTodayAttendanceTime(Long userId) {
        List<Attendance> attendance = attendanceRepository.findByUserIdAndEntryTimeAfter(userId, LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));

        Integer sum = 0;
        for (Attendance a : attendance) {
            if (a.getDurationMinutes() == null){
                sum += (int) Duration.between(a.getEntryTime(), LocalDateTime.now()).toMinutes();
                continue;
            }
            sum += a.getDurationMinutes();
        }

        return sum;
    }
}
