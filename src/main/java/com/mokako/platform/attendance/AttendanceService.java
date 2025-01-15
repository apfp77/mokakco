package com.mokako.platform.attendance;

import org.springframework.stereotype.Service;

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
        if (attendance.isEmpty()){
            throw new AttendanceException(AttendanceException.Reason.NO_PENDING_EXIT, userId.toString());
        }
        int singlePendingExitNode = 1;
        if (attendance.size() > singlePendingExitNode){
            throw new AttendanceException(AttendanceException.Reason.MULTIPLE_PENDING_EXIT, userId.toString());
        }

        Attendance currentAttendance = attendance.getFirst();
        LocalDateTime exitTime = LocalDateTime.now();
        System.out.println("currentAttendance.getEntryTime() : " + currentAttendance.getEntryTime());
        LocalDateTime exitEndTime = currentAttendance.getEntryTime()
                                                                    .withHour(23)
                                                                    .withMinute(59)
                                                                    .withSecond(59);
        if (exitTime.isAfter(exitEndTime)){
            currentAttendance.lastRecordedExit();
            attendanceRepository.save(currentAttendance);
            exitEndTime = exitEndTime.plusDays(1);
            List<Attendance> AttendanceFactory = new ArrayList<>();
            while(exitTime.isAfter(exitEndTime)){
                Attendance nextDayAttendance = new Attendance(userId, exitEndTime.withHour(0).withMinute(0).withSecond(0));
                nextDayAttendance.lastRecordedExit();
                nextDayAttendance.calculateDuration();
                AttendanceFactory.add(nextDayAttendance);
                // attendanceRepository.save(currentAttendance);
                exitEndTime = exitEndTime.plusDays(1);
            }
            Attendance nextDayAttendance = new Attendance(userId, exitEndTime.withHour(0).withMinute(0).withSecond(0));
            nextDayAttendance.exit();
            nextDayAttendance.calculateDuration();
            AttendanceFactory.add(nextDayAttendance);
            // attendanceRepository.save(nextDayAttendance);
            attendanceRepository.saveAll(AttendanceFactory);
        } else {
            currentAttendance.exit();
            attendanceRepository.save(currentAttendance);
        }
    }

    /**
     * 당일의 출근 기록 조회
     */
    public boolean findTodayAttendance(Long userId) {
        return attendanceRepository.findByUserIdAndEntryTimeAfter(userId, LocalDateTime.now().withHour(0).withMinute(0).withSecond(0)).size() == 1;
    }
}
