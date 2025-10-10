package ese.trab01.Tickets.client;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.time.OffsetDateTime;

@Component
public class EventClient {
    private final RestTemplate rest = new RestTemplate();
    @Value("${services.events.baseUrl:http://localhost:8081}")
    String base;

    public EventInfo getEvent(Long eventId) {
        // TODO trocar por OpenFeign se preferir
        return rest.getForObject(base + "/events/" + eventId, EventInfo.class);
    }

    @Data
    public static class EventInfo {
        private Long id;
        private String title;
        private Integer capacity; // capacidade máxima
        private boolean active;
        private OffsetDateTime dateTime;
        private String category;
        private String place;
    }
}
