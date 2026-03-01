package de.tebrox.communitybot.web.controller;

import de.tebrox.communitybot.service.LogBuffer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Controller
public class LogController {

    private final LogBuffer logBuffer;
    private final ScheduledExecutorService scheduler;

    public LogController(LogBuffer logBuffer, ScheduledExecutorService scheduler) {
        this.logBuffer = logBuffer;
        this.scheduler = scheduler;
    }

    @GetMapping("/logs")
    public String logsPage(Model model) {
        model.addAttribute("logs", logBuffer.getAll());
        return "logs";
    }

    /**
     * Server-Sent Events endpoint for live log streaming.
     * Polls every 2 seconds and sends new entries since the last count.
     */
    @GetMapping(value = "/api/logs/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public SseEmitter streamLogs(@RequestParam(defaultValue = "0") int since) {
        SseEmitter emitter = new SseEmitter(60_000L);

        int[] offset = {since};

        var future = scheduler.scheduleAtFixedRate(() -> {
            try {
                List<LogBuffer.LogEntry> newEntries = logBuffer.getSince(offset[0]);
                if (!newEntries.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (LogBuffer.LogEntry entry : newEntries) {
                        String cssClass = switch (entry.level()) {
                            case "WARN"  -> "log-warn";
                            case "ERROR" -> "log-error";
                            default      -> "log-info";
                        };
                        sb.append("<div class=\"log-entry ").append(cssClass).append("\">")
                          .append("<span class=\"log-ts\">").append(entry.timestamp()).append("</span>")
                          .append(" <span class=\"log-level\">").append(entry.level()).append("</span>")
                          .append(" ").append(escapeHtml(entry.message()))
                          .append("</div>");
                    }
                    offset[0] += newEntries.size();
                    emitter.send(SseEmitter.event().data(sb.toString()));
                }
            } catch (IOException e) {
                emitter.complete();
            }
        }, 0, 2, TimeUnit.SECONDS);

        emitter.onCompletion(() -> future.cancel(true));
        emitter.onTimeout(() -> future.cancel(true));
        emitter.onError(e -> future.cancel(true));

        return emitter;
    }

    @GetMapping("/api/logs/count")
    @ResponseBody
    public int getCount() {
        return logBuffer.size();
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
