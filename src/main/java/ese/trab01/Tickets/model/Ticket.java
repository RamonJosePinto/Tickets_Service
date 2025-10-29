package ese.trab01.Tickets.model;

import ese.trab01.Tickets.model.enums.PaymentMethod;
import ese.trab01.Tickets.model.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tickets")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String code;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private UUID participantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime expiresAt;

    private OffsetDateTime confirmedAt;
    private OffsetDateTime canceledAt;
    private OffsetDateTime usedAt;
}
