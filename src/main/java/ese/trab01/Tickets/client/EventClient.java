package ese.trab01.Tickets.client;

import ese.trab01.Tickets.commons.StatusEvento;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Component
public class EventClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public EventClient(@Value("${services.events.base-url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    public EventInfo getEventById(Long eventId) {
        String url = baseUrl + "/eventos/" + eventId;
        return restTemplate.getForObject(url, EventInfo.class);
    }

    // DTO que reflete o EventoRespostaDto do serviço de Eventos
    @Getter
    @Setter
    public static class EventInfo {
        private Long id;
        private String nome;
        private String descricao;
        private String localizacao;
        private LocalDateTime data;
        private Integer capacidade;
        private Integer vagas;
        private StatusEvento status;
        private Long organizerId;

    }
}
