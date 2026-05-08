package com.boraver.teamgenerator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class SseService {

    private static final long SSE_TIMEOUT = 0L;
    private static final long HEARTBEAT_INTERVAL_MS = 15000L;

    /**
     * championshipId -> clientId -> connection
     */
    private final Map<String, Map<String, ClientConnection>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String championshipId) {
        String clientId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        ClientConnection connection = new ClientConnection(clientId, championshipId, emitter);

        emitters
                .computeIfAbsent(championshipId, key -> new ConcurrentHashMap<>())
                .put(clientId, connection);

        log.info("Novo subscriber SSE | campeonato: {} | clientId: {} | total: {}",
                championshipId,
                clientId,
                emitters.get(championshipId).size());

        emitter.onCompletion(() -> {
            log.info("SSE completion | campeonato: {} | clientId: {}", championshipId, clientId);
            removeConnection(championshipId, clientId);
        });

        emitter.onTimeout(() -> {
            log.warn("SSE timeout | campeonato: {} | clientId: {}", championshipId, clientId);
            removeConnection(championshipId, clientId);
        });

        emitter.onError((ex) -> {
            log.warn("SSE error | campeonato: {} | clientId: {} | erro: {}",
                    championshipId,
                    clientId,
                    ex == null ? "desconhecido" : ex.getMessage());
            removeConnection(championshipId, clientId);
        });

        try {
            sendInternal(connection,
                    SseEmitter.event()
                            .name("connected")
                            .id(clientId)
                            .reconnectTime(3000)
                            .data("SSE conectado com sucesso"));
        } catch (Exception e) {
            log.warn("Erro ao enviar evento inicial | campeonato: {} | clientId: {} | erro: {}",
                    championshipId, clientId, e.getMessage());
            removeConnection(championshipId, clientId);
        }

        return emitter;
    }

    public void sendMatchUpdate(String championshipId, Object data) {
        sendEvent(championshipId, "matchUpdate", data);
    }

    public void sendEvent(String championshipId, String eventName, Object data) {
        Map<String, ClientConnection> connections = emitters.get(championshipId);

        log.info("Enviando SSE [{}] | campeonato: {} | conexões: {}",
                eventName,
                championshipId,
                connections == null ? 0 : connections.size());

        if (connections == null || connections.isEmpty()) {
            return;
        }

        for (Map.Entry<String, ClientConnection> entry : connections.entrySet()) {
            String clientId = entry.getKey();
            ClientConnection connection = entry.getValue();

            if (!connection.active.get()) {
                removeConnection(championshipId, clientId);
                continue;
            }

            try {
                sendInternal(connection,
                        SseEmitter.event()
                                .name(eventName)
                                .data(data, MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                log.warn("Erro ao enviar SSE [{}] | campeonato: {} | clientId: {} | erro: {}",
                        eventName, championshipId, clientId, e.getMessage());

                // 🔥 IMPORTANTE: NÃO chamar complete()
                removeConnection(championshipId, clientId);
            }
        }
    }

    /**
     * Heartbeat para manter conexão viva e detectar disconnect
     */
    @Scheduled(fixedDelay = HEARTBEAT_INTERVAL_MS)
    public void heartbeat() {
        for (Map.Entry<String, Map<String, ClientConnection>> championshipEntry : emitters.entrySet()) {
            String championshipId = championshipEntry.getKey();
            Map<String, ClientConnection> connections = championshipEntry.getValue();

            for (Map.Entry<String, ClientConnection> clientEntry : connections.entrySet()) {
                String clientId = clientEntry.getKey();
                ClientConnection connection = clientEntry.getValue();

                if (!connection.active.get()) {
                    removeConnection(championshipId, clientId);
                    continue;
                }

                try {
                    sendInternal(connection, SseEmitter.event().comment("heartbeat"));
                } catch (Exception e) {
                    log.warn("Heartbeat falhou | campeonato: {} | clientId: {} | erro: {}",
                            championshipId, clientId, e.getMessage());

                    // 🔥 IMPORTANTE: só remover, não completar
                    removeConnection(championshipId, clientId);
                }
            }
        }
    }

    private void sendInternal(ClientConnection connection, SseEmitter.SseEventBuilder event) throws IOException {
        if (!connection.active.get()) {
            throw new IOException("Emitter inativo");
        }

        connection.sendLock.lock();
        try {
            if (!connection.active.get()) {
                throw new IOException("Emitter inativo");
            }

            connection.emitter.send(event);
        } finally {
            connection.sendLock.unlock();
        }
    }

    private void removeConnection(String championshipId, String clientId) {
        Map<String, ClientConnection> connections = emitters.get(championshipId);

        if (connections == null) {
            return;
        }

        ClientConnection connection = connections.remove(clientId);

        if (connection != null) {
            connection.active.set(false);
        }

        if (connections.isEmpty()) {
            emitters.remove(championshipId);
        }
    }

    private static class ClientConnection {
        private final String clientId;
        private final String championshipId;
        private final SseEmitter emitter;
        private final AtomicBoolean active = new AtomicBoolean(true);
        private final ReentrantLock sendLock = new ReentrantLock();

        private ClientConnection(String clientId, String championshipId, SseEmitter emitter) {
            this.clientId = clientId;
            this.championshipId = championshipId;
            this.emitter = emitter;
        }
    }
}