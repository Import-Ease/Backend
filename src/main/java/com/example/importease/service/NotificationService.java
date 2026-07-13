package com.example.importease.service;

import com.example.importease.model.NotificationToken;
import com.example.importease.repository.NotificationTokenRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class NotificationService {

    private final NotificationTokenRepository notificationTokenRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public NotificationService(NotificationTokenRepository notificationTokenRepository) {
        this.notificationTokenRepository = notificationTokenRepository;
    }

    public void registerToken(String token, String userIdentifier) {
        notificationTokenRepository.findByToken(token).ifPresentOrElse(existing -> {
            existing.setUserIdentifier(userIdentifier);
            notificationTokenRepository.save(existing);
        }, () -> notificationTokenRepository.save(new NotificationToken(token, userIdentifier)));
    }

    public void sendNotification(String title, String body, String userIdentifier) {
        notificationTokenRepository.findAll().stream()
                .filter(token -> userIdentifier == null || userIdentifier.equals(token.getUserIdentifier()))
                .forEach(token -> sendToExpo(token.getToken(), title, body));
    }

    private void sendToExpo(String token, String title, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = Map.of(
                "to", token,
                "sound", "default",
                "title", title,
                "body", body
        );

        try {
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity("https://exp.host/--/api/v2/push/send", request, String.class);
        } catch (Exception ex) {
            System.err.println("Push notification failed: " + ex.getMessage());
        }
    }
}
