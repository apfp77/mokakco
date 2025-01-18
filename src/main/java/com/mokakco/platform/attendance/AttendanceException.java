package com.mokakco.platform.attendance;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class AttendanceException extends RuntimeException {

    private final Logger logger = LoggerFactory.getLogger("errorLogger");
    private final Reason reason;
    private final String customMessage;

    public enum Reason {
        MULTIPLE_PENDING_EXIT("퇴근 처리 대기 중인 기록이 여러 개입니다. 사용자 ID: {0}"),
        NO_PENDING_EXIT("퇴근 처리 대기 중인 기록이 없습니다. 사용자 ID: {0}");

        private final String messageTemplate;

        Reason(String messageTemplate) {
            this.messageTemplate = messageTemplate;
        }

        public String formatMessage(String... args) {
            // {0}, {1}, ... 형태의 포맷 문자열을 동적으로 치환
            String formattedMessage = messageTemplate;
            for (int i = 0; i < args.length; i++) {
                formattedMessage = formattedMessage.replace("{" + i + "}", args[i]);
            }
            return formattedMessage;
        }
    }

    public AttendanceException(Reason reason, String... args) {
        super(reason.formatMessage(args)); // 포맷 메시지로 동적으로 메시지 생성
        this.reason = reason;
        this.customMessage = reason.formatMessage(args);
        logger.error(customMessage);
    }
}


