package ese.trab01.Tickets.controller;

import ese.trab01.Tickets.dto.TicketReserveRequestDto;
import ese.trab01.Tickets.dto.TicketReserveResponseDto;
import ese.trab01.Tickets.dto.TicketResponseDto;
import ese.trab01.Tickets.dto.TicketCancelRequestDto;
import ese.trab01.Tickets.model.Ticket;
import ese.trab01.Tickets.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService service;

    @PostMapping("/reserve")
    public ResponseEntity<TicketReserveResponseDto> reserve(@Valid @RequestBody TicketReserveRequestDto req) {
        Ticket t = service.reserve(req);
        return ResponseEntity.status(201).body(
                new TicketReserveResponseDto(t.getId(), t.getCode(), t.getStatus(), t.getExpiresAt())
        );
    }

    @PostMapping("/{ticketId}/confirm")
    public ResponseEntity<Void> confirm(@PathVariable Long ticketId) {
        service.confirm(ticketId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{ticketId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long ticketId, @Valid @RequestBody TicketCancelRequestDto req) {
        service.cancel(ticketId, req.getStatus());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/validate/{code}")
    public ResponseEntity<Void> validate(@PathVariable String code) {
        service.validateUse(code);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> getMeusTickets(
            Pageable pageable,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesCsv
    ) {
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado.");
        if (!hasRole(rolesCsv, "CLIENTE"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas CLIENTE pode consultar.");

        return ResponseEntity.ok(service.buscarEventosDoParticipante(userId, pageable));
    }

    @GetMapping
    public ResponseEntity<Page<TicketResponseDto>> list(Pageable pageable) {
        var page = service.list(pageable);
        var dtoPage = new PageImpl<>(
                page.getContent().stream()
                        .map(t -> new TicketResponseDto(
                                t.getId(), t.getCode(), t.getEventId(), t.getParticipantId(), t.getStatus(),
                                t.getCreatedAt(), t.getConfirmedAt(), t.getCanceledAt(), t.getUsedAt()))
                        .toList(),
                pageable, page.getTotalElements()
        );
        return ResponseEntity.ok(dtoPage);
    }

    private boolean hasRole(String rolesCsv, String role) {
        if (rolesCsv == null || rolesCsv.isBlank()) return false;
        for (String r : rolesCsv.split(",")) {
            if (role.equalsIgnoreCase(r.trim())) return true;
        }
        return false;
    }
}
