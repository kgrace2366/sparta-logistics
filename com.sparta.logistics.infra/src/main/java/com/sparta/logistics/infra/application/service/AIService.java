package com.sparta.logistics.infra.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.logistics.infra.application.util.AIUtil;
import com.sparta.logistics.infra.domain.ai.Ai;
import com.sparta.logistics.infra.domain.slack.SlackToSendMessage;
import com.sparta.logistics.infra.infrastructure.persistence.entity.AIEntity;
import com.sparta.logistics.infra.infrastructure.persistence.repository.AIRepository;
import com.sparta.logistics.infra.infrastructure.slack.adapter.SlackMessageAdapter;
import com.sparta.logistics.infra.persistence.rest.dto.InfraRequestDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AIService {

    @Value("${ai.apiKey}")
    private String apiKey;

    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";
    private final AIRepository aiRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SlackMessageAdapter slackMessageAdapter;

    public AIEntity generateResponse(Ai ai) {

        String aiPrompt = buildAIRequestPrompt(ai);

        String requestBody = "{ \"contents\": [ { \"parts\": [ { \"text\": \"" + aiPrompt.replace("\"", "\\\"") + "\" } ] } ] }";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            API_URL + "?key=" + apiKey, HttpMethod.POST, request, String.class
        );

        String formattedResponse = parseFormattedResponse(response);
        AIEntity aiDescription = AIEntity.builder()
                .question(aiPrompt)
                .answer(formattedResponse.toString())
                .build();

        String message = AIUtil.createSlackMessage(ai, aiDescription.getAnswer());
        sendMessageToSlack(message, ai.getSnsAccount());
        return aiRepository.save(aiDescription);
    }

    @NotNull
    private String parseFormattedResponse(ResponseEntity<String> response) {
        String aiResponse = response.getBody();
        JsonNode rootNode = null;

        try {
            rootNode = objectMapper.readTree(aiResponse);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Unable to parse AI response");
        }

        JsonNode candidates = rootNode.path("candidates");
        StringBuilder formattedResponse = new StringBuilder();

        if (candidates.isArray() && candidates.size() > 0) {
            JsonNode candidate = candidates.get(0);
            JsonNode content = candidate.path("content");
            if (content.has("parts")) {
                for (JsonNode part : content.path("parts")) {
                    String text = part.path("text").asText();
                    text = text.replaceAll("\\\\", " ");
                    String datePattern = "\\d{1,2}월 \\d{1,2}일 \\d{1,2}시";
                    Pattern pattern = Pattern.compile(datePattern);
                    Matcher matcher = pattern.matcher(text);
                    if (matcher.find()) {
                        formattedResponse.append(matcher.group());
                    }
                }
            }
        }
        return formattedResponse.toString();
    }

    private void sendMessageToSlack(String message, String snsAccount) {
        SlackToSendMessage slackToSendMessage =
            new SlackToSendMessage(message, List.of(snsAccount));
        slackMessageAdapter.sendMessage(slackToSendMessage);
    }

    private String buildAIRequestPrompt(Ai ai) {
        return String.format(
                "상품: %s, 수량: %d, 요청: %s, 출발지: %s, 경유지: %s, 목적지: %s: "
                        + "위 데이터를 바탕으로 최종 발송 시한을 00월 00일 00시 형식으로만 답변해 주세요.",
                ai.getProductName(), ai.getProductQuantity(), ai.getRequest(),
                ai.getOriginHub(), ai.getDestinationHub(), ai.getCompanyAddress()
        );
    }
}
