package com.zerobase.order_drinks.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class NotificationService {

    private final EmitterRepositoryImpl emitterRepository;
    static String id;

    public SseEmitter subscribe(String email, String lastEventId) {

        String emitterId = makeTimeIncludeId(email);
        id = emitterId;

        SseEmitter emitter;

        //버그 방지용
        if (emitterRepository.findEmitterStartWithByEmail(email) != null){
            emitterRepository.deleteAllEmitterStartWithId(email);
            emitter = emitterRepository.save(emitterId, new SseEmitter(Long.MAX_VALUE));
        }
        else {
            emitter = emitterRepository.save(emitterId, new SseEmitter(Long.MAX_VALUE));
        }

        //오류 종류별 구독 취소 처리
        emitter.onCompletion(() -> emitterRepository.deleteById(emitterId)); //네트워크 오류
        emitter.onTimeout(() -> emitterRepository.deleteById(emitterId)); //시간 초과
        emitter.onError((e) -> emitterRepository.deleteById(emitterId)); //오류

        // 503 에러를 방지하기 위한 더미 이벤트 전송
        String eventId = makeTimeIncludeId(email);
        sendNotification(emitter, eventId, emitterId, "EventStream Created. [userId=" + email + "]");

        // 클라이언트가 미수신한 Event 목록이 존재할 경우 전송하여 Event 유실을 예방
        if (hasLostData(lastEventId)) {
            sendLostData(lastEventId, email, emitterId, emitter);
        }

        return emitter;
    }

    //단순 알림 전송
    private void sendNotification(SseEmitter emitter, String eventId, String emitterId, Object data) {

        try {
            emitter.send(SseEmitter.event()
                    .id(eventId)
                    .name("sse")
                    .data(data, MediaType.APPLICATION_JSON));
            //emitter.complete();
        } catch (IOException exception) {
            emitterRepository.deleteById(emitterId);
            emitter.completeWithError(exception);
        }
    }

    private String makeTimeIncludeId(String email) { return email + "_" + System.currentTimeMillis(); }//Last-Event-ID의 값을 이용하여 유실된 데이터를 찾는데 필요한 시점을 파악하기 위한 형태

    //Last-Event-Id의 존재 여부 boolean 값
    private boolean hasLostData(String lastEventId) {
        return !lastEventId.isEmpty();
    }

    //유실된 데이터 다시 전송
    private void sendLostData(String lastEventId, String email, String emitterId, SseEmitter emitter) {

        Map<String, Object> eventCaches = emitterRepository.findAllEventCacheStartWithByEmail(String.valueOf(email));
        eventCaches.entrySet().stream()
                .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                .forEach(entry -> sendNotification(emitter, entry.getKey(), emitterId, entry.getValue()));
    }

//    sse연결 요청 응답
    /*-----------------------------------------------------------------------------------------------------------------------------------*/
//    서버에서 클라이언트로 일방적인 데이터 보내기

    //특정 유저에게 알림 전송
    public void send(String receiver, String content, int urlValue) {

        Notification notification = createNotification(receiver, content, urlValue);

        // 로그인 한 유저의 SseEmitter 가져오기
        SseEmitter sseEmitters = emitterRepository.findEmitterStartWithByEmail(receiver);

        sendToClient(sseEmitters, id, notification);
        log.info("send alert receiver : " + receiver +" , urlValue : " + urlValue + ", eventId : " + id);
    }

    private Notification createNotification(String receiver, String content, int urlValue) {
        return Notification.builder()
                    .receiver(receiver)
                    .content(content)
                    .url("/order/" + urlValue)
                    .isRead(false)
                    .build();
    }

    private void sendToClient(SseEmitter emitter, String id, Object data) {
        log.info("id : "+ id);
        try {
            emitter.send(SseEmitter.event()
                    .id(id)
                    .name("info")
                    .data(data, MediaType.APPLICATION_JSON)
                    .reconnectTime(0));

        } catch (Exception exception) {
            emitterRepository.deleteById(id);
            emitter.completeWithError(exception);
        }
    }
}