package com.mokakco.platform.listener;

import lombok.Getter;

@Getter
public enum CommandEnum {
    ATTENDANCE_TIME("출석시간", "금일 출석 기록을 확인합니다.");

    private final String command;
    private final String description;

    CommandEnum(String command, String description) {
        this.command = command;
        this.description = description;
    }

    public static CommandEnum fromCommand(String command) {
        for (CommandEnum cmd : values()) {
            if (cmd.getCommand().equals(command)) {
                return cmd;
            }
        }
        return null;
    }
}
