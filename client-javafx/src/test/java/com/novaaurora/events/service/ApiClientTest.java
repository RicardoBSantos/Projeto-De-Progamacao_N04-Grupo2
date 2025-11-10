package com.novaaurora.events.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.novaaurora.events.model.Category;
import com.novaaurora.events.model.Event;
import com.novaaurora.events.model.Organization;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiClientTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    private ApiClient apiClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        apiClient = new ApiClient(httpClient, objectMapper);
    }

    @Test
    void testGetEvents_Success() throws Exception {
        // Arrange
        Event event1 = new Event();
        event1.setId(1L);
        event1.setTitle("Evento Teste 1");
        event1.setDescription("Descrição do evento 1");
        
        Event event2 = new Event();
        event2.setId(2L);
        event2.setTitle("Evento Teste 2");
        event2.setDescription("Descrição do evento 2");
        
        List<Event> expectedEvents = Arrays.asList(event1, event2);
        String jsonResponse = objectMapper.writeValueAsString(expectedEvents);
        
        // Mock the HTTP response
        when(httpResponse.body()).thenReturn(jsonResponse);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        // Act
        ObservableList<Event> result = apiClient.getEvents();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Evento Teste 1", result.get(0).getTitle());
        assertEquals("Evento Teste 2", result.get(1).getTitle());
    }

    @Test
    void testGetCategories_Success() throws Exception {
        // Arrange
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Categoria Teste 1");
        
        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Categoria Teste 2");
        
        List<Category> expectedCategories = Arrays.asList(category1, category2);
        String jsonResponse = objectMapper.writeValueAsString(expectedCategories);
        
        // Mock the HTTP response
        when(httpResponse.body()).thenReturn(jsonResponse);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        // Act
        ObservableList<Category> result = apiClient.getCategories();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Categoria Teste 1", result.get(0).getName());
        assertEquals("Categoria Teste 2", result.get(1).getName());
    }

    @Test
    void testCreateEvent_Success() throws Exception {
        // Arrange
        Event newEvent = new Event();
        newEvent.setTitle("Novo Evento");
        newEvent.setDescription("Descrição do novo evento");
        newEvent.setStart(LocalDateTime.now().plusDays(1));
        newEvent.setEnd(LocalDateTime.now().plusDays(2));
        newEvent.setSeatLimit(100);
        
        Event createdEvent = new Event();
        createdEvent.setId(1L);
        createdEvent.setTitle(newEvent.getTitle());
        createdEvent.setDescription(newEvent.getDescription());
        
        String jsonResponse = objectMapper.writeValueAsString(createdEvent);
        
        // Mock the HTTP response
        when(httpResponse.body()).thenReturn(jsonResponse);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        // Act
        Event result = apiClient.createEvent(newEvent);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Novo Evento", result.getTitle());
    }

    @Test
    void testDeleteEvent_Success() throws Exception {
        // Arrange
        Long eventId = 1L;
        
        // Mock the HTTP response
        when(httpResponse.statusCode()).thenReturn(204);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        // Act
        boolean deleted = apiClient.deleteEvent(eventId);
        
        // Assert
        assertTrue(deleted);
    }

    @Test
    void testHandleHttpError() throws Exception {
        // Arrange
        // Mock error response (invalid JSON to trigger parse exception)
        when(httpResponse.body()).thenReturn("Internal Server Error");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        // Act
        assertThrows(IOException.class, () -> {
            // Simulate mapper reading and throwing due to unexpected body
            apiClient.getEvents();
        });
    }
}