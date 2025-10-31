package ese.trab01.Tickets;

import ese.trab01.Tickets.client.EventClient;
import ese.trab01.Tickets.client.NotificationClient;
import ese.trab01.Tickets.dto.TicketReserveRequestDto;
import ese.trab01.Tickets.model.Ticket;
import ese.trab01.Tickets.model.enums.PaymentMethod;
import ese.trab01.Tickets.model.enums.TicketStatus;
import ese.trab01.Tickets.repository.TicketRepository;
import ese.trab01.Tickets.service.TicketService;
import ese.trab01.Tickets.commons.StatusEvento;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    TicketRepository ticketRepo;
    @Mock
    EventClient eventClient;
    @Mock
    NotificationClient notificationClient;

    @InjectMocks
    TicketService service;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "reserveTtlMinutes", 10);
    }

    // helper para criar EventInfo semelhante ao do EventClient
    private EventClient.EventInfo activeEvent(Integer capacidade) {
        var e = new EventClient.EventInfo();
        e.setId(1L);
        e.setCapacidade(capacidade);
        e.setStatus(StatusEvento.ATIVO);
        return e;
    }

    @Test
    void reserve_ShouldCreateTicket_WhenEventActiveAndHasCapacity() {
        when(eventClient.getEventById(1L)).thenReturn(activeEvent(10));
        when(ticketRepo.countByEventIdAndStatus(1L, TicketStatus.CONFIRMED)).thenReturn(3L);
        when(ticketRepo.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(123L);
            if (t.getCode() == null) t.setCode(UUID.randomUUID().toString());
            return t;
        });

        var req = new TicketReserveRequestDto();
        UUID participantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        req.setEventId(1L);
        req.setParticipantId(participantId);
        req.setMethod(PaymentMethod.PIX);

        Ticket t = service.reserve(req);

        assertNotNull(t.getId());
        assertEquals(TicketStatus.RESERVED, t.getStatus());
        assertEquals(participantId, t.getParticipantId());
        assertNotNull(t.getExpiresAt());
        verify(ticketRepo).save(any(Ticket.class));
    }

    @Test
    void reserve_ShouldThrow_WhenEventNotFound() {
        // caso seu client lance exception:
        when(eventClient.getEventById(99L)).thenThrow(new EntityNotFoundException("not found"));

        var req = new TicketReserveRequestDto();
        UUID participantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        req.setEventId(99L);
        req.setParticipantId(participantId);

        assertThrows(EntityNotFoundException.class, () -> service.reserve(req));
        verify(ticketRepo, never()).save(any());
    }

    @Test
    void reserve_ShouldThrow_WhenCapacityFull() {
        when(eventClient.getEventById(1L)).thenReturn(activeEvent(3));
        when(ticketRepo.countByEventIdAndStatus(1L, TicketStatus.CONFIRMED)).thenReturn(3L);

        var req = new TicketReserveRequestDto();
        UUID participantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        req.setEventId(1L);
        req.setParticipantId(participantId);

        assertThrows(IllegalStateException.class, () -> service.reserve(req));
        verify(ticketRepo, never()).save(any());
    }

    @Test
    void confirm_ShouldConfirmAndNotify_WhenValid() {
        UUID participantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        Ticket t = Ticket.builder()
                .id(5L)
                .eventId(1L)
                .participantId(participantId)
                .status(TicketStatus.RESERVED)
                .expiresAt(OffsetDateTime.now().plusMinutes(5))
                .build();
        when(ticketRepo.findById(5L)).thenReturn(Optional.of(t));

        service.confirm(5L);

        assertEquals(TicketStatus.CONFIRMED, t.getStatus());
        assertNotNull(t.getConfirmedAt());
        verify(ticketRepo).save(t);
        verify(notificationClient).sendPurchaseConfirmation(participantId, 1L, 5L);
    }

    @Test
    void confirm_ShouldSetExpired_WhenExpired() {
        Ticket t = Ticket.builder()
                .id(6L)
                .status(TicketStatus.RESERVED)
                .expiresAt(OffsetDateTime.now().minusMinutes(1))
                .build();
        when(ticketRepo.findById(6L)).thenReturn(Optional.of(t));

        assertThrows(IllegalStateException.class, () -> service.confirm(6L));
        assertEquals(TicketStatus.EXPIRED, t.getStatus());
        verify(ticketRepo).save(t);
        verify(notificationClient, never()).sendPurchaseConfirmation(any(), any(), any());
    }

    @Test
    void cancel_ShouldCancel_WhenReserved() {
        Ticket t = Ticket.builder()
                .id(7L)
                .status(TicketStatus.RESERVED)
                .build();
        when(ticketRepo.findById(7L)).thenReturn(Optional.of(t));

        service.cancel(7L);

        assertEquals(TicketStatus.CANCELED, t.getStatus());
        assertNotNull(t.getCanceledAt());
        verify(ticketRepo).save(t);
    }

    @Test
    void cancel_ShouldThrow_WhenAlreadyUsed() {
        Ticket t = Ticket.builder()
                .id(8L)
                .status(TicketStatus.USED)
                .build();
        when(ticketRepo.findById(8L)).thenReturn(Optional.of(t));

        assertThrows(IllegalStateException.class, () -> service.cancel(8L));
        verify(ticketRepo, never()).save(any());
    }

    @Test
    void validateUse_ShouldMarkUsed_WhenConfirmed() {
        Ticket t = Ticket.builder()
                .id(9L)
                .code("ABC")
                .status(TicketStatus.CONFIRMED)
                .build();
        when(ticketRepo.findByCode("ABC")).thenReturn(Optional.of(t));

        service.validateUse("ABC");

        assertEquals(TicketStatus.USED, t.getStatus());
        assertNotNull(t.getUsedAt());
        verify(ticketRepo).save(t);
    }

    @Test
    void validateUse_ShouldThrow_WhenNotConfirmed() {
        Ticket t = Ticket.builder()
                .id(10L)
                .code("ABC")
                .status(TicketStatus.RESERVED)
                .build();
        when(ticketRepo.findByCode("ABC")).thenReturn(Optional.of(t));

        assertThrows(IllegalStateException.class, () -> service.validateUse("ABC"));
        verify(ticketRepo, never()).save(any());
    }
}
