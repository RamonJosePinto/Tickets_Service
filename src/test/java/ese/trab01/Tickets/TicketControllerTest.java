package ese.trab01.Tickets;

import com.fasterxml.jackson.databind.ObjectMapper;
import ese.trab01.Tickets.controller.TicketController;
import ese.trab01.Tickets.dto.ReservaRequisicaoDto;
import ese.trab01.Tickets.dto.ReservaRespostaDto;
import ese.trab01.Tickets.model.Reservation;
import ese.trab01.Tickets.model.Ticket;
import ese.trab01.Tickets.model.enums.PaymentMethod;
import ese.trab01.Tickets.model.enums.ReservationStatus;
import ese.trab01.Tickets.repository.ReservationRepository;
import ese.trab01.Tickets.service.TicketService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
@AutoConfigureMockMvc(addFilters = false)
class TicketControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper om;

    @MockitoBean
    private TicketService service;
    @MockitoBean
    private ReservationRepository reservationRepo;

    @Test
    void reserve_endpoint_deveRetornar200() throws Exception {
        var req = new ReservaRequisicaoDto();
        req.setEventId(1L);
        req.setEmail("ramon@example.com");
        req.setQuantity(2);
        req.setMethod(PaymentMethod.PIX);

        Mockito.when(service.reserve(eq(1L), anyString(), eq(2), eq(PaymentMethod.PIX)))
                .thenReturn(new Reservation());

        mvc.perform(post("/tickets/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk());          // ← era isCreated()
    }

    @Test
    void confirm_endpoint_deveRetornar202() throws Exception {
        mvc.perform(post("/tickets/reservations/5/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentId\":\"PAY-123\"}"))
                .andExpect(status().isAccepted());     // ← era isNoContent()
    }

    @Test
    void cancel_endpoint_deveRetornar204() throws Exception {
        mvc.perform(post("/tickets/reservations/5/cancel"))
                .andExpect(status().isNoContent());
    }

    @Test
    void my_endpoint_deveRetornar200() throws Exception {
        Page<Ticket> page = new PageImpl<>(List.of());
        Mockito.when(service.listMyTickets(eq("ramon@example.com"), any(PageRequest.class)))
                .thenReturn(page);

        mvc.perform(get("/tickets/my")
                        .param("email", "ramon@example.com")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void validate_endpoint_deveRetornar200() throws Exception {
        Mockito.when(service.validate("CODE-1")).thenReturn(new Ticket());
        mvc.perform(post("/tickets/validate/CODE-1"))
                .andExpect(status().isOk());
    }

    @Test
    void getReservation_endpoint_deveRetornar200() throws Exception {
        Mockito.when(reservationRepo.findById(1L)).thenReturn(Optional.of(new Reservation()));
        mvc.perform(get("/tickets/reservations/1"))
                .andExpect(status().isOk());
    }
}
