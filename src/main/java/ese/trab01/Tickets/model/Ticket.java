package ese.trab01.Tickets.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long eventId;

    @NotBlank
    @Column(unique = true, length = 64)
    private String code; // código do ingresso (ex: "ABC123XYZ")

    @NotBlank
    @Column(length = 32)
    private String status; // ex: "EMITIDO", "VALIDADO", "CANCELADO"

    @Email
    @NotBlank
    private String purchaserEmail;

    @NotNull
    private OffsetDateTime purchasedAt;
}
