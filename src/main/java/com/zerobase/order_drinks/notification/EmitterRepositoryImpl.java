package com.zerobase.order_drinks.notification;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
@NoArgsConstructor
@Log4j2
public class EmitterRepositoryImpl implements EmitterRepository {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, Object> eventCache = new ConcurrentHashMap<>();

    @Override
    public SseEmitter save(String emitterId, SseEmitter sseEmitter) {
        emitters.put(emitterId, sseEmitter);
        log.info(emitters);
        return sseEmitter;
    }

    @Override
    public void saveEventCache(String eventCacheId, Object event) {
        eventCache.put(eventCacheId, event);
    }

    @Override
    public SseEmitter findEmitterStartWithByEmail(String email) {
        for(Map.Entry<String, SseEmitter> map : emitters.entrySet()){
            if(map.getKey().startsWith(email)){
                return map.getValue();
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> findAllEventCacheStartWithByEmail(String email) {
        return emitters.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(email))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void deleteById(String id) {
        emitters.remove(id);
    }

    @Override
    public void deleteAllEmitterStartWithId(String email) {
        emitters.forEach((key, emitter) -> {
            if (key.startsWith(email)){
                emitters.remove(key);
            }
        });
    }

    @Override
    public void deleteAllEventCacheStartWithId(String email) {
        emitters.forEach((key, emitter) -> {
            if (key.startsWith(email)){
                emitters.remove(key);
            }
        });
    }
}