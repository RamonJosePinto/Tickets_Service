package ese.trab01.Tickets.repository;

import ese.trab01.Tickets.model.Ticket;
import ese.trab01.Tickets.model.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByCode(String code);

    Page<Ticket> findByParticipantId(UUID participantId, Pageable pageable);

    long countByEventIdAndStatus(Long eventId, TicketStatus status);
}
