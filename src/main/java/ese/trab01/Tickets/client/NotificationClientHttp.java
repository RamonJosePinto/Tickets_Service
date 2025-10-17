package ese.trab01.Tickets.client.http;

import ese.trab01.Tickets.client.NotificationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@Profile("!stub") // qualquer perfil que não seja "stub"
@RequiredArgsConstructor
public class NotificationClientHttp implements NotificationClient {

    private final RestTemplate restTemplate;

    @Value("${notifications.base-url}")
    private String baseUrl;

    @Override
    public void sendPurchaseConfirmation(String recipientEmail, Long eventId, Long reservationId, Integer quantity) {
        String url = baseUrl + "/notifications/purchase";

        String body = """
                {
                  "recipientEmail": "%s",
                  "eventId": %d,
                  "reservationId": %d,
                  "quantity": %d
                }
                """.formatted(recipientEmail, eventId, reservationId, quantity);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
            log.info("Notificação (HTTP) enviada para {} (reserva #{}, evento {}, qty {})",
                    recipientEmail, reservationId, eventId, quantity);
        } catch (Exception e) {
            // não derruba o fluxo do pagamento por causa da notificação mock/externa
            log.warn("Falha ao enviar notificação (HTTP): {}", e.getMessage());
        }
    }
}
