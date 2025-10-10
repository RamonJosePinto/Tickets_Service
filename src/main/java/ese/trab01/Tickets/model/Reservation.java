package ese.trab01.Tickets.model;

import ese.trab01.Tickets.model.enums.PaymentMethod;
import ese.trab01.Tickets.model.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    // se já tiver Auth/JWT, guarde userId; por ora mantenho email:
    @Column(nullable = false)
    private String purchaserEmail;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReservationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentMethod method;

    private String paymentId; // id do pagamento no serviço de pagamentos (opcional)

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime expiresAt;
}
