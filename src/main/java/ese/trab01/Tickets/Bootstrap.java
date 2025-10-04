package ese.trab01.Tickets;

import ese.trab01.Tickets.model.Ticket;
import ese.trab01.Tickets.repository.TicketRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.OffsetDateTime;

@Configuration
public class Bootstrap {

    Ticket t = new Ticket();

    @Bean
    CommandLineRunner seedTickets(TicketRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.save(Ticket.builder()
                        .eventId(1001L)
                        .code("TEST-ING-001")
                        .status("EMITIDO")
                        .purchaserEmail("ramon@example.com")
                        .purchasedAt(OffsetDateTime.now())
                        .build());
            }
        };
    }
}