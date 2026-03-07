package de.tebrox.communitybot.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.io.IOException;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static boolean isSseRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return (accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE)) || request.getRequestURI().startsWith("/api/logs/stream");
    }

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleAsyncNotUsable(AsyncRequestNotUsableException e) {
        // Passiert bei SSE/Async wenn Client weg ist; kein echter Serverfehler
        // optional: log.debug("[WebHandler] Async response not usable (client disconnected): {}", e.getMessage());
    }

    @ExceptionHandler(IOException.class)
    public void handleIo(IOException e) throws IOException {
        String msg = String.valueOf(e.getMessage()).toLowerCase();
        if (msg.contains("broken pipe") || msg.contains("connection reset")) {
            // Normaler Client-Abbruch (Reload/Tab zu)
            // optional: log.debug("[WebHandler] Client disconnected: {}", e.getMessage());
            return;
        }
        // Andere IOExceptions nicht schlucken
        throw e;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("[WebHandler] IllegalArgument: {}", e.getMessage());
        if(isSseRequest(request)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public Object handleGeneral(Exception e, Model model, HttpServletRequest request) {
        log.error("[WebHandler] Unexpected error: {}", e.getMessage());

        if(isSseRequest(request)) {
            return ResponseEntity.status(500).build();
        }

        model.addAttribute("error", "Ein unerwarteter Fehler ist aufgetreten.");
        return "error";
    }
}
