package com.adithya.trackfolio.controller;

import com.adithya.trackfolio.dto.SkillRequest;
import com.adithya.trackfolio.dto.SkillResponse;
import com.adithya.trackfolio.service.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/skills")
@RequiredArgsConstructor
@Slf4j
public class SkillController {

    private final SkillService skillService;

    @PostMapping
    public ResponseEntity<SkillResponse> saveSkills(@Valid @RequestBody SkillRequest request) {
        skillService.saveSkills(request.getSkills());
        return ResponseEntity.ok(new SkillResponse(request.getSkills()));
    }

    @GetMapping
    public ResponseEntity<SkillResponse> getSkills() {
        List<String> skills = skillService.getSkills();
        return ResponseEntity.ok(new SkillResponse(skills));
    }
}
