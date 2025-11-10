package com.novaaurora.events.controller;

import com.novaaurora.events.model.Category;
import com.novaaurora.events.service.ApiClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

 
public class CategoriesController {
    @FXML private TextField txtNewCategory;
    @FXML private TableView<Category> categoriesTable;
    @FXML private TableColumn<Category, String> colCatName;

    private final ApiClient api = new ApiClient();

    @FXML
    public void initialize() {
        colCatName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        onRefresh(null);
    }

    @FXML
    public void onAddCategory(ActionEvent e) {
        String name = txtNewCategory.getText();
        if (name == null || name.isBlank()) { warn("Nome é obrigatório."); return; }
        Category c = new Category();
        c.setName(name);
        try {
            api.createCategory(c);
            txtNewCategory.clear();
            onRefresh(null);
        } catch (Exception ex) {
            error("Falha ao criar categoria: " + ex.getMessage());
        }
    }

    @FXML
    public void onUpdateCategory(ActionEvent e) {
        Category selected = categoriesTable.getSelectionModel().getSelectedItem();
        if (selected == null) { warn("Selecione uma categoria para atualizar."); return; }
        String newName = txtNewCategory.getText();
        if (newName == null || newName.isBlank()) { warn("Informe o novo nome no campo acima."); return; }
        selected.setName(newName);
        try {
            api.updateCategory(selected);
            txtNewCategory.clear();
            onRefresh(null);
        } catch (Exception ex) {
            error("Falha ao atualizar categoria: " + ex.getMessage());
        }
    }

    @FXML
    public void onDeleteCategory(ActionEvent e) {
        Category selected = categoriesTable.getSelectionModel().getSelectedItem();
        if (selected == null) { warn("Por favor, selecione uma categoria para excluir."); return; }
        try {
            boolean ok = api.deleteCategory(selected.getId());
            if (ok) onRefresh(null); else error("Exclusão falhou.");
        } catch (Exception ex) {
            error("Falha ao excluir: " + ex.getMessage());
        }
    }

    @FXML
    public void onRefresh(ActionEvent e) {
        try {
            categoriesTable.setItems(api.getCategories());
        } catch (Exception ex) {
            categoriesTable.setItems(javafx.collections.FXCollections.observableArrayList());
            error("Falha ao carregar categorias: " + ex.getMessage());
        }
    }

    private void warn(String m) { new Alert(Alert.AlertType.WARNING, m, ButtonType.OK).showAndWait(); }
    private void error(String m) { new Alert(Alert.AlertType.ERROR, m, ButtonType.OK).showAndWait(); }
}