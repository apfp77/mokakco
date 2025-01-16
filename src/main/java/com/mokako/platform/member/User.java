package com.mokako.platform.member;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor
@ToString
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 19, scale = 0, nullable = false) // 19자리 정수, 소수점 없음
    private long discordId;

    // 서버 프로필 이름
    @Column(nullable = false)
    private String effectiveName;

    // 계정 이름
    @Column(nullable = false)
    private String name;

    protected User(long discordId, String effectiveName, String name) {
        this.discordId = discordId;
        this.effectiveName = effectiveName;
        this.name = name;
    }
}
