package com.novaaurora.events.controller;

import com.novaaurora.events.model.Organization;
import com.novaaurora.events.service.ApiClient;
import javafx.fxml.FXML;
import javafx.scene.control.*;

 
public class OrganizationsController {
    @FXML private TableView<Organization> organizationsTable;
    @FXML private TableColumn<Organization, String> colOrgName;

    private final ApiClient api = new ApiClient();

    @FXML
    public void initialize() {
        colOrgName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        refresh();
    }

    private void refresh() {
        try {
            organizationsTable.setItems(api.getOrganizations());
        } catch (Exception ex) {
            organizationsTable.setItems(javafx.collections.FXCollections.observableArrayList());
            new Alert(Alert.AlertType.WARNING, "Organization endpoints not available.", ButtonType.OK).showAndWait();
        }
    }
}