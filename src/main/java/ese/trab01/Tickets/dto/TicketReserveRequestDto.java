package ese.trab01.Tickets.dto;

import ese.trab01.Tickets.model.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class TicketReserveRequestDto {
    @NotNull
    @Positive
    private Long eventId;

    @NotNull
    @Positive
    private UUID participantId;


    private PaymentMethod method;
}
