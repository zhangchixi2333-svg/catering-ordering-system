package org.example.notificationservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class UserOnlineStatusClient {

    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public UserOnlineStatusClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${services.user-service-url:http://localhost:8087}") String userServiceUrl) {
        this.restTemplate = restTemplateBuilder.build();
        this.userServiceUrl = userServiceUrl;
    }

    public void updateOnlineStatus(Long userId, boolean online) {
        if (userId == null) {
            return;
        }
        try {
            restTemplate.postForObject(
                    userServiceUrl + "/api/auth/internal/users/" + userId + "/online?online=" + online,
                    null,
                    Object.class);
        } catch (Exception e) {
            log.warn("Failed to sync user online status - userId: {}, online: {}", userId, online, e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<Long, Boolean> getOnlineStatuses(List<Long> userIds) {
        Map<Long, Boolean> statuses = new LinkedHashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return statuses;
        }
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(userServiceUrl + "/api/auth/internal/users/online");
            userIds.stream().distinct().forEach(userId -> builder.queryParam("userIds", userId));
            Map<String, Object> response = restTemplate.getForObject(builder.toUriString(), Map.class);
            Object data = response == null ? null : response.get("data");
            if (data instanceof Map<?, ?> map) {
                map.forEach((key, value) -> statuses.put(Long.valueOf(String.valueOf(key)), Boolean.TRUE.equals(value)));
            }
        } catch (Exception e) {
            log.warn("Failed to query user online statuses - userIds: {}", userIds, e);
        }
        return statuses;
    }
}
