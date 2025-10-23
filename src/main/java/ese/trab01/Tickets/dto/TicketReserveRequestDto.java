package ese.trab01.Tickets.dto;

import ese.trab01.Tickets.model.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TicketReserveRequestDto {
    @NotNull
    @Positive
    private Long eventId;

    @NotBlank
    @Email
    private String email;

    // opcional: se já souber o método ao reservar
    private PaymentMethod method;
}
