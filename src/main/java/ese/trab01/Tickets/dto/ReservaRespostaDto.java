package ese.trab01.Tickets.dto;

import ese.trab01.Tickets.model.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservaRespostaDto {
    private Long reservationId;
    private ReservationStatus status;
    private OffsetDateTime expiresAt;
}
