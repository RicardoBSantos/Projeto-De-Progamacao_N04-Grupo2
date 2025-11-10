package com.novaaurora.events;

import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.testfx.assertions.api.Assertions.assertThat;

@Disabled("Pendente configurar ambiente de UI headless para TestFX")
@ExtendWith(ApplicationExtension.class)
class MainTest {

    private Stage stage;

    @Start
    private void start(Stage stage) throws Exception {
        this.stage = stage;
        new Main().start(stage);
    }

    @Test
    void should_contain_tabs_with_portuguese_titles(FxRobot robot) {
        // Given
        TabPane tabPane = robot.lookup(".tab-pane").queryAs(TabPane.class);
        
        // Then
        assertThat(tabPane).isNotNull();
        assertThat(tabPane.getTabs()).hasSize(5);
        assertThat(tabPane.getTabs().get(0).getText()).isEqualTo("Eventos");
        assertThat(tabPane.getTabs().get(1).getText()).isEqualTo("Categorias");
        assertThat(tabPane.getTabs().get(2).getText()).isEqualTo("Organizadores");
        assertThat(tabPane.getTabs().get(3).getText()).isEqualTo("Relatórios");
        assertThat(tabPane.getTabs().get(4).getText()).isEqualTo("Avaliação");
    }

    @Test
    void should_contain_portuguese_buttons_in_events_tab(FxRobot robot) {
        // Given
        robot.clickOn("Eventos");
        
        // When & Then
        assertThat(robot.lookup("Adicionar Evento").queryButton()).isNotNull();
        assertThat(robot.lookup("Editar").queryButton()).isNotNull();
        assertThat(robot.lookup("Excluir").queryButton()).isNotNull();
        assertThat(robot.lookup("Atualizar").queryButton()).isNotNull();
    }

    @Test
    void should_contain_portuguese_labels_in_event_form(FxRobot robot) {
        // Given
        robot.clickOn("Eventos");
        
        // When
        robot.clickOn("Adicionar Evento");
        
        // Then
        assertThat(robot.lookup("Nome").queryLabeled()).isNotNull();
        assertThat(robot.lookup("Descrição").queryLabeled()).isNotNull();
        assertThat(robot.lookup("Categoria").queryLabeled()).isNotNull();
        assertThat(robot.lookup("Data de Início").queryLabeled()).isNotNull();
        assertThat(robot.lookup("Data de Término").queryLabeled()).isNotNull();
        assertThat(robot.lookup("Limite de Vagas").queryLabeled()).isNotNull();
        assertThat(robot.lookup("Organizador").queryLabeled()).isNotNull();
        assertThat(robot.lookup("Autorizado").queryLabeled()).isNotNull();
        
        // Check buttons
        assertThat(robot.lookup("Salvar").queryButton()).isNotNull();
        assertThat(robot.lookup("Cancelar").queryButton()).isNotNull();
    }

    @Test
    void should_contain_portuguese_labels_in_categories_tab(FxRobot robot) {
        // Given
        robot.clickOn("Categorias");
        
        // When & Then
        assertThat(robot.lookup("Adicionar").queryButton()).isNotNull();
        assertThat(robot.lookup("Excluir").queryButton()).isNotNull();
        assertThat(robot.lookup("Atualizar").queryButton()).isNotNull();
        
        // Check table column header
        TableView<?> tableView = robot.lookup(".table-view").queryAs(TableView.class);
        assertThat(tableView).isNotNull();
        assertThat(tableView.getColumns().get(0).getText()).isEqualTo("Nome");
    }

    @Test
    void should_contain_portuguese_labels_in_reports_tab(FxRobot robot) {
        // Given
        robot.clickOn("Relatórios");
        
        // When & Then
        assertThat(robot.lookup("Total de Eventos:").queryLabeled()).isNotNull();
        assertThat(robot.lookup("Total de Participantes:").queryLabeled()).isNotNull();
        assertThat(robot.lookup("Vagas Disponíveis:").queryLabeled()).isNotNull();
        assertThat(robot.lookup("Atualizar").queryButton()).isNotNull();
    }

    @Test
    void should_contain_portuguese_labels_in_feedback_tab(FxRobot robot) {
        // Given
        robot.clickOn("Avaliação");
        
        // When & Then
        assertThat(robot.lookup("Digite sua avaliação aqui...").queryAs(javafx.scene.control.TextArea.class)).isNotNull();
        assertThat(robot.lookup("Enviar").queryButton()).isNotNull();
        assertThat(robot.lookup("Limpar").queryButton()).isNotNull();
    }
}