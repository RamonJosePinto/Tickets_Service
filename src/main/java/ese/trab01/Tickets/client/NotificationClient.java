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
    public void registrationConfirmation(Long participantId) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/notifications/registration-confirmation")
                .toUriString();

        Map<String, Object> body = Map.of("participantId", participantId);
        log.info("[mock] Notificação de inscricao registrada");
        try {
            rest.postForEntity(url, body, Void.class);
        } catch (RestClientException ex) {
            log.warn("Failed to notify: {}", ex.getMessage());
        }
    }

    // tickets-service: client/NotificationClient.java
    public void sendTicketCanceled(Long participantId, Long eventId, Long ticketId, String reason) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/notifications/ticket-canceled")
                .toUriString();

        Map<String, Object> body = new HashMap<>();
        body.put("participantId", participantId);
        body.put("eventId", eventId);
        body.put("ticketId", ticketId);
        if (reason != null) body.put("reason", reason);

        try {
            rest.postForEntity(url, body, Void.class);
        } catch (RestClientException ex) {
            log.warn("Failed to notify cancel: {}", ex.getMessage());
        }
    }

}