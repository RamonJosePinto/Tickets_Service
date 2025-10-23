package ese.trab01.Tickets.model.enums;

public enum TicketStatus {
    RESERVED,   // criado/segurado até expiresAt
    CONFIRMED,  // pago/confirmado
    CANCELED,   // cancelado pelo usuário/sistema
    EXPIRED,    // reserva expirou (job que varre e expira)
    USED        // validado na entrada do evento
}
