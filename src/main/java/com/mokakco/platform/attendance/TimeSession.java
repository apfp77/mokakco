package com.mokakco.platform.attendance;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Embeddable
public class TimeSession {

    @Enumerated(EnumType.STRING)
    private final Session timeSession;

    @Getter
    private enum Session {
        MORNING("MORNING", "오전반"),
        AFTERNOON("AFTERNOON", "오후반"),
        EVENING("EVENING", "저녁반"),
        DAWN("DAWN", "심야반");

        private final String value;
        private final String koreanValue;

        Session(String value, String koreanValue) {
            this.value = value;
            this.koreanValue = koreanValue;
        }

        // 문자열을 기반으로 Session 찾기
        public static Session fromString(String session) {
            return Arrays.stream(Session.values())
                    .filter(s -> s.getValue().equalsIgnoreCase(session))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid session: " + session));
        }

    }

    // 모든 enum의 문자열 값을 리스트로 반환
    public static List<String> getAllValues() {
        return Arrays.stream(Session.values())
                .map(Session::getValue)
                .collect(Collectors.toList());
    }

    /**
     * JPA를 위한 기본 생성자
     * 가급적 사용하지 않는 것을 권장
     */
    protected TimeSession() {
        this.timeSession = determineSession(LocalDateTime.now().getHour());
    }

    protected TimeSession(LocalDateTime time) {
        this.timeSession = determineSession(time.getHour());
    }

    private TimeSession(Session session) {
        this.timeSession = session;
    }

    // 현재 시간 기준으로 세션 결정
    private Session determineSession(int hour) {
        if (hour >= 6 && hour < 12) {
            return Session.MORNING;
        } else if (hour >= 12 && hour < 18) {
            return Session.AFTERNOON;
        } else if (hour >= 18 && hour < 24) {
            return Session.EVENING;
        } else {
            return Session.DAWN;
        }
    }

    // 문자열을 통한 세션 반환 메서드 추가
    public static TimeSession fromString(String sessionString) {
        return new TimeSession(Session.fromString(sessionString));
    }

    public static String findSessionByTime(LocalDateTime time) {
        return new TimeSession(time).getKoreanSession();
    }

    // 현재 세션 값을 문자열로 반환
    public String getSessionAsString() {
        return timeSession.getValue();
    }

    public String getKoreanSession() {
        return timeSession.getKoreanValue();
    }
}
