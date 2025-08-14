package com.adithya.trackfolio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_skills", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "skill"})
})
@NoArgsConstructor
@Getter
@Setter
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "skill", nullable = false, length = 100)
    private String skill;

    public Skill(Long userId, String skill) {
        this.userId = userId;
        this.skill = skill;
    }
}