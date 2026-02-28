package de.tebrox.rolesbot.web.controller;

import de.tebrox.rolesbot.util.SnowflakeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("[WebHandler] IllegalArgument: {}", e.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception e, Model model) {
        log.error("[WebHandler] Unexpected error: {}", e.getMessage());
        model.addAttribute("error", "Ein unerwarteter Fehler ist aufgetreten.");
        return "error";
    }
}
