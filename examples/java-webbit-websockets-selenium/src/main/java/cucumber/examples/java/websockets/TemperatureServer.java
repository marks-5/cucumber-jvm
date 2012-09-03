package cucumber.examples.java.websockets;

import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.handler.EmbeddedResourceHandler;

import java.io.IOException;

public class TemperatureServer {
    private final WebServer webServer;

    public TemperatureServer(int port) {
        webServer = WebServers.createWebServer(port);
        webServer.add(new EmbeddedResourceHandler("web"));
        webServer.add("/temperature", new BaseWebSocketHandler() {
            @Override
            public void onMessage(WebSocketConnection connection, String msg) throws Throwable {
                String[] parts = msg.split(":");
                String unit = parts[0];
                double value = Double.parseDouble(parts[1]);
                if (unit.equals("celcius")) {
                    double f = (9.0 / 5.0) * value + 32;
                    connection.send("fahrenheit:" + roundOneDecimal(f));
                }
                if (unit.equals("fahrenheit")) {
                    double c = (value - 32) * (5.0 / 9.0);
                    connection.send("celcius:" + roundOneDecimal(c));
                }
            }
        });
    }

    private double roundOneDecimal(double n) {
        return (double) Math.round(n * 10) / 10;
    }

    public void start() throws IOException {
        webServer.start();
    }

    public void stop() throws IOException {
        webServer.stop();
    }

    public static void main(String[] args) throws IOException {
        new TemperatureServer(9988).start();
    }
}
