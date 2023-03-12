package com.zerobase.order_drinks.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@Log4j2
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(HttpServletRequest request, @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId){
        String email = request.getUserPrincipal().getName();
        return notificationService.subscribe(email, lastEventId);
    }
}