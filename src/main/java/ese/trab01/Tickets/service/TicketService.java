package ese.trab01.Tickets.service;

import ese.trab01.Tickets.client.EventClient;
import ese.trab01.Tickets.exception.BusinessException;
import ese.trab01.Tickets.model.Reservation;
import ese.trab01.Tickets.model.Ticket;
import ese.trab01.Tickets.model.enums.PaymentMethod;
import ese.trab01.Tickets.model.enums.ReservationStatus;
import ese.trab01.Tickets.model.enums.TicketStatus;
import ese.trab01.Tickets.repository.ReservationRepository;
import ese.trab01.Tickets.repository.TicketRepository;
import jakarta.transaction.Transactional;


import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final ReservationRepository reservationRepo;
    private final TicketRepository ticketRepo;
    private final EventClient eventClient;

    @Value("${tickets.hold.minutes:10}")
    private int holdMinutes;

    @Transactional
    public Reservation reserve(Long eventId, String email, int quantity, PaymentMethod method) {
        var event = eventClient.getEvent(eventId);
        if (event == null || !event.isActive()) {
            throw new BusinessException("MSG001"); // evento não vendendo
        }
        long paid = reservationRepo.sumByEventAndStatus(eventId, ReservationStatus.PAGO);
        long reservedActive = reservationRepo.sumActiveByEventAndStatus(eventId, ReservationStatus.RESERVADO);
        long used = paid + reservedActive;
        
        if (used + quantity > event.getCapacity()) {
            throw new BusinessException("MSG001");
        }
        var now = OffsetDateTime.now();
        var res = Reservation.builder()
                .eventId(eventId).purchaserEmail(email).quantity(quantity)
                .status(ReservationStatus.RESERVADO).method(method)
                .createdAt(now).expiresAt(now.plusMinutes(holdMinutes))
                .build();
        return reservationRepo.save(res);
    }

    @Transactional
    public void confirmPayment(Long reservationId, String paymentId) {
        var r = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reserva não encontrada"));
        if (r.getStatus() != ReservationStatus.RESERVADO) return;
        if (r.getExpiresAt().isBefore(OffsetDateTime.now())) {
            r.setStatus(ReservationStatus.EXPIRADO);
            return;
        }
        r.setStatus(ReservationStatus.PAGO);
        r.setPaymentId(paymentId);
        // Gera N tickets
        for (int i = 0; i < r.getQuantity(); i++) {
            var t = Ticket.builder()
                    .eventId(r.getEventId())
                    .reservationId(r.getId())
                    .purchaserEmail(r.getPurchaserEmail())
                    .code(UUID.randomUUID().toString())
                    .status(TicketStatus.EMITIDO)
                    .purchasedAt(OffsetDateTime.now())
                    .build();
            ticketRepo.save(t);
        }
    }

    @Transactional
    public void cancel(Long reservationId) {
        var r = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reserva não encontrada"));
        if (r.getStatus() == ReservationStatus.PAGO) {
            throw new BusinessException("Reserva já paga; emitir estorno no serviço de pagamentos.");
        }
        r.setStatus(ReservationStatus.CANCELADO);
    }

    @Transactional
    public Ticket validate(String code) {
        var t = ticketRepo.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Ingresso não encontrado"));
        if (t.getStatus() == TicketStatus.VALIDADO)
            throw new BusinessException("Ingresso já validado.");
        t.setStatus(TicketStatus.VALIDADO);
        return t;
    }

    public Page<Ticket> listMyTickets(String email, Pageable pageable) {
        return ticketRepo.findByPurchaserEmail(email, pageable);
    }

    // Expira reservas
    @Scheduled(fixedDelayString = "${tickets.expire.millis:60000}")
    @Transactional
    public void expireHolds() {
        var now = OffsetDateTime.now();
        reservationRepo.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.RESERVADO && r.getExpiresAt().isBefore(now))
                .forEach(r -> r.setStatus(ReservationStatus.EXPIRADO));
    }
}
