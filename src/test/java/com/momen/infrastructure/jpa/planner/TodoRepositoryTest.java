package com.momen.infrastructure.jpa.planner;

import com.momen.domain.mentoring.Mentee;
import com.momen.domain.planner.CreatorType;
import com.momen.domain.planner.Todo;
import com.momen.domain.user.User;
import com.momen.domain.user.UserRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TodoRepository 통합 테스트
 *
 * MariaDB-specific columnDefinition (TINYINT(1), reserved words like MONTH/YEAR) 때문에
 * H2에서는 스키마 생성이 불가하므로, 이 테스트는 MariaDB가 실행 중일 때만 동작합니다.
 * CI 환경에서 MariaDB 서비스를 추가하거나, Testcontainers를 도입하여 실행할 수 있습니다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:mariadb://localhost:3306/momen_test?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Seoul",
    "spring.datasource.driver-class-name=org.mariadb.jdbc.Driver",
    "spring.datasource.username=root",
    "spring.datasource.password=1234",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MariaDBDialect",
    "spring.sql.init.mode=never"
})
@org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable(named = "INTEGRATION_TEST", matches = "true")
class TodoRepositoryTest {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private EntityManager entityManager;

    private Mentee testMentee;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .loginId("testmentee")
                .email("mentee@test.com")
                .passwordHash("encoded")
                .name("테스트멘티")
                .role(UserRole.MENTEE)
                .build();
        entityManager.persist(user);

        testMentee = new Mentee(user, null, "고1");
        entityManager.persist(testMentee);

        Todo mathTodo = new Todo(testMentee, "수학 문제풀이", "MATH", "미적분 연습",
                LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 15), user.getId(), CreatorType.MENTOR);
        entityManager.persist(mathTodo);

        Todo englishTodo = new Todo(testMentee, "영어 독해", "ENGLISH", "지문 분석",
                LocalDate.of(2025, 1, 12), LocalDate.of(2025, 1, 14), user.getId(), CreatorType.MENTOR);
        entityManager.persist(englishTodo);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("findByMenteeIdAndDate - 날짜 범위 내 Todo 조회 성공")
    void findByMenteeIdAndDate_성공() {
        LocalDate queryDate = LocalDate.of(2025, 1, 13);

        List<Todo> result = todoRepository.findByMenteeIdAndDate(testMentee.getId(), queryDate);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findByMenteeIdAndDateAndSubjects - 과목 필터링")
    void findByMenteeIdAndDateAndSubjects_과목필터() {
        LocalDate queryDate = LocalDate.of(2025, 1, 13);

        List<Todo> result = todoRepository.findByMenteeIdAndDateAndSubjects(
                testMentee.getId(), queryDate, List.of("MATH"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSubject()).isEqualTo("MATH");
    }

    @Test
    @DisplayName("findByMenteeId - 데이터 없을 때 빈 리스트")
    void findByMenteeId_빈결과() {
        List<Todo> result = todoRepository.findByMenteeId(9999L);

        assertThat(result).isEmpty();
    }
}
