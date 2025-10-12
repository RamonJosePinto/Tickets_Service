package ese.trab01.Tickets.dto;

import ese.trab01.Tickets.model.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReservaRequisicaoDto {

    @NotNull(message = "O ID do evento é obrigatório.")
    @Positive(message = "O ID do evento deve ser positivo.")
    private Long eventId;

    @NotNull(message = "A quantidade é obrigatória.")
    @Min(value = 1, message = "A quantidade mínima é 1.")
    private Integer quantity;

    @NotNull(message = "O método de pagamento é obrigatório.")
    private PaymentMethod method;

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "E-mail inválido.")
    private String email;
}
