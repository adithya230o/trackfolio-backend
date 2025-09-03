package com.adithya.trackfolio.service;

import com.adithya.trackfolio.dto.ChatRequestDTO;
import com.adithya.trackfolio.entity.JD;
import com.adithya.trackfolio.entity.Skill;
import com.adithya.trackfolio.repository.JDRepository;
import com.adithya.trackfolio.repository.SkillRepository;
import com.adithya.trackfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final JDRepository jdRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final WebClient webClient;

    public String handleChat(ChatRequestDTO request) {
        // 1. Get email from JWT auth context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        // 2. Resolve user_id from DB
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        // 3. Get JD text for drive_id
        String jdText = jdRepository.findByDriveId(request.getDriveId())
                .map(JD::getJdText)
                .orElse("");

        // 4. Get skills for user
        List<String> skills = Optional.ofNullable(skillRepository.findByUserId(userId))
                .orElse(List.of())   // empty list if null
                .stream()
                .map(Skill::getSkill)
                .collect(Collectors.toList());

        // 5. Build prompt
        String prompt = buildPrompt(jdText, skills, request.getQuestion());

        // 6. Prepare JSON payload
        String aiRequestJson = String.format("{\"prompt\":\"%s\"}", escapeJson(prompt));

        // 7. Send to AI-core microservice using WebClient
        String aiCoreUrl = "http://localhost:8081/api/prompt/userPrompt";

        return webClient.post()
                .uri(aiCoreUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(aiRequestJson)
                .retrieve()
                .bodyToMono(String.class)
                .block(); // sync for now
    }

    private String buildPrompt(String jdText, List<String> skills, String question) {
        StringBuilder prompt = new StringBuilder("You are an AI chatbot helping a user prepare for a company placement drive.\n\n");

        if (!skills.isEmpty()) {
            prompt.append("The user's skills are:\n")
                    .append(String.join(", ", skills))
                    .append("\n\n");
        }

        if (!jdText.isEmpty()) {
            prompt.append("The Job Description (JD) for the company drive is:\n")
                    .append(jdText)
                    .append("\n\n");
        }

        prompt.append("Answer the following question concisely and to the point. ")
                .append("Do not include unnecessary lines, symbols, or formatting. ")
                .append("Keep the explanation simple and easy to understand. Provide precise answers, as longer responses will increase API token usage.\n")
                .append("Question: \"")
                .append(question)
                .append("\"");

        return prompt.toString();
    }

    private String escapeJson(String text) {
        return text.replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}