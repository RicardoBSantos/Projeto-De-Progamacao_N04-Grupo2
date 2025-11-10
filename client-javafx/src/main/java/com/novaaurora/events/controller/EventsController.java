package com.novaaurora.events.controller;

import com.novaaurora.events.model.Category;
import com.novaaurora.events.model.Event;
import com.novaaurora.events.service.ApiClient;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

 
public class EventsController {
    @FXML private TableView<Event> eventsTable;
    @FXML private TableColumn<Event, String> colTitle;
    @FXML private TableColumn<Event, String> colCategory;
    @FXML private TableColumn<Event, LocalDateTime> colStart;
    @FXML private TableColumn<Event, LocalDateTime> colEnd;
    @FXML private TableColumn<Event, Integer> colSeats;
    @FXML private ComboBox<Category> categoryFilter;

    private final ApiClient api = new ApiClient();

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colCategory.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getCategory() != null ? cell.getValue().getCategory().getName() : ""));
        colStart.setCellValueFactory(new PropertyValueFactory<>("start"));
        colEnd.setCellValueFactory(new PropertyValueFactory<>("end"));
        colSeats.setCellValueFactory(new PropertyValueFactory<>("seatLimit"));

        refreshCategories();
        refreshEvents();
    }

    @FXML
    public void onRefresh(ActionEvent e) {
        refreshEvents();
    }

    @FXML
    public void onFilterByCategory(ActionEvent e) {
        Category selected = categoryFilter.getSelectionModel().getSelectedItem();
        if (selected == null) {
            refreshEvents();
            return;
        }
        try {
            ObservableList<Event> events = api.getEventsByCategoryId(selected.getId());
            eventsTable.setItems(events);
        } catch (Exception ex) {
            showError("Falha ao filtrar eventos por categoria: " + ex.getMessage());
        }
    }

    @FXML
    public void onAddEvent(ActionEvent e) {
        openEventForm(null);
    }

    @FXML
    public void onEditEvent(ActionEvent e) {
        Event selected = eventsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Por favor, selecione um evento para editar.");
            return;
        }
        if (selected.getStart() != null && selected.getStart().isBefore(LocalDateTime.now())) {
            showWarning("Não é possível editar um evento que já começou.");
            return;
        }
        openEventForm(selected);
    }

    @FXML
    public void onDeleteEvent(ActionEvent e) {
        Event selected = eventsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Por favor, selecione um evento para excluir.");
            return;
        }
        if (selected.getStart() != null && selected.getStart().isBefore(LocalDateTime.now())) {
            showWarning("Não é possível excluir um evento que já começou.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Excluir evento selecionado?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                boolean ok = api.deleteEvent(selected.getId());
                if (ok) refreshEvents(); else showError("Exclusão falhou.");
            } catch (Exception ex) {
                showError("Falha ao excluir: " + ex.getMessage());
            }
        }
    }

    private void openEventForm(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EventForm.fxml"));
            Stage dlg = new Stage();
            dlg.initModality(Modality.APPLICATION_MODAL);
            dlg.setTitle(event == null ? "Adicionar Evento" : "Editar Evento");
            dlg.setScene(new Scene(loader.load()));
            EventFormController ctrl = loader.getController();
            ctrl.setApi(api);
            ctrl.setEvent(event);
            ctrl.setCategories(categoryFilter.getItems());
            dlg.showAndWait();
            refreshEvents();
        } catch (IOException ex) {
            showError("Falha ao abrir formulário de evento: " + ex.getMessage());
        }
    }

    private void refreshEvents() {
        try {
            eventsTable.setItems(api.getEvents());
        } catch (Exception ex) {
            showError("Falha ao carregar eventos: " + ex.getMessage());
        }
    }

    private void refreshCategories() {
        try {
            ObservableList<Category> cats = api.getCategories();
            categoryFilter.setItems(cats);
        } catch (Exception ex) {
            categoryFilter.setItems(javafx.collections.FXCollections.observableArrayList());
        }
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    private void showWarning(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }
}