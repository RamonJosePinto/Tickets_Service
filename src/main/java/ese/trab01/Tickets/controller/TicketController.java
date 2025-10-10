package ese.trab01.Tickets.controller;


import ese.trab01.Tickets.model.Reservation;
import ese.trab01.Tickets.model.Ticket;
import ese.trab01.Tickets.model.enums.PaymentMethod;
import ese.trab01.Tickets.model.enums.ReservationStatus;
import ese.trab01.Tickets.repository.ReservationRepository;
import ese.trab01.Tickets.repository.TicketRepository;
import ese.trab01.Tickets.service.TicketService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService service;
    private final ReservationRepository reservationRepo;

    @PostMapping("/reservations")
    public ResponseEntity<?> reserve(@RequestBody ReserveRequest req) {
        // por enquanto usamos o e-mail enviado pelo cliente
        var email = req.email();
        var res = service.reserve(req.eventId(), email, req.quantity(), req.method());
        return ResponseEntity.ok(new ReserveResponse(res.getId(), res.getStatus(), res.getExpiresAt()));
    }

    @PostMapping("/reservations/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable Long id, @RequestBody ConfirmRequest req) {
        service.confirmPayment(id, req.paymentId());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/reservations/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        service.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public Page<Ticket> my(@RequestParam String email,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size) {
        return service.listMyTickets(email, PageRequest.of(page, size, Sort.by("purchasedAt").descending()));
    }

    @PostMapping("/validate/{code}")
    public Ticket validate(@PathVariable String code) {
        return service.validate(code);
    }

    @GetMapping("/reservations/{id}")
    public Reservation getReservation(@PathVariable Long id) {
        return reservationRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reserva não encontrada"));
    }

    // DTOs
    public record ReserveRequest(Long eventId, int quantity, PaymentMethod method, String email) {
    }

    public record ReserveResponse(Long reservationId, ReservationStatus status, OffsetDateTime expiresAt) {
    }

    public record ConfirmRequest(String paymentId) {
    }
}