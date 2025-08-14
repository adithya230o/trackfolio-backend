package com.adithya.trackfolio.repository;

import com.adithya.trackfolio.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByUserId(Long userId);

    Optional<Skill> findByUserIdAndSkill(Long userId, String skill);

    void deleteByUserId(Long userId);
}