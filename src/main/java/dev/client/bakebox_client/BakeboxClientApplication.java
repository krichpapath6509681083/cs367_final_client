package dev.client.bakebox_client;

import dev.client.bakebox_client.model.Box;
import dev.client.bakebox_client.model.Item;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@SpringBootApplication
public class BakeboxClientApplication {

    private static final String BASE_URL = "http://172.20.10.2:8080/api";

    public static void main(String[] args) {
        SpringApplication.run(BakeboxClientApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public CommandLineRunner runTests(RestTemplate restTemplate) {
        return args -> {
            // Test Case 01
            System.out.println("\n=== [TC01] Get all available boxes ===");
            ResponseEntity<List<Box>> tc01Response = restTemplate.exchange(
                    BASE_URL + "/boxes",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            tc01Response.getBody().forEach(System.out::println);

            // Test Case 02
            System.out.println("\n=== [TC02] Get box by exact name (Matcha Lover Box) ===");
            ResponseEntity<List<Box>> tc02Response = restTemplate.exchange(
                    BASE_URL + "/boxes/boxname/Matcha Lover Box",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            tc02Response.getBody().forEach(System.out::println);

            // Test Case 03
            System.out.println("\n=== [TC03] Get all items from all boxes ===");
            ResponseEntity<List<Item>> tc03Response = restTemplate.exchange(
                    BASE_URL + "/items",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            tc03Response.getBody().forEach(System.out::println);

            // Test Case 04
            System.out.println("\n=== [TC04] Create a new box with full item list (valid) ===");
            Box newBox = new Box("Japanese Snack Box", 59, List.of(
                    new Item("Tokyo Banana", 85, 4),
                    new Item("Matcha Pocky", 45, 6),
                    new Item("Yuzu Citrus Candy", 50, 5),
                    new Item("Shiroi Koibito Cookies", 90, 3),
                    new Item("Ume Plum Rice Crackers", 65, 4)
            ));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Box> tc04Request = new HttpEntity<>(newBox, headers);
            ResponseEntity<Box> tc04Response = restTemplate.postForEntity(BASE_URL + "/boxes", tc04Request, Box.class);
            System.out.println(tc04Response.getBody());

            // Test Case 05
            System.out.println("\n=== [TC05] Create a box with empty item list ===");
            Box emptyBox = new Box("Empty Box", 10, List.of());
            HttpEntity<Box> tc05Request = new HttpEntity<>(emptyBox, headers);
            try {
                ResponseEntity<Box> tc05Response = restTemplate.postForEntity(BASE_URL + "/boxes", tc05Request, Box.class);
                System.out.println(tc05Response.getBody());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

            // Test Case 06
            System.out.println("\n=== [TC06] Create a box with missing fields (invalid payload) ===");
            HttpEntity<Map<String, Object>> tc06Request = new HttpEntity<>(Map.of("boxName", "Invalid Box"), headers);
            try {
                ResponseEntity<String> tc06Response = restTemplate.postForEntity(BASE_URL + "/boxes", tc06Request, String.class);
                System.out.println(tc06Response.getBody());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

            // Test Case 15
            System.out.println("\n=== [TC15] Update and delete items with valid actions ===");
            List<Map<String, Object>> actions = List.of(
                    Map.of("itemId", 3, "action", "delete"),
                    Map.of("itemId", 4, "action", "update", "deltaAmount", -2),
                    Map.of("itemId", 5, "action", "update", "deltaAmount", 1000)
            );
            HttpEntity<List<Map<String, Object>>> tc15Request = new HttpEntity<>(actions, headers);
            ResponseEntity<String> tc15Response = restTemplate.exchange(BASE_URL + "/items/quantity", HttpMethod.PUT, tc15Request, String.class);
            System.out.println(tc15Response.getBody());

            // Test Case 16
            System.out.println("\n=== [TC16] Update item with invalid action keyword ===");
            List<Map<String, Object>> invalidAction = List.of(
                    Map.of("itemId", 5, "action", "explode")
            );
            HttpEntity<List<Map<String, Object>>> tc16Request = new HttpEntity<>(invalidAction, headers);
            try {
                ResponseEntity<String> tc16Response = restTemplate.exchange(BASE_URL + "/items/quantity", HttpMethod.PUT, tc16Request, String.class);
                System.out.println(tc16Response.getBody());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

            // Test Case 17
            System.out.println("\n=== [TC17] Update non-existent itemId ===");
            List<Map<String, Object>> nonexistentItem = List.of(
                    Map.of("itemId", 9999, "action", "update", "deltaAmount", 2)
            );
            HttpEntity<List<Map<String, Object>>> tc17Request = new HttpEntity<>(nonexistentItem, headers);
            try {
                ResponseEntity<String> tc17Response = restTemplate.exchange(BASE_URL + "/items/quantity", HttpMethod.PUT, tc17Request, String.class);
                System.out.println(tc17Response.getBody());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

            // Test Case 18
            System.out.println("\n=== [TC18] Delete box by valid ID ===");
            try {
                ResponseEntity<String> tc18Response = restTemplate.exchange(BASE_URL + "/boxes/1", HttpMethod.DELETE, null, String.class);
                System.out.println(tc18Response.getBody());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

            // Test Case 19
            System.out.println("\n=== [TC19] Delete non-existent box by ID ===");
            try {
                ResponseEntity<String> tc19Response = restTemplate.exchange(BASE_URL + "/boxes/9999", HttpMethod.DELETE, null, String.class);
                System.out.println(tc19Response.getBody());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

            // Test Case 20
            System.out.println("\n=== [TC20] Delete with invalid ID type (string instead of number) ===");
            try {
                ResponseEntity<String> tc20Response = restTemplate.exchange(BASE_URL + "/boxes/abc", HttpMethod.DELETE, null, String.class);
                System.out.println(tc20Response.getBody());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        };
    }
}