package ese.trab01.Tickets;

import com.fasterxml.jackson.databind.ObjectMapper;
import ese.trab01.Tickets.controller.TicketController;
import ese.trab01.Tickets.dto.TicketReserveRequestDto;
import ese.trab01.Tickets.dto.TicketReserveResponseDto;
import ese.trab01.Tickets.dto.TicketResponseDto;
import ese.trab01.Tickets.model.Ticket;
import ese.trab01.Tickets.model.enums.PaymentMethod;
import ese.trab01.Tickets.model.enums.TicketStatus;
import ese.trab01.Tickets.service.TicketService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

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

    @Test
    void reserve_ShouldReturn201() throws Exception {
        var req = new TicketReserveRequestDto();
        UUID participantId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        req.setEventId(1L);
        req.setParticipantId(participantId);
        req.setMethod(PaymentMethod.PIX);

        var saved = Ticket.builder()
                .id(123L)
                .code("123")
                .eventId(1L)
                .participantId(participantId)
                .status(TicketStatus.RESERVED)
                .expiresAt(OffsetDateTime.now().plusMinutes(15))
                .build();

        Mockito.when(service.reserve(any(TicketReserveRequestDto.class))).thenReturn(saved);

        mvc.perform(post("/tickets/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticketId").value(123L))
                .andExpect(jsonPath("$.code").value("123"))
                .andExpect(jsonPath("$.status").value("RESERVED"));
    }

    @Test
    void confirm_ShouldReturn204() throws Exception {
        mvc.perform(post("/tickets/{ticketId}/confirm", 5L)).andExpect(status().isNoContent());
        Mockito.verify(service).confirm(5L);
    }

    @Test
    void cancel_ShouldReturn204() throws Exception {
        long ticketId = 5L;

        mvc.perform(post("/tickets/{ticketId}/cancel", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                { "status": "CANCELED" }
            """)).andExpect(status().isNoContent());

        Mockito.verify(service).cancel(eq(ticketId), eq(TicketStatus.CANCELED));
    }

    @Test
    void validateUse_ShouldReturn204() throws Exception {
        Page<Ticket> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        Mockito.when(service.list(any(PageRequest.class))).thenReturn(page);

        mvc.perform(get("/tickets")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void listByParticipant_ShouldReturn200() throws Exception {
        UUID participant = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        Page<TicketResponseDto> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        Mockito.when(service.buscarTicketsDoParticipante(eq(participant), any(Pageable.class)))
                .thenReturn(page);

        mvc.perform(get("/tickets/buscar")
                        .param("page", "0")
                        .param("size", "10")
                        .header("X-User-Id", participant.toString())
                        .header("X-User-Roles", "CLIENTE"))
                .andExpect(status().isOk());
    }
}
