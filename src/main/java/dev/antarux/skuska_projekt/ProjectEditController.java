package dev.antarux.skuska_projekt;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDateTime;

public class ProjectEditController {
    @FXML private TextField tfProjectKey;
    @FXML private TextField tfName;
    @FXML private TextArea taDescription;
    @FXML private CheckBox chkActive;
    @FXML private Button btnDelete;

    private Project project;
    private MainController mainController;
    private boolean dataChanged = false;

    public void setProject(Project project) {
        this.project = project;

        if (project != null) {
            tfProjectKey.setText(project.getProjectKey());
            tfName.setText(project.getName());
            taDescription.setText(project.getDescription());
            chkActive.setSelected(project.isActive());

            btnDelete.setVisible(true);
            tfProjectKey.setDisable(true);
        } else {
            this.project = new Project();
            this.project.setCreatedAt(LocalDateTime.now());
            this.project.setActive(true);
            btnDelete.setVisible(false);
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public boolean isDataChanged() {
        return dataChanged;
    }

    @FXML private void saveProject() {
        String key = tfProjectKey.getText().trim();
        String name = tfName.getText().trim();

        if (key.isEmpty()) {
            showError("Kľúč projektu je povinný!");
            return;
        }
        if (name.isEmpty()) {
            showError("Názov projektu je povinný!");
            return;
        }
        if (key.length() > 20) {
            showError("Kľúč projektu môže mať maximálne 20 znakov!");
            return;
        }

        project.setProjectKey(key);
        project.setName(name);
        project.setDescription(taDescription.getText());
        project.setActive(chkActive.isSelected());

        boolean isNew = (project.getId() == 0);

        Connection conn = null;
        try {
            conn = new Database().getConnection();
            conn.setAutoCommit(false);

            if (isNew) {
                String sql = """
                        INSERT INTO projects (name, project_key, description, created_at, is_active)
                        VALUES (?, ?, ?, ?, ?)
                        """;
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, project.getName());
                    ps.setString(2, project.getProjectKey());
                    ps.setString(3, project.getDescription());
                    ps.setTimestamp(4, Timestamp.valueOf(project.getCreatedAt()));
                    ps.setBoolean(5, project.isActive());
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            project.setId(rs.getLong(1));
                        }
                    }
                }
            } else {
                String sql = """
                        UPDATE projects
                        SET name = ?, description = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """;
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, project.getName());
                    ps.setString(2, project.getDescription());
                    ps.setBoolean(3, project.isActive());
                    ps.setLong(4, project.getId());
                    ps.executeUpdate();
                }
            }

            conn.commit();
            dataChanged = true;
            closeWindow();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            showError("Chyba pri ukladaní projektu:\n" + e.getMessage());
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @FXML private void deleteProject() {
        if (project == null || project.getId() == 0) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Naozaj chcete vymazať projekt " + project.getProjectKey() + "?\n" +
                        "Úlohy projektu nebudú vymazané, ale projekt sa stane neaktívny.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Potvrdenie mazania");

        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try (Connection conn = new Database().getConnection()) {
                String sql = "UPDATE projects SET is_active = FALSE, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setLong(1, project.getId());
                    ps.executeUpdate();
                }

                dataChanged = true;
                closeWindow();
            } catch (SQLException e) {
                showError("Chyba pri deaktivácii projektu:\n" + e.getMessage());
            }
        }
    }

    @FXML private void closeWindow() {
        Stage stage = (Stage) tfName.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }
}
