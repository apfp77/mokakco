package com.mokako.platform.member;

import org.springframework.stereotype.Service;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    // discord 회원 정보를 유저로 등록

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private void registerUser(Member member) {
        userRepository.save(new User(
                member.getIdLong(),
                member.getEffectiveName(),
                member.getUser().getName()
        ));
    }

    public void registerNewMembers(List<Member> members) {
        List<User> dbUsers = userRepository.findAll();
        Set<Long> dbUserIds = dbUsers.stream().map(User::getDiscord_id).collect(Collectors.toSet());

        for (Member member : members) {
            if (!dbUserIds.contains(member.getIdLong())) {
                registerUser(member);
            }
        }
    }
}
