package ese.trab01.Tickets;

import ese.trab01.Tickets.client.EventClient;
import ese.trab01.Tickets.client.EventClient.EventInfo;
import ese.trab01.Tickets.client.NotificationClient;
import ese.trab01.Tickets.commons.StatusEvento;
import ese.trab01.Tickets.exception.RecursoNaoEncontradoException;
import ese.trab01.Tickets.model.Reservation;
import ese.trab01.Tickets.model.Ticket;
import ese.trab01.Tickets.model.enums.PaymentMethod;
import ese.trab01.Tickets.model.enums.ReservationStatus;
import ese.trab01.Tickets.model.enums.TicketStatus;
import ese.trab01.Tickets.repository.ReservationRepository;
import ese.trab01.Tickets.repository.TicketRepository;
import ese.trab01.Tickets.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    ReservationRepository reservationRepo;
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
        // Define hold minutes para 10 (ou qualquer valor fixo) via Reflection
        ReflectionTestUtils.setField(service, "holdMinutes", 10);
    }

    private EventInfo activeEvent(Integer capacidade, Integer vagas) {
        EventInfo e = new EventInfo();
        e.setId(1L);
        e.setCapacidade(capacidade);
        e.setVagas(vagas);
        e.setStatus(StatusEvento.ATIVO);
        return e;
    }

    @Test
    void reserve_deveCriarReserva_quandoEventoAtivoComVaga() {
        when(eventClient.getEventById(1L)).thenReturn(activeEvent(10, 3));
        when(reservationRepo.sumActiveByEventAndStatus(1L, ReservationStatus.RESERVADO)).thenReturn(0L);
        when(reservationRepo.sumByEventAndStatus(1L, ReservationStatus.PAGO)).thenReturn(6L);
        when(reservationRepo.save(any())).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            r.setId(123L);
            return r;
        });

        Reservation r = service.reserve(1L, "ramon@example.com", 2, PaymentMethod.CARTAO);

        assertEquals(ReservationStatus.RESERVADO, r.getStatus()); // ID simulado no mock; em prod JPA preenche
        assertEquals(2, r.getQuantity());
        assertEquals(ReservationStatus.RESERVADO, r.getStatus());
        assertEquals(2, r.getQuantity());
        assertNotNull(r.getExpiresAt());
        verify(reservationRepo).save(any(Reservation.class));
    }

    @Test
    void reserve_deveLancarQuandoEventoNaoEncontrado() {
        when(eventClient.getEventById(99L))
                .thenThrow(new EntityNotFoundException("Evento não encontrado"));

        assertThrows(EntityNotFoundException.class,
                () -> service.reserve(99L, "x@x", 1, PaymentMethod.PIX));

        verify(reservationRepo, never()).save(any());
    }


    @Test
    void reserve_deveLancarQuandoEventoInativo() {
        EventInfo e = activeEvent(10, 10);
        e.setStatus(StatusEvento.CANCELADO);
        when(eventClient.getEventById(1L)).thenReturn(e);
        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.reserve(1L, "x@x", 1, PaymentMethod.PIX));
    }

    @Test
    void reserve_deveLancarQuandoSemCapacidade() {
        when(eventClient.getEventById(1L)).thenReturn(activeEvent(10, 0));
        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.reserve(1L, "x@x", 1, PaymentMethod.PIX));
    }

    @Test
    void confirmPayment_deveGerarIngressosENotificar_quandoReservaValida() {
        Reservation res = Reservation.builder()
                .id(5L).eventId(1L).purchaserEmail("ramon@example.com")
                .quantity(3).status(ReservationStatus.RESERVADO)
                .expiresAt(OffsetDateTime.now().plusMinutes(5))
                .build();
        when(reservationRepo.findById(5L)).thenReturn(Optional.of(res));

        service.confirmPayment(5L, "PAY-123");

        assertEquals(ReservationStatus.PAGO, res.getStatus());
        assertEquals("PAY-123", res.getPaymentId());

        // Verifica que 3 tickets foram persistidos
        ArgumentCaptor<Ticket> cap = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepo, times(3)).save(cap.capture());
        assertTrue(cap.getAllValues().stream().allMatch(t -> t.getStatus() == TicketStatus.EMITIDO));

        // Verifica notificação
        verify(notificationClient).sendPurchaseConfirmation("ramon@example.com", 1L, 5L, 3);
    }

    @Test
    void confirmPayment_naoFazNada_seReservaNaoEstaReservada() {
        Reservation res = Reservation.builder()
                .id(7L).status(ReservationStatus.CANCELADO)
                .expiresAt(OffsetDateTime.now().plusMinutes(5))
                .build();
        when(reservationRepo.findById(7L)).thenReturn(Optional.of(res));

        service.confirmPayment(7L, "PAY-XYZ");
        verify(ticketRepo, never()).save(any());
        verify(notificationClient, never()).sendPurchaseConfirmation(any(), any(), any(), any());
    }

    @Test
    void confirmPayment_expiraReservaSePassouDoPrazo() {
        Reservation res = Reservation.builder()
                .id(8L).status(ReservationStatus.RESERVADO)
                .expiresAt(OffsetDateTime.now().minusMinutes(1))
                .build();
        when(reservationRepo.findById(8L)).thenReturn(Optional.of(res));

        service.confirmPayment(8L, "PAY-LATE");
        assertEquals(ReservationStatus.EXPIRADO, res.getStatus());
        verify(ticketRepo, never()).save(any());
    }

    @Test
    void cancel_deveCancelarQuandoNaoPago() {
        Reservation res = Reservation.builder().id(9L)
                .status(ReservationStatus.RESERVADO)
                .build();
        when(reservationRepo.findById(9L)).thenReturn(Optional.of(res));

        service.cancel(9L);
        assertEquals(ReservationStatus.CANCELADO, res.getStatus());
    }

    @Test
    void cancel_deveFalharSePago() {
        Reservation res = Reservation.builder().id(10L)
                .status(ReservationStatus.PAGO).build();
        when(reservationRepo.findById(10L)).thenReturn(Optional.of(res));

        assertThrows(RecursoNaoEncontradoException.class, () -> service.cancel(10L));
    }

    @Test
    void validate_deveMarcarComoValidado() {
        Ticket t = new Ticket();
        t.setId(1L);
        t.setStatus(TicketStatus.EMITIDO);
        when(ticketRepo.findByCode("ABC")).thenReturn(Optional.of(t));

        Ticket result = service.validate("ABC");
        assertEquals(TicketStatus.VALIDADO, result.getStatus());
    }

    @Test
    void validate_deveFalharSeJaValidado() {
        Ticket t = new Ticket();
        t.setId(1L);
        t.setStatus(TicketStatus.VALIDADO);
        when(ticketRepo.findByCode("ABC")).thenReturn(Optional.of(t));

        assertThrows(RecursoNaoEncontradoException.class, () -> service.validate("ABC"));
    }
}