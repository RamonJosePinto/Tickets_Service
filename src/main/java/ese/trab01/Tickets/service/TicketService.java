package ese.trab01.Tickets.service;

import ese.trab01.Tickets.client.EventClient;
import ese.trab01.Tickets.client.NotificationClient;
import ese.trab01.Tickets.dto.TicketReserveRequestDto;
import ese.trab01.Tickets.model.Ticket;
import ese.trab01.Tickets.model.enums.TicketStatus;
import ese.trab01.Tickets.repository.TicketRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepo;
    private final EventClient eventClient;
    private final NotificationClient notificationClient;

    @Value("${tickets.reserve.ttl-minutes:15}")
    private int reserveTtlMinutes;

    /**
     * Reserva um ingresso (1 ticket)
     */
    @Transactional
    public Ticket reserve(TicketReserveRequestDto req) {
        EventClient.EventInfo event = eventClient.getEventById(req.getEventId());
        if (event == null || event.getStatus() == null) {
            throw new EntityNotFoundException("Evento não encontrado.");
        }
        
        long confirmedCount = ticketRepo.countByEventIdAndStatus(req.getEventId(), TicketStatus.CONFIRMED);
        if (event.getCapacidade() != null && confirmedCount >= event.getCapacidade()) {
            throw new IllegalStateException("Evento lotado.");
        }

        Ticket ticket = Ticket.builder()
                .code(UUID.randomUUID().toString())
                .participantId(req.getParticipantId())
                .eventId(req.getEventId())
                .status(TicketStatus.RESERVED)
                .expiresAt(OffsetDateTime.now().plus(reserveTtlMinutes, ChronoUnit.MINUTES))
                .method(req.getMethod())
                .build();

        notificationClient.registrationConfirmation(req.getParticipantId());

        return ticketRepo.save(ticket);
    }

    /**
     * Confirma (pós-pagamento)
     */
    @Transactional
    public void confirm(Long ticketId) {
        Ticket t = ticketRepo.findById(ticketId).orElseThrow(EntityNotFoundException::new);

        if (t.getStatus() == TicketStatus.CANCELED || t.getStatus() == TicketStatus.EXPIRED) {
            throw new IllegalStateException("Ticket não pode ser confirmado (cancelado/expirado).");
        }
        if (t.getStatus() == TicketStatus.CONFIRMED) return;

        if (t.getExpiresAt() != null && OffsetDateTime.now().isAfter(t.getExpiresAt())) {
            t.setStatus(TicketStatus.EXPIRED);
            ticketRepo.save(t);
            throw new IllegalStateException("Reserva expirada.");
        }

        t.setStatus(TicketStatus.CONFIRMED);
        t.setConfirmedAt(OffsetDateTime.now());
        ticketRepo.save(t);


        notificationClient.sendPurchaseConfirmation(t.getParticipantId(), t.getEventId(), t.getId());
    }

    /**
     * Cancela
     */
    @Transactional
    public void cancel(Long ticketId) {
        Ticket t = ticketRepo.findById(ticketId).orElseThrow(EntityNotFoundException::new);
        if (t.getStatus() == TicketStatus.USED) {
            throw new IllegalStateException("Ticket já utilizado.");
        }
        if (t.getStatus() == TicketStatus.CANCELED || t.getStatus() == TicketStatus.EXPIRED) return;

        t.setStatus(TicketStatus.CANCELED);
        t.setCanceledAt(OffsetDateTime.now());
        ticketRepo.save(t);

        notificationClient.sendTicketCanceled(t.getParticipantId(), t.getEventId(), t.getId(), null);
    }

    /**
     * Cancela e atualiza o status com o valor recebido no corpo
     */
    @Transactional
    public void cancel(Long ticketId, TicketStatus newStatus) {
        Ticket t = ticketRepo.findById(ticketId).orElseThrow(EntityNotFoundException::new);
        if (t.getStatus() == TicketStatus.USED) {
            throw new IllegalStateException("Ticket já utilizado.");
        }
        if (t.getStatus() == TicketStatus.CANCELED || t.getStatus() == TicketStatus.EXPIRED) return;

        t.setStatus(newStatus);
        t.setCanceledAt(OffsetDateTime.now());
        ticketRepo.save(t);

        notificationClient.sendTicketCanceled(t.getParticipantId(), t.getEventId(), t.getId(), null);
    }

    @Transactional
    public void validateUse(String code) {
        Ticket t = ticketRepo.findByCode(code).orElseThrow(EntityNotFoundException::new);
        if (t.getStatus() != TicketStatus.CONFIRMED) {
            throw new IllegalStateException("Ticket não está confirmado.");
        }
        t.setStatus(TicketStatus.USED);
        t.setUsedAt(OffsetDateTime.now());
        ticketRepo.save(t);
    }

    /**
     * Expira reservas antigas (job agendado)
     */
    @Transactional
    public int expireOldReservations() {
        var now = OffsetDateTime.now();
        var toExpire = ticketRepo.findAll().stream()
                .filter(t -> t.getStatus() == TicketStatus.RESERVED
                        && t.getExpiresAt() != null
                        && now.isAfter(t.getExpiresAt()))
                .toList();
        toExpire.forEach(t -> t.setStatus(TicketStatus.EXPIRED));
        ticketRepo.saveAll(toExpire);
        return toExpire.size();
    }

    public Page<Ticket> list(Pageable pageable) {
        return ticketRepo.findAll(pageable);
    }

    public Page<Ticket> listByParticipant(UUID participantId, Pageable pageable) {
        return ticketRepo.findByParticipantId(participantId, pageable);
    }
}
