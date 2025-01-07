package com.mokako.platform.member;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;


import java.math.BigInteger;

@Entity
@NoArgsConstructor
@ToString
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 19, scale = 0, nullable = false) // 19자리 정수, 소수점 없음
    private long discord_id;

    // 서버 프로필 이름
    @Column(nullable = false)
    private String effectiveName;

    // 계정 이름
    @Column(nullable = false)
    private String name;

    protected User(long discord_id, String effectiveName, String name) {
        this.discord_id = discord_id;
        this.effectiveName = effectiveName;
        this.name = name;
    }
}
