package ese.trab01.Tickets.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class NotificationClient {

    private final RestTemplate rest = new RestTemplate();

    @Value("${services.notifications.base-url}")
    private String baseUrl;

    public void sendPurchaseConfirmation(String recipientEmail,
                                         Long eventId,
                                         Long reservationId,
                                         Integer quantity) {

        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/notifications/purchase-confirmation")
                .toUriString();

        Map<String, Object> body = new HashMap<>();
        body.put("email", recipientEmail);
        body.put("eventId", eventId);
        body.put("reservationId", reservationId);
        body.put("quantity", quantity);

        try {
            rest.postForEntity(url, body, Void.class);
            log.info("Notification sent to {} for reservation {}", recipientEmail, reservationId);
        } catch (RestClientException ex) {
            log.warn("Failed to send notification (email={}, reservationId={}): {}",
                    recipientEmail, reservationId, ex.getMessage());
        }
    }
}
