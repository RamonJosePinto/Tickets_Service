package ese.trab01.Tickets.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class NotificationClient {

    private final RestTemplate rest = new RestTemplate();

    @Value("${services.notifications.base-url}")
    private String baseUrl;

    public void sendPurchaseConfirmation(Long participantId, Long eventId, Long ticketId) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/notifications/purchase-confirmation")
                .toUriString();

        Map<String, Object> body = Map.of(
                "participantId", participantId,
                "eventId", eventId,
                "ticketId", ticketId
        );

        try {
            rest.postForEntity(url, body, Void.class);
        } catch (RestClientException ex) {
            log.warn("Failed to notify: {}", ex.getMessage());
        }
    }
}