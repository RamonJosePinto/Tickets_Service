package ese.trab01.Tickets.client;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@Primary
@Profile("stub")
public class EventClientStub extends EventClient {
    @Override
    public EventInfo getEvent(Long eventId) {
        EventInfo e = new EventInfo();
        e.setId(eventId);
        e.setTitle("Evento STUB");
        e.setCapacity(5);
        e.setActive(true);
        e.setCategory("show");
        e.setPlace("Auditório");
        e.setDateTime(OffsetDateTime.parse("2025-12-10T20:00:00Z"));
        return e;
    }
}
