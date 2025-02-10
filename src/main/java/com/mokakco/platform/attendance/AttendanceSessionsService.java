package com.mokakco.platform.attendance;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceSessionsService {

    private final AttendanceSessionsRepository attendanceSessionsRepository;
    private final Logger logger = LoggerFactory.getLogger("errorLogger");

    /**
     * 사용자의 식별값과 입장 시간, 퇴장 시간을 받아서 입장 시간대에 따른 체류 시간을 기록한다.
     * 심야: 00:00 ~ 06:00
     * 오전: 06:00 ~ 12:00
     * 오후: 12:00 ~ 18:00
     * 저녁: 18:00 ~ 00:00
     *
     * 입장 시간이 각 입장 시간대보다 빠르다면 입장 시간대의 "마감 시간"까지를 체류 시간으로 기록한다.
     * 예) 05:30 입장 -> 06:00 마감까지 30분 체류
     * 예) 10:00 입장 -> 12:00 마감까지 120분 체류
     * 예) 19:00 입장 -> 24:00(=00:00)까지 300분 체류
     *
     * 같은 입장 시간대에 접속한 경우 이전 세션과 합쳐서 기록한다.
     *
     * @param userId    사용자 식별값
     * @param exitTime 입장 시간
     // * @return 생성된 AttendanceSessions 엔티티 목록 (하나 이상일 수 있음)
     */
    public void recordSession(Long userId,
                                                  LocalDateTime exitTime, Integer stayDurationMinutes) {
        // 데이터베이스에서 userId와 date가 같은 세션을 조회
        List<AttendanceSessions> sessions =
                attendanceSessionsRepository.findByUserIdAndDateAndTimeSession(
                        userId,
                        exitTime.toLocalDate(),
                        new TimeSession(exitTime));
        if (sessions.size() > 1) {
            // 같은 시간대에 여러 세션이 존재하면 오류
            logger.error("동일 시간대에 여러 세션이 존재합니다. userId=" + userId + ", exitTime=" + exitTime);
        }
        if (sessions.isEmpty()){
            AttendanceSessions session = new AttendanceSessions(
                    userId,
                    exitTime,
                    stayDurationMinutes,
                    exitTime.toLocalDate()
            );
            attendanceSessionsRepository.save(session);
            return;
        }
        sessions.getFirst().updateStayDurationMinutes(stayDurationMinutes);
        attendanceSessionsRepository.save(sessions.getFirst());
    }

    /**
     * 사용자의 최근 1주일 기록을 반환한다.
     * 해당하는 record 가 없다면 새로 생성 후 strayDurationMinutes 필드를 0으로 채워서 반환한다.
     * @param userId 사용자 아이디
     * @param date 기준날짜
     * @return 최근 1주일 기록
     */
    public Map<LocalDate, Map<String, AttendanceSessions>> findSessionsByUserIdAndDateBetween(Long userId, LocalDate date) {
        // 최근 1주일
        int week = 7;
        LocalDate startDate = date.minusDays(week);
        LocalDate endDate = date.plusDays(1);

        List<String> timeSessionValues = TimeSession.getAllValues();
        List<AttendanceSessions> existingSessions = attendanceSessionsRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(userId, startDate, endDate);
        List<LocalDate> dateRange = startDate.datesUntil(endDate).toList();

        // 기존 데이터를 빠르게 조회할 수 있도록 Map 변환 (날짜 → (세션 값 → AttendanceSessions))
        Map<LocalDate, Map<String, AttendanceSessions>> sessionMap = existingSessions.stream()
                .collect(Collectors.groupingBy(
                        AttendanceSessions::getDate, // 날짜별 그룹화
                        Collectors.toMap(
                                s -> s.getTimeSession().getSessionAsString(), // 세션 값을 Key로 저장
                                s -> s,
                                (oldValue, newValue) -> newValue // 중복 방지

                        )
                ));


        // 모든 날짜 + 모든 세션을 포함하도록 보정
        for (LocalDate currentDate : dateRange) {
            sessionMap.putIfAbsent(currentDate, new HashMap<>()); // 날짜 없으면 기본 추가
            Map<String, AttendanceSessions> sessionsForDate = sessionMap.get(currentDate);

            for (String timeSessionValue : timeSessionValues) {
                sessionsForDate.putIfAbsent(timeSessionValue, new AttendanceSessions(userId, currentDate.atStartOfDay(), 0, currentDate));
            }
        }

        return sessionMap;
    }


    /**
     * currentTime이 속한 블록의 마감 시각을 구하는 메서드
     * - 00:00 ~ 06:00 (마감: 06:00)
     * - 06:00 ~ 12:00 (마감: 12:00)
     * - 12:00 ~ 18:00 (마감: 18:00)
     * - 18:00 ~ 24:00 (마감: 00:00 -> 다음 날)
     */
    private LocalDateTime getBlockEnd(LocalDateTime time) {
        int hour = time.getHour();

        if (hour < 6) {
            return time.toLocalDate().atTime(LocalTime.of(6, 0));
        } else if (hour < 12) {
            return time.toLocalDate().atTime(LocalTime.of(12, 0));
        } else if (hour < 18) {
            return time.toLocalDate().atTime(LocalTime.of(18, 0));
        } else {
            // 다음날 0시(=24:00)
            return time.toLocalDate().plusDays(1).atTime(LocalTime.of(0, 0));
        }
    }

    // private void handleMissingAndCurrentRecords(Long userId,
    //                           LocalDateTime entryTime,
    //                           LocalDateTime exitTime) {
    //     // 여러 세션이 기록될 수 있으므로 리스트로 관리
    //     List<AttendanceSessions> sessionsList = new ArrayList<>();
    //
    //     // 현재 처리 중인 시각을 entryTime으로 시작
    //     LocalDateTime currentTime = entryTime;
    //
    //     // 퇴장 시각 이전까지 반복
    //     while (currentTime.isBefore(exitTime)) {
    //         // 1) currentTime이 속한 블록의 마감 시점 구하기
    //         LocalDateTime blockEnd = getBlockEnd(currentTime);
    //
    //         // 2) 실제 마감 시점은 blockEnd와 exitTime 중 "더 이른 시각"
    //         LocalDateTime actualEnd = blockEnd.isAfter(exitTime) ? exitTime : blockEnd;
    //
    //         // 3) 체류 시간(분) 계산
    //         long stayMinutes = ChronoUnit.MINUTES.between(currentTime, actualEnd);
    //         if (stayMinutes < 0) {
    //             stayMinutes = 0; // 혹시 역전이 발생하면 0처리
    //         }
    //
    //         // 당일 동일한 시간대에 이미 기록된 세션이 있는지 확인
    //
    //         // 4) 세션 엔티티 생성 (userId, 시작 시각, 체류 시간)
    //         AttendanceSessions session = new AttendanceSessions(
    //                 userId,
    //                 currentTime,
    //                 (int) stayMinutes,
    //                 currentTime.toLocalDate()
    //         );
    //         sessionsList.add(session);
    //
    //         // 5) 다음 블록을 위해 currentTime을 actualEnd로 이동
    //         currentTime = actualEnd;
    //     }
    //
    //     // 한 번에 여러 레코드 저장
    //     attendanceSessionsRepository.saveAll(sessionsList);
    // }
}
