package ese.trab01.Tickets.dto;

import ese.trab01.Tickets.model.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class TicketReserveResponseDto {
    private Long ticketId;
    private String code;
    private TicketStatus status;
    private OffsetDateTime expiresAt;
}
