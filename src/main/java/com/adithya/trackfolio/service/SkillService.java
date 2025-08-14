package com.adithya.trackfolio.service;

import com.adithya.trackfolio.entity.Skill;
import com.adithya.trackfolio.repository.SkillRepository;
import com.adithya.trackfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

    private Long getUserIdFromContext() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
                .getId();
    }

    @Transactional
    public void saveSkills(List<String> skills) {
        Long userId = getUserIdFromContext();

        // Normalize skills: lowercase, trim, distinct
        List<String> normalizedSkills = skills.stream()
                .map(s -> s.toLowerCase().trim())
                .distinct()
                .toList();

        // Delete existing skills for user
        skillRepository.deleteByUserId(userId);
        skillRepository.flush();
        log.info("deleted skills");

        // Insert new skills
        List<Skill> entities = normalizedSkills.stream()
                .map(skill -> new Skill(userId, skill))
                .toList();

        skillRepository.saveAll(entities);
    }

    public List<String> getSkills() {
        Long userId = getUserIdFromContext();

        return skillRepository.findByUserId(userId).stream()
                .map(Skill::getSkill)
                .toList();
    }
}