package ese.trab01.Tickets.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import ese.trab01.Tickets.model.Ticket;

import java.util.Map;

@Component
@Slf4j
public class PaymentClient {

    private final RestTemplate rest = new RestTemplate();

    @Value("${services.payment.base-url}")
    private String baseUrl;

    public void createBilling(Ticket ticket) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/billings")
                .toUriString();

        String dueDate = ticket.getExpiresAt().toString();
        log.info("Due date test :: " + dueDate);

        Map<String, Object> body = Map.of(
                "ticketId", ticket.getCode(),
                "value",10.00f, // ticket.getValue(),
                "status", "PENDING",
                "dueDate", dueDate //dueDate
        );


        try {
            rest.postForEntity(url, body, Void.class);
        } catch (RestClientException ex) {
            log.warn("Failed to create billing: {}", ex.getMessage());
        }
    }

}