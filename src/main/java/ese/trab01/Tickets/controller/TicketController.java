package ese.trab01.Tickets.controller;


import ese.trab01.Tickets.model.Ticket;
import ese.trab01.Tickets.repository.TicketRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketRepository repo;

    public TicketController(TicketRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> findAll() {
        List<Ticket> tickets = repo.findAll();
        return ResponseEntity.ok().body(tickets);
    }
}