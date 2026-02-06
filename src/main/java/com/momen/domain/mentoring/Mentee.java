package com.momen.domain.mentoring;

import com.momen.core.entity.BaseTimeEntity;
import com.momen.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "mentees")
public class Mentee extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mentee_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    private Mentor mentor; // 담당 멘토

    @Column(length = 20)
    private String grade; // 학년

    @Column(name = "target_university", length = 100)
    private String targetUniversity; // 목표 대학

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "mentee_cards", joinColumns = @JoinColumn(name = "mentee_id"))
    @Column(name = "card", length = 50)
    private List<String> cards = new ArrayList<>();

    public Mentee(User user, Mentor mentor, String grade, String targetUniversity) {
        this.user = user;
        this.mentor = mentor;
        this.grade = grade;
        this.targetUniversity = targetUniversity;
    }
}
