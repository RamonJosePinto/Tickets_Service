package ese.trab01.Tickets.dto;

import ese.trab01.Tickets.model.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TicketReserveRequestDto {
    @NotNull
    @Positive
    private Long eventId;

    @NotNull
    @Positive
    private Long participantId;


    private PaymentMethod method;
}
