package com.example.Kaizer_Back.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AutoPingerService {

    private static final Logger log = LoggerFactory.getLogger(AutoPingerService.class);
    private final RestClient restClient;
    private final String appUrl;

    public AutoPingerService(@Value("${app.render.url:}") String appUrl) {
        this.appUrl = appUrl;
        this.restClient = RestClient.builder().build();
    }

    @Scheduled(fixedRate = 600000, initialDelay = 60000)
    public void pingSelf() {
        if (appUrl == null || appUrl.isBlank()) {
            log.warn("AutoPinger desactivado: 'app.render.url' no está configurada.");
            return;
        }

        try {
            String targetUrl = appUrl.endsWith("/") ? appUrl + "api/health" : appUrl + "/api/health";
            log.info("Enviando auto-ping para mantener activo el servidor: {}", targetUrl);

            restClient.get()
                    .uri(targetUrl)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Auto-ping completado con éxito.");
        } catch (Exception e) {
            log.error("Error al enviar el auto-ping: {}", e.getMessage());
        }
    }
}