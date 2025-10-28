package ese.trab01.Tickets.dto;

import ese.trab01.Tickets.model.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketCancelRequestDto {
    @NotNull
    private TicketStatus status;
}

