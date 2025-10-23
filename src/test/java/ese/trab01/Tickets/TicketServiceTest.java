package ese.trab01.Tickets;

import ese.trab01.Tickets.client.EventClient;
import ese.trab01.Tickets.client.NotificationClient;
import ese.trab01.Tickets.commons.StatusEvento;
import ese.trab01.Tickets.dto.TicketReserveRequestDto;
import ese.trab01.Tickets.model.Ticket;
import ese.trab01.Tickets.model.enums.PaymentMethod;
import ese.trab01.Tickets.model.enums.TicketStatus;
import ese.trab01.Tickets.repository.TicketRepository;
import ese.trab01.Tickets.service.TicketService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
        // no service novo, o TTL veio como reserveTtlMinutes
        ReflectionTestUtils.setField(service, "reserveTtlMinutes", 10);
    }

    private EventClient.EventInfo activeEvent(Integer capacidade) {
        var e = new EventClient.EventInfo();
        e.setId(1L);
        e.setCapacidade(capacidade);
        e.setStatus(StatusEvento.ATIVO);
        return e;
    }

    @Test
    void reserve_deveCriarTicket_quandoEventoAtivoEComCapacidade() {
        when(eventClient.getEventById(1L)).thenReturn(activeEvent(10));
        when(ticketRepo.countByEventIdAndStatus(1L, TicketStatus.CONFIRMED)).thenReturn(3L);
        when(ticketRepo.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(123L);
            if (t.getCode() == null) t.setCode(UUID.randomUUID().toString());
            return t;
        });

        var req = new TicketReserveRequestDto();
        req.setEventId(1L);
        req.setEmail("ramon@example.com");
        req.setMethod(PaymentMethod.PIX);

        Ticket t = service.reserve(req);

        assertNotNull(t.getId());
        assertEquals(TicketStatus.RESERVED, t.getStatus());
        assertNotNull(t.getExpiresAt());
        verify(ticketRepo).save(any(Ticket.class));
    }

    @Test
    void reserve_deveLancarQuandoEventoNaoEncontrado() {
        when(eventClient.getEventById(99L)).thenReturn(null);

        var req = new TicketReserveRequestDto();
        req.setEventId(99L);
        req.setEmail("x@x");
        assertThrows(EntityNotFoundException.class, () -> service.reserve(req));

        verify(ticketRepo, never()).save(any());
    }

    @Test
    void reserve_deveLancarQuandoCapacidadeEsgotada() {
        when(eventClient.getEventById(1L)).thenReturn(activeEvent(3));
        when(ticketRepo.countByEventIdAndStatus(1L, TicketStatus.CONFIRMED)).thenReturn(3L);

        var req = new TicketReserveRequestDto();
        req.setEventId(1L);
        req.setEmail("x@x");
        assertThrows(IllegalStateException.class, () -> service.reserve(req));
    }

    @Test
    void confirm_deveConfirmarENotificar_quandoValido() {
        Ticket t = Ticket.builder()
                .id(5L)
                .eventId(1L)
                .email("ramon@example.com")
                .status(TicketStatus.RESERVED)
                .expiresAt(OffsetDateTime.now().plusMinutes(5))
                .build();

        when(ticketRepo.findById(5L)).thenReturn(Optional.of(t));

        service.confirm(5L);

        assertEquals(TicketStatus.CONFIRMED, t.getStatus());
        assertNotNull(t.getConfirmedAt());
        verify(ticketRepo).save(t);
        verify(notificationClient).sendPurchaseConfirmation("ramon@example.com", 1L, 5L, 1);
    }

    @Test
    void confirm_deveLancarQuandoReservaExpirada() {
        Ticket t = Ticket.builder()
                .id(6L)
                .status(TicketStatus.RESERVED)
                .expiresAt(OffsetDateTime.now().minusMinutes(1))
                .build();

        when(ticketRepo.findById(6L)).thenReturn(Optional.of(t));
        assertThrows(IllegalStateException.class, () -> service.confirm(6L));

        assertEquals(TicketStatus.EXPIRED, t.getStatus());
        verify(ticketRepo).save(t);
        verify(notificationClient, never()).sendPurchaseConfirmation(any(), any(), any(), any());
    }

    @Test
    void cancel_deveCancelar_quandoNaoUsado() {
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
    void cancel_deveFalharSeJaUsado() {
        Ticket t = Ticket.builder()
                .id(8L)
                .status(TicketStatus.USED)
                .build();
        when(ticketRepo.findById(8L)).thenReturn(Optional.of(t));

        assertThrows(IllegalStateException.class, () -> service.cancel(8L));
        verify(ticketRepo, never()).save(any());
    }

    @Test
    void validateUse_deveMarcarComoUSED_quandoConfirmado() {
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
    void validateUse_deveFalharSeNaoConfirmado() {
        Ticket t = Ticket.builder()
                .id(10L)
                .code("DEF")
                .status(TicketStatus.RESERVED)
                .build();
        when(ticketRepo.findByCode("DEF")).thenReturn(Optional.of(t));

        assertThrows(IllegalStateException.class, () -> service.validateUse("DEF"));
        verify(ticketRepo, never()).save(any());
    }
}
