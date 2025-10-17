package ese.trab01.Tickets.client;

public interface NotificationClient {
    void sendPurchaseConfirmation(String recipientEmail, Long eventId, Long reservationId, Integer quantity);
}
