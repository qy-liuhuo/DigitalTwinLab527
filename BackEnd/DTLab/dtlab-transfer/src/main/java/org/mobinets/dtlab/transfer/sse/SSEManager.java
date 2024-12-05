package org.mobinets.dtlab.transfer.sse;

import org.mobinets.dtlab.common.exception.SSEException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class SSEManager {
    private static Map<String, SseEmitter> cache = new ConcurrentHashMap<>();

    public boolean exist(String clientId) {
        return cache.containsKey(clientId);
    }

    public void addConnection(String clientId, SseEmitter emitter) {
        SseEmitter oldEmitter = cache.get(clientId);
        if (oldEmitter != null){
            oldEmitter.completeWithError(new SSEException("Duplicate connections for id:" + clientId));
        }
        cache.put(clientId, emitter);
    }

    public Boolean removeConnection(String clientId) {
        SseEmitter emitter = cache.remove(clientId);
        if (emitter != null){
            emitter.complete();
            return true;
        }
        return false;
    }

    public SseEmitter getEmitter(String clientId) {
        return cache.get(clientId);
    }

    public Iterable<SseEmitter> getAllClients() {
        return cache.values();
    }
}
