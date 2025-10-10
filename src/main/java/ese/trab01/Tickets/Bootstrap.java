package ese.trab01.Tickets;

import ese.trab01.Tickets.model.enums.PaymentMethod;
import ese.trab01.Tickets.service.TicketService;
import ese.trab01.Tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile("stub")              // só roda no profile stub
@Order(1)
@RequiredArgsConstructor
public class Bootstrap implements ApplicationRunner {

    private final TicketService ticketService;
    private final TicketRepository ticketRepository;

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {
        if (ticketRepository.count() > 0) return; // já tem dado, não semeia

        // 1) cria reserva (capacidade vem do EventClientStub: 5)
        var res = ticketService.reserve(
                1001L,
                "ramon@example.com",
                2,
                PaymentMethod.PIX
        );

        // 2) simula webhook de pagamento aprovado -> gera 2 tickets
        ticketService.confirmPayment(res.getId(), "seed_pay_001");
    }
}
