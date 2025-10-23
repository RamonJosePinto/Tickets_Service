package ese.trab01.Tickets.dto;

import ese.trab01.Tickets.model.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class TicketResponseDto {
    private Long id;
    private String code;
    private Long eventId;
    private String email;
    private TicketStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime confirmedAt;
    private OffsetDateTime canceledAt;
    private OffsetDateTime usedAt;
}
