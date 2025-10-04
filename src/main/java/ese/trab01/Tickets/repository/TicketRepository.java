package ese.trab01.Tickets.repository;

import ese.trab01.Tickets.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByCode(String code);
}
