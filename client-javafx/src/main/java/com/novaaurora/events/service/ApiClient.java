package com.novaaurora.events.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.novaaurora.events.model.Category;
import com.novaaurora.events.model.Event;
import com.novaaurora.events.model.Organization;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * Simple REST client using Java HttpClient to talk to the Spring Boot backend.
 * Endpoints default to /api/eventos, but can be adapted by changing BASE_URL.
 */
public class ApiClient {
    private static final String BASE_URL = "http://localhost:8080/api"; // adjust if needed
    private static final String EVENTS_PATH = "/eventos";
    private static final String CATEGORIES_PATH = "/categorias"; // requires backend support
    private static final String ORGANIZATIONS_PATH = "/organizacoes"; // requires backend support

    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private Long usuarioIdHeader = 1L; // used for POST/PUT/DELETE when backend checks organizer permission

    public ApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.mapper = defaultMapper();
    }

    public ApiClient(HttpClient httpClient, ObjectMapper mapper) {
        this.httpClient = httpClient != null ? httpClient : HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.mapper = mapper != null ? mapper : defaultMapper();
    }

    private static ObjectMapper defaultMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void setUsuarioIdHeader(Long id) {
        this.usuarioIdHeader = id;
    }

    // Events
    public ObservableList<Event> getEvents() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE_URL + EVENTS_PATH))
                .GET()
                .build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        List<Event> events = mapper.readValue(res.body(), mapper.getTypeFactory().constructCollectionType(List.class, Event.class));
        return FXCollections.observableArrayList(events);
    }

    public ObservableList<Event> getEventsByCategoryId(Long categoriaId) throws IOException, InterruptedException {
        // Uses the filter endpoint found in backend: /api/eventos/busca?categoriaId=
        String url = BASE_URL + EVENTS_PATH + "/busca?categoriaId=" + categoriaId;
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        List<Event> events = mapper.readValue(res.body(), mapper.getTypeFactory().constructCollectionType(List.class, Event.class));
        return FXCollections.observableArrayList(events);
    }

    public Event createEvent(Event event) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(event);
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE_URL + EVENTS_PATH))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Usuario-Id", String.valueOf(usuarioIdHeader))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        int status = res.statusCode();
        String responseBody = res.body();
        String contentType = res.headers().firstValue("Content-Type").orElse("");
        if (status >= 200 && status < 300) {
            if (responseBody != null && !responseBody.isBlank() && contentType.toLowerCase().contains("json")) {
                return mapper.readValue(responseBody, Event.class);
            }
            return event; // 201 sem corpo ou corpo não-JSON
        }
        throw new IOException("HTTP " + status + " ao criar evento" + (responseBody != null && !responseBody.isBlank() ? ": " + responseBody : ""));
    }

    public Event updateEvent(Long id, Event event) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(event);
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE_URL + EVENTS_PATH + "/" + id))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Usuario-Id", String.valueOf(usuarioIdHeader))
                .PUT(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        int status = res.statusCode();
        String responseBody = res.body();
        String contentType = res.headers().firstValue("Content-Type").orElse("");
        if (status >= 200 && status < 300) {
            if (responseBody != null && !responseBody.isBlank() && contentType.toLowerCase().contains("json")) {
                return mapper.readValue(responseBody, Event.class);
            }
            return event;
        }
        throw new IOException("HTTP " + status + " ao atualizar evento" + (responseBody != null && !responseBody.isBlank() ? ": " + responseBody : ""));
    }

    public boolean deleteEvent(Long id) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE_URL + EVENTS_PATH + "/" + id))
                .header("Usuario-Id", String.valueOf(usuarioIdHeader))
                .DELETE()
                .build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return res.statusCode() == 204 || res.statusCode() == 200;
    }

    // Categories (requires backend controller; UI will handle errors gracefully if missing)
    public ObservableList<Category> getCategories() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE_URL + CATEGORIES_PATH)).GET().build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        int status = res.statusCode();
        String body = res.body();
        if (status >= 200 && status < 300) {
            if (body == null || body.isBlank()) {
                return FXCollections.observableArrayList();
            }
            try {
                List<Category> cats = mapper.readValue(body, mapper.getTypeFactory().constructCollectionType(List.class, Category.class));
                return FXCollections.observableArrayList(cats);
            } catch (Exception parseEx) {
                throw new IOException("HTTP " + status + " ao consultar categorias: corpo inválido", parseEx);
            }
        }
        throw new IOException("HTTP " + status + " ao consultar categorias" + (body != null && !body.isBlank() ? ": " + body : ""));
    }

    public Category createCategory(Category category) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(category);
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE_URL + CATEGORIES_PATH))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Usuario-Id", String.valueOf(usuarioIdHeader))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        try {
            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int status = res.statusCode();
            String responseBody = res.body();
            if (status >= 200 && status < 300) {
                if (responseBody != null && !responseBody.isBlank()) {
                    return mapper.readValue(responseBody, Category.class);
                }
                // Backend may return 201 without body
                return category;
            }
            throw new IOException("HTTP " + status + " ao criar categoria" + (responseBody != null && !responseBody.isBlank() ? ": " + responseBody : ""));
        } catch (Exception ex) {
            throw new IOException("Erro ao criar categoria: " + ex.toString(), ex);
        }
    }

    public Category updateCategory(Category category) throws IOException, InterruptedException {
        if (category.getId() == null) throw new IOException("Categoria sem ID para atualização");
        String body = mapper.writeValueAsString(category);
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE_URL + CATEGORIES_PATH + "/" + category.getId()))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Usuario-Id", String.valueOf(usuarioIdHeader))
                .PUT(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        int status = res.statusCode();
        String responseBody = res.body();
        if (status >= 200 && status < 300) {
            if (responseBody != null && !responseBody.isBlank()) {
                return mapper.readValue(responseBody, Category.class);
            }
            return category;
        }
        throw new IOException("HTTP " + status + " ao atualizar categoria" + (responseBody != null && !responseBody.isBlank() ? ": " + responseBody : ""));
    }

    public boolean deleteCategory(Long id) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE_URL + CATEGORIES_PATH + "/" + id))
                .header("Usuario-Id", String.valueOf(usuarioIdHeader))
                .DELETE().build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        int status = res.statusCode();
        if (status == 204 || status == 200) return true;
        String responseBody = res.body();
        throw new IOException("HTTP " + status + " ao excluir categoria" + (responseBody != null && !responseBody.isBlank() ? ": " + responseBody : ""));
    }

    // Organizations (requires backend controller)
    public ObservableList<Organization> getOrganizations() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE_URL + ORGANIZATIONS_PATH)).GET().build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        List<Organization> orgs = mapper.readValue(res.body(), mapper.getTypeFactory().constructCollectionType(List.class, Organization.class));
        return FXCollections.observableArrayList(orgs);
    }
}