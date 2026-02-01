package com.momen.core.config;

import com.momen.domain.mentoring.Mentee;
import com.momen.domain.mentoring.Mentor;
import com.momen.domain.user.User;
import com.momen.domain.user.UserRole;
import com.momen.infrastructure.jpa.mentoring.MenteeRepository;
import com.momen.infrastructure.jpa.mentoring.MentorRepository;
import com.momen.infrastructure.jpa.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserJpaRepository userRepository;
    private final MentorRepository mentorRepository;
    private final MenteeRepository menteeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Test data already exists, skipping seeding.");
            return;
        }

        String encodedPassword = passwordEncoder.encode("test1234!");

        // 멘토 1명
        User mentorUser = User.builder()
                .email("mentor@test.com")
                .passwordHash(encodedPassword)
                .name("김멘토")
                .phone("010-1111-1111")
                .role(UserRole.ADMIN)
                .build();
        mentorUser.verifyEmail();
        userRepository.save(mentorUser);

        Mentor mentor = new Mentor(mentorUser, "수학/영어 전문 멘토입니다.");
        mentorRepository.save(mentor);

        // 멘티 2명
        User mentee1User = User.builder()
                .email("mentee1@test.com")
                .passwordHash(encodedPassword)
                .name("이학생")
                .phone("010-2222-2222")
                .role(UserRole.STUDENT)
                .build();
        mentee1User.verifyEmail();
        userRepository.save(mentee1User);

        Mentee mentee1 = new Mentee(mentee1User, mentor, "고2", "서울대");
        menteeRepository.save(mentee1);

        User mentee2User = User.builder()
                .email("mentee2@test.com")
                .passwordHash(encodedPassword)
                .name("박학생")
                .phone("010-3333-3333")
                .role(UserRole.STUDENT)
                .build();
        mentee2User.verifyEmail();
        userRepository.save(mentee2User);

        Mentee mentee2 = new Mentee(mentee2User, mentor, "고3", "연세대");
        menteeRepository.save(mentee2);

        log.info("Test data seeded: 1 mentor (mentor@test.com), 2 mentees (mentee1@test.com, mentee2@test.com). Password: test1234!");
    }
}
