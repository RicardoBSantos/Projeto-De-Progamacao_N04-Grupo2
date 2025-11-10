package com.novaaurora.events.controller;

import com.novaaurora.events.model.Category;
import com.novaaurora.events.model.Event;
import com.novaaurora.events.model.Organization;
import com.novaaurora.events.service.ApiClient;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Modal controller for adding/editing events. Includes validation and alerts.
 */
public class EventFormController {
    @FXML private TextField txtName;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<Category> comboCategory;
    @FXML private DatePicker dateStart;
    @FXML private DatePicker dateEnd;
    @FXML private TextField txtSeats;
    @FXML private TextField txtOrganizer;
    @FXML private CheckBox chkAuthorized;

    private ApiClient api;
    private Event editing;

    public void setApi(ApiClient api) { this.api = api; }
    public void setEvent(Event event) {
        this.editing = event;
        if (event != null) {
            txtName.setText(event.getTitle());
            txtDescription.setText(event.getDescription());
            comboCategory.getSelectionModel().select(event.getCategory());
            if (event.getStart() != null) dateStart.setValue(event.getStart().toLocalDate());
            if (event.getEnd() != null) dateEnd.setValue(event.getEnd().toLocalDate());
            txtSeats.setText(event.getSeatLimit() != null ? String.valueOf(event.getSeatLimit()) : "");
            if (event.getOrganization() != null) txtOrganizer.setText(event.getOrganization().getName());
            chkAuthorized.setSelected(event.isAuthorized());
        }
    }
    public void setCategories(ObservableList<Category> cats) { comboCategory.setItems(cats); }

    @FXML
    public void onSave(ActionEvent e) {
        // Basic validations
        if (!chkAuthorized.isSelected()) {
            showWarn("Você deve estar autorizado para submeter.");
            return;
        }
        String name = txtName.getText();
        if (name == null || name.isBlank()) { showWarn("Nome é obrigatório."); return; }
        Category cat = comboCategory.getSelectionModel().getSelectedItem();
        if (cat == null) { showWarn("Categoria é obrigatória."); return; }
        LocalDate ds = dateStart.getValue();
        LocalDate de = dateEnd.getValue();
        if (ds == null || de == null) { showWarn("Datas de início e término são obrigatórias."); return; }
        LocalDateTime start = LocalDateTime.of(ds, LocalTime.NOON);
        LocalDateTime end = LocalDateTime.of(de, LocalTime.NOON);
        if (!end.isAfter(start)) { showWarn("Término deve ser após o início."); return; }
        int seats;
        try {
            seats = Integer.parseInt(txtSeats.getText());
        } catch (Exception ex) { showWarn("Vagas devem ser um número positivo."); return; }
        if (seats <= 0) { showWarn("Limite de vagas deve ser > 0."); return; }

        Event payload = editing != null ? editing : new Event();
        payload.setTitle(name);
        payload.setDescription(txtDescription.getText());
        payload.setCategory(cat);
        payload.setStart(start);
        payload.setEnd(end);
        payload.setSeatLimit(seats);
        payload.setAuthorized(true);

        String orgName = txtOrganizer.getText();
        if (orgName != null && !orgName.isBlank()) {
            Organization org = payload.getOrganization() != null ? payload.getOrganization() : new Organization();
            org.setName(orgName);
            payload.setOrganization(org);
        }

        try {
            if (payload.getId() == null) {
                api.createEvent(payload);
            } else {
                api.updateEvent(payload.getId(), payload);
            }
            close();
        } catch (Exception ex) {
            showErr("Falha ao salvar: " + ex.getMessage());
        }
    }

    @FXML
    public void onCancel() { close(); }

    private void close() {
        Stage s = (Stage) txtName.getScene().getWindow();
        s.close();
    }

    private void showWarn(String msg) { new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait(); }
    private void showErr(String msg) { new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait(); }
}