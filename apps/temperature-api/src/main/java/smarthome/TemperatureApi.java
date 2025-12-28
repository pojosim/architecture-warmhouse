package smarthome;

import io.javalin.Javalin;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class TemperatureApi {
    private static final Random random = new Random();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.http.defaultContentType = "application/json";
            config.http.asyncTimeout = 10000L;
            config.bundledPlugins.enableCors(cors ->
                    cors.addRule(it -> it.reflectClientOrigin = true));
        });

        app.get("/temperature", ctx -> {
            String location = ctx.queryParam("location");

            ctx.json(generateTemperatureData(location, null));
        });

        app.get("/temperature/{sensorId}", ctx -> {
            String sensorId = ctx.pathParam("sensorId");
            ctx.json(generateTemperatureData(null, sensorId));
        });

        app.start(8081);
    }

    private static TemperatureData generateTemperatureData(String location, String sensorId) {
        //~ +15+30
        double temperature = 15 + random.nextDouble() * 15;

        // If no location is provided, use a default based on sensor ID
        if (location == null || location.isEmpty()) {
            location = switch (sensorId) {
                case "1" -> "Living Room";
                case "2" -> "Bedroom";
                case "3" -> "Kitchen";
                default -> "Unknown";
            };
        }

        // If no sensor ID is provided, generate one based on location
        if (sensorId == null || sensorId.isEmpty()) {
            sensorId = switch (location) {
                case "Living Room" -> "1";
                case "Bedroom" -> "2";
                case "Kitchen" -> "3";
                default -> "0";
            };
        }

        // Описание на основе температуры
        String description;
        if (temperature < 18) {
            description = "Прохладно";
        } else if (temperature < 22) {
            description = "Комфортно";
        } else if (temperature < 26) {
            description = "Тепло";
        } else {
            description = "Жарко";
        }

        return new TemperatureData(
                location,
                sensorId,
                Math.round(temperature * 100.0) / 100.0,
                "°C",
                "online",
                formatter.format(Instant.now()),
                description
        );
    }

    public static class TemperatureData {
        public String location;
        public String sensorId;
        public double value;
        public String unit;
        public String status;
        public String timestamp;
        public String description;

        public TemperatureData(String location, String sensorId, double value,
                               String unit, String status, String timestamp,
                               String description) {
            this.location = location;
            this.sensorId = sensorId;
            this.value = value;
            this.unit = unit;
            this.status = status;
            this.timestamp = timestamp;
            this.description = description;
        }
    }
}