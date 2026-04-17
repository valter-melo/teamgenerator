package com.boraver.teamgenerator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class SseService {

    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String championshipId) {
        SseEmitter emitter = new SseEmitter(0L);

        emitters
                .computeIfAbsent(championshipId, key -> new CopyOnWriteArrayList<>())
                .add(emitter);

        log.info("Novo subscriber SSE para campeonato {} | total: {}",
                championshipId,
                emitters.get(championshipId).size());

        emitter.onCompletion(() -> {
            log.info("SSE completion: {}", championshipId);
            removeEmitter(championshipId, emitter);
        });

        emitter.onTimeout(() -> {
            log.warn("SSE timeout: {}", championshipId);
            removeEmitter(championshipId, emitter);
            safeComplete(emitter);
        });

        emitter.onError((ex) -> {
            log.warn("SSE error: {} - {}", championshipId, ex.getMessage());
            removeEmitter(championshipId, emitter);
            safeComplete(emitter);
        });

        try {
            emitter.send(
                    SseEmitter.event()
                            .name("connected")
                            .data("SSE conectado com sucesso")
            );
        } catch (IOException e) {
            log.warn("Erro ao enviar evento inicial SSE para campeonato {}: {}", championshipId, e.getMessage());
            removeEmitter(championshipId, emitter);
            safeComplete(emitter);
        }

        return emitter;
    }

    public void sendMatchUpdate(String championshipId, Object data) {
        List<SseEmitter> championshipEmitters = emitters.get(championshipId);

        log.info("Enviando SSE para campeonato {} | emitters: {}",
                championshipId,
                championshipEmitters == null ? 0 : championshipEmitters.size());

        if (championshipEmitters == null || championshipEmitters.isEmpty()) {
            return;
        }

        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : championshipEmitters) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name("matchUpdate")
                                .data(data, MediaType.APPLICATION_JSON)
                );
            } catch (Exception e) {
                log.warn("Erro ao enviar SSE para campeonato {}: {}", championshipId, e.getMessage());
                deadEmitters.add(emitter);
                safeComplete(emitter);
            }
        }

        championshipEmitters.removeAll(deadEmitters);

        if (championshipEmitters.isEmpty()) {
            emitters.remove(championshipId);
        }
    }

    public void sendEvent(String championshipId, String eventName, Object data) {
        List<SseEmitter> championshipEmitters = emitters.get(championshipId);

        log.info("Enviando evento SSE [{}] para campeonato {} | emitters: {}",
                eventName,
                championshipId,
                championshipEmitters == null ? 0 : championshipEmitters.size());

        if (championshipEmitters == null || championshipEmitters.isEmpty()) {
            return;
        }

        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : championshipEmitters) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name(eventName)
                                .data(data, MediaType.APPLICATION_JSON)
                );
            } catch (Exception e) {
                log.warn("Erro ao enviar evento SSE [{}] para campeonato {}: {}",
                        eventName, championshipId, e.getMessage());
                deadEmitters.add(emitter);
                safeComplete(emitter);
            }
        }

        championshipEmitters.removeAll(deadEmitters);

        if (championshipEmitters.isEmpty()) {
            emitters.remove(championshipId);
        }
    }

    private void removeEmitter(String championshipId, SseEmitter emitter) {
        List<SseEmitter> championshipEmitters = emitters.get(championshipId);

        if (championshipEmitters != null) {
            championshipEmitters.remove(emitter);

            if (championshipEmitters.isEmpty()) {
                emitters.remove(championshipId);
            }
        }
    }

    private void safeComplete(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception ignored) {
        }
    }
}