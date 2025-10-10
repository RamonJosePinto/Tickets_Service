package ese.trab01.Tickets.model;

import ese.trab01.Tickets.model.enums.TicketStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tickets", indexes = {
        @Index(name = "idx_ticket_event", columnList = "eventId"),
        @Index(name = "idx_ticket_purchaser", columnList = "purchaserEmail")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Long reservationId;

    @Email
    @NotBlank
    private String purchaserEmail;

    @Column(unique = true, length = 64, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private TicketStatus status;

    @NotNull
    private OffsetDateTime purchasedAt;
}
