package com.novaaurora.events.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;


public class FeedbackController {
    @FXML private TextArea txtFeedback;

    @FXML
    public void onSubmit(ActionEvent e) {
        String text = txtFeedback.getText();
        if (text == null || text.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Please write feedback before submitting.", ButtonType.OK).showAndWait();
            return;
        }
        
        new Alert(Alert.AlertType.INFORMATION, "Feedback submitted (local only).", ButtonType.OK).showAndWait();
        txtFeedback.clear();
    }

    @FXML
    public void onClear(ActionEvent e) { txtFeedback.clear(); }
}