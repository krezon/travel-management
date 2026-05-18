package com.selim.travel;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

@RestController
public class TripController {

    private static final List<Map<String, Object>> TRIPS = List.of(
            Map.of("id", 1, "destination", "Cappadocia", "country", "Turkey",  "days", 4, "priceUSD", 320),
            Map.of("id", 2, "destination", "Kyoto",      "country", "Japan",   "days", 7, "priceUSD", 1450),
            Map.of("id", 3, "destination", "Reykjavik",  "country", "Iceland", "days", 5, "priceUSD", 980),
            Map.of("id", 4, "destination", "Marrakech",  "country", "Morocco", "days", 6, "priceUSD", 540)
    );

    private String hostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    @GetMapping("/")
    public Map<String, Object> index() {
        return Map.of(
                "app", "travel-management",
                "hostname", hostname(),
                "endpoints", List.of("GET /", "GET /trips", "GET /trips/{id}")
        );
    }

    @GetMapping("/trips")
    public Map<String, Object> listTrips() {
        return Map.of(
                "servedBy", hostname(),
                "count", TRIPS.size(),
                "trips", TRIPS
        );
    }

    @GetMapping("/trips/{id}")
    public ResponseEntity<Map<String, Object>> getTrip(@PathVariable int id) {
        return TRIPS.stream()
                .filter(t -> t.get("id").equals(id))
                .findFirst()
                .<ResponseEntity<Map<String, Object>>>map(trip -> ResponseEntity.ok(
                        Map.of("servedBy", hostname(), "trip", trip)
                ))
                .orElseGet(() -> ResponseEntity.status(404).body(
                        Map.of("error", "trip not found", "id", id)
                ));
    }
}
