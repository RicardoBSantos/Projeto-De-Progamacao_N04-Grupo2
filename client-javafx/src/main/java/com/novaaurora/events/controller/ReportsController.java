package com.novaaurora.events.controller;

import com.novaaurora.events.model.Event;
import com.novaaurora.events.service.ApiClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

 
public class ReportsController {
    @FXML private Label lblTotalEvents;
    @FXML private Label lblTotalParticipants;
    @FXML private Label lblAvailableSeats;

    private final ApiClient api = new ApiClient();

    @FXML
    public void initialize() {
        compute();
    }

    @FXML
    public void onRefresh(ActionEvent e) { compute(); }

    private void compute() {
        try {
            var events = api.getEvents();
            int total = events.size();
            
            int participants = 0;
            int seats = events.stream().map(Event::getSeatLimit).filter(s -> s != null).mapToInt(Integer::intValue).sum();
            lblTotalEvents.setText(String.valueOf(total));
            lblTotalParticipants.setText(String.valueOf(participants));
            lblAvailableSeats.setText(String.valueOf(seats));
        } catch (Exception ex) {
            lblTotalEvents.setText("-");
            lblTotalParticipants.setText("-");
            lblAvailableSeats.setText("-");
            new Alert(Alert.AlertType.ERROR, "Failed to load reports: " + ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }
}