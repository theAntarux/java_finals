package dev.antarux.skuska_projekt;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDateTime;

public class TaskEditController {
    @FXML private ComboBox<Project> cbProject;
    @FXML private ComboBox<Task.Priority> cbPriority;
    @FXML private ComboBox<Task.Status> cbStatus;
    @FXML private TextField tfTitle;
    @FXML private TextArea taDescription;
    @FXML private TextField tfEstimatedHours;

    @FXML private ComboBox<TeamMember> cbAvailableMembers;
    @FXML private ListView<TeamMember> lvAssignedMembers;
    @FXML private ComboBox<Tag> cbAvailableTags;
    @FXML private ListView<Tag> lvAssignedTags;

    @FXML private Button btnDelete;

    private Task task;
    private MainController mainController;
    private boolean dataChanged = false;

    private ObservableList<Project> projects = FXCollections.observableArrayList();
    private ObservableList<TeamMember> allMembers = FXCollections.observableArrayList();
    private ObservableList<Tag> allTags = FXCollections.observableArrayList();

    @FXML public void initialize() {
        cbPriority.getItems().addAll(Task.Priority.values());
        cbStatus.getItems().addAll(Task.Status.values());

        loadProjects();
        loadAllMembers();
        loadAllTags();

        cbProject.setItems(projects);
        cbAvailableMembers.setItems(allMembers);
        cbAvailableTags.setItems(allTags);

        lvAssignedMembers.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lvAssignedTags.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void setTask(Task task) {
        this.task = task;

        if (task != null) {
            cbProject.setValue(findProjectById(task.getProjectId()));
            cbPriority.setValue(task.getPriority());
            cbStatus.setValue(task.getStatus());
            tfTitle.setText(task.getTitle());
            taDescription.setText(task.getDescription());
            tfEstimatedHours.setText(String.valueOf(task.getEstimatedHours()));

            lvAssignedMembers.setItems(task.getAssignees());
            lvAssignedTags.setItems(task.getTags());

            btnDelete.setVisible(true);
        } else {
            this.task = new Task();
            this.task.setCreatedAt(LocalDateTime.now());
            this.task.setCreatedById(1);
            btnDelete.setVisible(false);
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public boolean isDataChanged() {
        return dataChanged;
    }

    private Project findProjectById(long id) {
        for (Project p : projects) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    private void loadProjects() {
        projects.clear();
        String sql = """
                SELECT id, name, project_key, description, created_at, is_active
                FROM projects
                WHERE is_active = true
                ORDER BY project_key
                """;

        try (Connection conn = new Database().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Project p = new Project();
                p.setId(rs.getLong("id"));
                p.setName(rs.getString("name"));
                p.setProjectKey(rs.getString("project_key"));
                p.setDescription(rs.getString("description"));
                p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                p.setActive(rs.getBoolean("is_active"));
                projects.add(p);
            }
        } catch (SQLException e) {
            showError("Nepodarilo sa načítať projekty: " + e.getMessage());
        }
    }

    private void loadAllMembers() {
        allMembers.clear();
        String sql = """
                SELECT id, username, full_name, email, role, active, created_at
                FROM team_members
                WHERE active = true
                ORDER BY username
                """;

        try (Connection conn = new Database().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                TeamMember m = new TeamMember();
                m.setId(rs.getLong("id"));
                m.setUsername(rs.getString("username"));
                m.setFullName(rs.getString("full_name"));
                m.setEmail(rs.getString("email"));
                m.setRole(rs.getString("role"));
                m.setActive(rs.getBoolean("active"));
                m.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                allMembers.add(m);
            }
        } catch (SQLException e) {
            showError("Nepodarilo sa načítať členov: " + e.getMessage());
        }
    }

    private void loadAllTags() {
        allTags.clear();
        String sql = """
                SELECT id, name, color_hex, description
                FROM tags
                ORDER BY name
                """;

        try (Connection conn = new Database().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Tag t = new Tag();
                t.setId(rs.getLong("id"));
                t.setName(rs.getString("name"));
                t.setColorHex(rs.getString("color_hex"));
                t.setDescription(rs.getString("description"));
                allTags.add(t);
            }
        } catch (SQLException e) {
            showError("Nepodarilo sa načítať tagy: " + e.getMessage());
        }
    }

    @FXML private void addAssignee() {
        TeamMember selected = cbAvailableMembers.getValue();
        if (selected != null && !lvAssignedMembers.getItems().contains(selected)) {
            lvAssignedMembers.getItems().add(selected);
            cbAvailableMembers.setValue(null);
        }
    }

    @FXML private void removeAssignee() {
        ObservableList<TeamMember> selected = lvAssignedMembers.getSelectionModel().getSelectedItems();
        lvAssignedMembers.getItems().removeAll(selected);
    }

    @FXML private void addTag() {
        Tag selected = cbAvailableTags.getValue();
        if (selected != null && !lvAssignedTags.getItems().contains(selected)) {
            lvAssignedTags.getItems().add(selected);
            cbAvailableTags.setValue(null);
        }
    }

    @FXML private void removeTag() {
        ObservableList<Tag> selected = lvAssignedTags.getSelectionModel().getSelectedItems();
        lvAssignedTags.getItems().removeAll(selected);
    }

    @FXML private void saveTask() {
        String title = tfTitle.getText().trim();
        if (title.isEmpty()) {
            showError("Názov úlohy je povinný!");
            return;
        }

        Project proj = cbProject.getValue();
        if (proj == null) {
            showError("Vyberte projekt!");
            return;
        }

        Task.Priority prio = cbPriority.getValue();
        if (prio == null) {
            prio = Task.Priority.MEDIUM;
            cbPriority.setValue(prio);
        }

        Task.Status stat = cbStatus.getValue();
        if (stat == null) {
            stat = Task.Status.TODO;
            cbStatus.setValue(stat);
        }

        double hours = 0.0;
        try {
            String hoursText = tfEstimatedHours.getText().trim();
            if (!hoursText.isEmpty()) {
                hours = Double.parseDouble(hoursText);
                if (hours < 0) {
                    throw new Exception();
                }
            }
        } catch (Exception e) {
            showError("Odhadovaný čas musí byť kladné číslo!");
            return;
        }

        String desc = taDescription.getText();

        task.setProjectId(proj.getId());
        task.setTitle(title);
        task.setDescription(desc);
        task.setPriority(prio);
        task.setStatus(stat);
        task.setEstimatedHours(hours);
        task.getAssignees().setAll(lvAssignedMembers.getItems());
        task.getTags().setAll(lvAssignedTags.getItems());

        if (task.getStatus() == Task.Status.DONE && task.getAssignees().isEmpty()) {
            showError("Nemôžete uzavrieť úlohu bez priradeného riešiteľa!");
            return;
        }

        boolean isNew = (task.getId() == 0);
        if (!isNew) {
            task.setUpdatedAt(LocalDateTime.now());
        }
        if (task.getStatus() == Task.Status.DONE && task.getClosedAt() == null) {
            task.setClosedAt(LocalDateTime.now());
        } else if (task.getStatus() != Task.Status.DONE && task.getClosedAt() != null) {
            task.setClosedAt(null);
        }

        Connection conn = null;
        try {
            conn = new Database().getConnection();
            conn.setAutoCommit(false);

            if (isNew) {
                String sql = """
                        INSERT INTO tasks (project_id, title, description, priority, status, 
                                           estimated_hours, created_at, created_by_id, closed_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """;
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, task.getProjectId());
                    ps.setString(2, task.getTitle());
                    ps.setString(3, task.getDescription());
                    ps.setString(4, task.getPriority().name());
                    ps.setString(5, task.getStatus().name());
                    ps.setDouble(6, task.getEstimatedHours());
                    ps.setTimestamp(7, Timestamp.valueOf(task.getCreatedAt()));
                    ps.setLong(8, task.getCreatedById());
                    ps.setTimestamp(9, task.getClosedAt() != null ? Timestamp.valueOf(task.getClosedAt()) : null);

                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) task.setId(keys.getLong(1));
                    }
                }
            } else {
                String sql = """
                        UPDATE tasks
                        SET project_id = ?, title = ?, description = ?, priority = ?,
                            status = ?, estimated_hours = ?, updated_at = ?, closed_at = ?
                        WHERE id = ?
                        """;
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setLong(1, task.getProjectId());
                    ps.setString(2, task.getTitle());
                    ps.setString(3, task.getDescription());
                    ps.setString(4, task.getPriority().name());
                    ps.setString(5, task.getStatus().name());
                    ps.setDouble(6, task.getEstimatedHours());
                    ps.setTimestamp(7, Timestamp.valueOf(task.getUpdatedAt()));
                    ps.setTimestamp(8, task.getClosedAt() != null ? Timestamp.valueOf(task.getClosedAt()) : null);
                    ps.setLong(9, task.getId());
                    ps.executeUpdate();
                }
            }

            Statement stmt = conn.createStatement();
            stmt.execute("DELETE FROM task_assignees WHERE task_id = " + task.getId());

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO task_assignees (task_id, member_id) VALUES (?, ?)")) {
                for (TeamMember m : task.getAssignees()) {
                    ps.setLong(1, task.getId());
                    ps.setLong(2, m.getId());
                    ps.executeUpdate();
                }
            }

            stmt.execute("DELETE FROM task_tags WHERE task_id = " + task.getId());

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO task_tags (task_id, tag_id) VALUES (?, ?)")) {
                for (Tag t : task.getTags()) {
                    ps.setLong(1, task.getId());
                    ps.setLong(2, t.getId());
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
            showError("Chyba pri ukladaní úlohy:\n" + e.getMessage());
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @FXML private void deleteTask() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Naozaj chcete vymazať túto úlohu?\nTáto akcia je nevratná.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Potvrdenie mazania");

        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try (Connection conn = new Database().getConnection()) {
                Statement stmt = conn.createStatement();
                stmt.execute("DELETE FROM task_assignees WHERE task_id = " + task.getId());
                stmt.execute("DELETE FROM task_tags WHERE task_id = " + task.getId());
                stmt.execute("DELETE FROM tasks WHERE id = " + task.getId());

                dataChanged = true;
                closeWindow();
            } catch (SQLException e) {
                showError("Chyba pri mazaní úlohy:\n" + e.getMessage());
            }
        }
    }

    @FXML private void closeWindow() {
        Stage stage = (Stage) tfTitle.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK)
                .showAndWait();
    }
}
