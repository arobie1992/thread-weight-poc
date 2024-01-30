import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    private static final boolean RUN_SERVER = false;
    private static final boolean USE_VIRTUAL = true;

    private static final int PORT = 10_000;
    private static final AtomicInteger activeThreads = new AtomicInteger();

    public static void main(String[] args) throws IOException {
        if(RUN_SERVER) {
            runServer();
        } else {
            loadTest();
        }
    }

    private static void loadTest() {
        for(int i = 0; i < 1_000_000; i++) {
            var tb = USE_VIRTUAL ? Thread.ofVirtual() : Thread.ofPlatform();
            tb.start(() -> handle(() -> {}));
        }
    }

    private static void runServer() throws IOException {
        try(var server = new ServerSocket(PORT)) {
            System.out.println("Starting server");
            while (true) {
                var s = server.accept();
                var tb = USE_VIRTUAL ? Thread.ofVirtual() : Thread.ofPlatform();
                Runnable respHandler = () -> {
                    try (var os = s.getOutputStream()) {
                        var resp = new HttpResp("test");
                        os.write(resp.serialize());
                        os.flush();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                };
                tb.start(() -> handle(respHandler));
            }
        }
    }

    private static void handle(Runnable r) {
        var count = activeThreads.incrementAndGet();
        System.out.println("Currently active threads: " + count);
        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        r.run();
        activeThreads.decrementAndGet();
    }

    private record HttpResp(String body) {
        public byte[] serialize() {
            var respTemplate = """
                    HTTP/1.1 200 OK
                    Content-Type: text
                    Content-Length: %d
                    
                    %s
                    
                    """.stripIndent().stripLeading();
            var contentLength = body.getBytes(StandardCharsets.UTF_8).length;
            return String.format(respTemplate, contentLength, body).getBytes(StandardCharsets.UTF_8);
        }
    }
}
