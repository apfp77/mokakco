package com.mokakco.platform.member;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class UserException extends RuntimeException {

    private final Reason reason;
    private final String customMessage;
    private final Logger logger = LoggerFactory.getLogger("errorLogger");

    public enum Reason {
        ID_NOT_FOUND("사용자를 찾을 수 없습니다. ID: {0}"),
        EMAIL_NOT_FOUND("사용자 이메일({0})을 찾을 수 없습니다."),
        INVALID_INPUT("입력값({0})이 잘못되었습니다."),
        UNKNOWN_ERROR("알 수 없는 오류가 발생했습니다: {0}");

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
    
    public UserException(Reason reason, String... args) {
        super(reason.formatMessage(args)); // 포맷 메시지로 동적으로 메시지 생성
        this.reason = reason;
        this.customMessage = reason.formatMessage(args);
        logger.error(customMessage);
    }

}
