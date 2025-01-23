package com.mokakco.platform.attendance;

import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Embeddable
public class TimeSession {

    private final Session timeSession;

    private enum Session {
        MORNING("MORNING"),
        AFTERNOON("AFTERNOON"),
        EVENING("EVENING"),
        DAWN("DAWN");

        private final String value;

        Session(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
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

    // 현재 세션 값을 문자열로 반환
    public String getSessionAsString() {
        return timeSession.getValue();
    }
}
