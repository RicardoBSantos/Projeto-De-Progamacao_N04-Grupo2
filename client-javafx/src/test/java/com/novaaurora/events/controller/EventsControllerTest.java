package com.novaaurora.events.controller;

import com.novaaurora.events.model.Category;
import com.novaaurora.events.model.Event;
import com.novaaurora.events.service.ApiClient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.ComboBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Disabled("Pendente configurar ambiente JavaFX para testes de controller")
@ExtendWith(MockitoExtension.class)
class EventsControllerTest {

    @Mock
    private ApiClient apiClient;

    @Mock
    private TableView<Event> eventsTable;

    @Mock
    private ComboBox<Category> categoryFilter;

    @InjectMocks
    private EventsController eventsController;

    @BeforeEach
    void setUp() {
        // Initialize controller with mocked dependencies
    }

    @Test
    void testRefreshEvents_Success() throws Exception {
        // Arrange
        Event event1 = new Event();
        event1.setId(1L);
        event1.setTitle("Evento Teste 1");
        
        Event event2 = new Event();
        event2.setId(2L);
        event2.setTitle("Evento Teste 2");
        
        ObservableList<Event> expectedEvents = FXCollections.observableArrayList(event1, event2);
        when(apiClient.getEvents()).thenReturn(expectedEvents);
        
        // Act
        eventsController.onRefresh(null);
        
        // Assert
        verify(eventsTable).setItems(expectedEvents);
    }

    @Test
    void testFilterByCategory_Success() throws Exception {
        // Arrange
        Category selectedCategory = new Category();
        selectedCategory.setId(1L);
        selectedCategory.setName("Categoria Teste");
        
        Event event1 = new Event();
        event1.setId(1L);
        event1.setTitle("Evento Categoria 1");
        
        ObservableList<Event> filteredEvents = FXCollections.observableArrayList(event1);
        when(categoryFilter.getSelectionModel()).thenReturn(mock(javafx.scene.control.SingleSelectionModel.class));
        when(categoryFilter.getSelectionModel().getSelectedItem()).thenReturn(selectedCategory);
        when(apiClient.getEventsByCategoryId(1L)).thenReturn(filteredEvents);
        
        // Act
        eventsController.onFilterByCategory(null);
        
        // Assert
        verify(eventsTable).setItems(filteredEvents);
    }

    @Test
    void testFilterByCategory_NoSelection() throws Exception {
        // Arrange
        when(categoryFilter.getSelectionModel()).thenReturn(mock(javafx.scene.control.SingleSelectionModel.class));
        when(categoryFilter.getSelectionModel().getSelectedItem()).thenReturn(null);
        
        ObservableList<Event> allEvents = FXCollections.observableArrayList();
        when(apiClient.getEvents()).thenReturn(allEvents);
        
        // Act
        eventsController.onFilterByCategory(null);
        
        // Assert
        verify(eventsTable).setItems(allEvents);
    }

    @Test
    void testHandleApiException() throws Exception {
        // Arrange
        when(apiClient.getEvents()).thenThrow(new RuntimeException("Erro de conexão"));
        
        // Act & Assert
        // Note: This would require mocking the alert dialogs or refactoring the controller
        // For now, we just verify the method doesn't throw an unhandled exception
        assertDoesNotThrow(() -> eventsController.onRefresh(null));
    }
}