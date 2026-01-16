package dev.antarux.skuska_projekt;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController {
    @FXML private TableView<Task> tasksTable;

    @FXML private TableColumn<Task, Number>   colId;
    @FXML private TableColumn<Task, String>   colProject;
    @FXML private TableColumn<Task, String>   colTitle;
    @FXML private TableColumn<Task, Task.Priority> colPriority;
    @FXML private TableColumn<Task, Task.Status>   colStatus;
    @FXML private TableColumn<Task, String>   colAssignees;
    @FXML private TableColumn<Task, String>   colTags;
    @FXML private TableColumn<Task, String>   colCreated;
    @FXML private TableColumn<Task, String>   colUpdated;

    @FXML private ComboBox<Project> cbProjects;
    @FXML private ComboBox<Task.Status> cbStatus;
    @FXML private ComboBox<Task.Priority> cbPriority;
    @FXML private TextField tfSearch;

    @FXML private Label lblActiveProjectsCount;
    @FXML private Label lblOpenTasksCount;

    @FXML private Pagination pagination;

    private ObservableList<Task>    allTasks    = FXCollections.observableArrayList();
    private ObservableList<Task>    filteredTasks = FXCollections.observableArrayList();
    private ObservableList<Project> allProjects = FXCollections.observableArrayList();

    private static final int ROWS_PER_PAGE = 15;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @FXML public void initialize() {
        setupTableColumns();

        cbStatus.getItems().addAll(Task.Status.values());
        cbPriority.getItems().addAll(Task.Priority.values());

        loadProjects();
        loadTasks();
        filteredTasks.setAll(allTasks);
        updateStatistics();

        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            showPage(newIndex.intValue());
        });
        updatePagination();
        showPage(0);
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));

        colProject.setCellValueFactory(cellData -> {
            Task task = cellData.getValue();
            Project p = findProjectById(task.getProjectId());
            return new SimpleStringProperty(p != null ? p.toString() : "?");
        });

        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colAssignees.setCellValueFactory(cellData -> {
            List<String> names = new ArrayList<>();
            for (TeamMember member : cellData.getValue().getAssignees()) {
                names.add(member.getUsername());
            }
            return new SimpleStringProperty(String.join(", ", names));
        });

        colTags.setCellValueFactory(cellData -> {
            List<String> tags = new ArrayList<>();
            for (Tag tag : cellData.getValue().getTags()) {
                tags.add(tag.getName());
            }
            return new SimpleStringProperty(String.join(", ", tags));
        });

        colCreated.setCellValueFactory(cellData -> {
            LocalDateTime dt = cellData.getValue().getCreatedAt();
            return new SimpleStringProperty(dt != null ? dt.format(DATE_FORMATTER) : "");
        });

        colUpdated.setCellValueFactory(cellData -> {
            LocalDateTime dt = cellData.getValue().getUpdatedAt();
            return new SimpleStringProperty(dt != null ? dt.format(DATE_FORMATTER) : "");
        });
    }

    private Project findProjectById(long id) {
        for (Project p : allProjects) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    private void loadProjects() {
        allProjects.clear();
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
                allProjects.add(p);
            }

            cbProjects.getItems().clear();
            cbProjects.getItems().addAll(allProjects);
            cbProjects.getItems().add(0, null);

        } catch (SQLException e) {
            showError("Nepodarilo sa načítať projekty: " + e.getMessage());
        }
    }

    private void loadTasks() {
        allTasks.clear();
        String sql = """
            SELECT t.*
            FROM tasks t
            JOIN projects p ON t.project_id = p.id
            WHERE p.is_active = true
            ORDER BY t.created_at DESC
            """;

        try (Connection conn = new Database().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getLong("id"));
                task.setProjectId(rs.getLong("project_id"));
                task.setTitle(rs.getString("title"));
                task.setDescription(rs.getString("description"));
                task.setPriority(Task.Priority.valueOf(rs.getString("priority")));
                task.setStatus(Task.Status.valueOf(rs.getString("status")));
                task.setEstimatedHours(rs.getDouble("estimated_hours"));

                Timestamp ca = rs.getTimestamp("created_at");
                if (ca != null) task.setCreatedAt(ca.toLocalDateTime());

                Timestamp ua = rs.getTimestamp("updated_at");
                if (ua != null) task.setUpdatedAt(ua.toLocalDateTime());

                Timestamp cla = rs.getTimestamp("closed_at");
                if (cla != null) task.setClosedAt(cla.toLocalDateTime());

                task.setCreatedById(rs.getLong("created_by_id"));

                allTasks.add(task);
            }

            // Batch load assignees for all active tasks in one query
            Map<Long, List<TeamMember>> assigneesByTask = new HashMap<>();
            String sqlAssignees = """
                SELECT ta.task_id, tm.id, tm.username, tm.full_name, tm.created_at
                FROM task_assignees ta
                JOIN team_members tm ON ta.member_id = tm.id
                JOIN tasks t ON ta.task_id = t.id
                JOIN projects p ON t.project_id = p.id
                WHERE p.is_active = true
                """;
            try (Statement stmt = conn.createStatement();
                 ResultSet rsAss = stmt.executeQuery(sqlAssignees)) {
                while (rsAss.next()) {
                    long taskId = rsAss.getLong("task_id");
                    TeamMember m = new TeamMember();
                    m.setId(rsAss.getLong("id"));
                    m.setUsername(rsAss.getString("username"));
                    m.setFullName(rsAss.getString("full_name"));
                    Timestamp ts = rsAss.getTimestamp("created_at");
                    if (ts != null) m.setCreatedAt(ts.toLocalDateTime());

                    if (!assigneesByTask.containsKey(taskId)) {
                        assigneesByTask.put(taskId, new ArrayList<>());
                    }
                    assigneesByTask.get(taskId).add(m);
                }
            }

            // Batch load tags for all active tasks in one query
            Map<Long, List<Tag>> tagsByTask = new HashMap<>();
            String sqlTags = """
                SELECT tt.task_id, tg.id, tg.name, tg.color_hex, tg.description
                FROM task_tags tt
                JOIN tags tg ON tt.tag_id = tg.id
                JOIN tasks t ON tt.task_id = t.id
                JOIN projects p ON t.project_id = p.id
                WHERE p.is_active = true
                """;
            try (Statement stmt = conn.createStatement();
                 ResultSet rsTags = stmt.executeQuery(sqlTags)) {
                while (rsTags.next()) {
                    long taskId = rsTags.getLong("task_id");
                    Tag tag = new Tag();
                    tag.setId(rsTags.getLong("id"));
                    tag.setName(rsTags.getString("name"));
                    tag.setColorHex(rsTags.getString("color_hex"));
                    tag.setDescription(rsTags.getString("description"));

                    if (!tagsByTask.containsKey(taskId)) {
                        tagsByTask.put(taskId, new ArrayList<>());
                    }
                    tagsByTask.get(taskId).add(tag);
                }
            }

            // Assign the batched data to each task
            for (Task task : allTasks) {
                List<TeamMember> assignees = assigneesByTask.get(task.getId());
                if (assignees != null) {
                    task.getAssignees().addAll(assignees);
                }

                List<Tag> tags = tagsByTask.get(task.getId());
                if (tags != null) {
                    task.getTags().addAll(tags);
                }
            }

        } catch (SQLException e) {
            showError("Nepodarilo sa načítať úlohy: " + e.getMessage());
        }
    }

    @FXML private void applyFilters() {
        filteredTasks.clear();
        Project selectedProject = cbProjects.getValue();
        Task.Status selectedStatus = cbStatus.getValue();
        Task.Priority selectedPriority = cbPriority.getValue();
        String search = tfSearch.getText().toLowerCase().trim();

        for (Task t : allTasks) {
            boolean match = true;

            if (selectedProject != null && t.getProjectId() != selectedProject.getId()) {
                match = false;
            }
            if (selectedStatus != null && t.getStatus() != selectedStatus) {
                match = false;
            }
            if (selectedPriority != null && t.getPriority() != selectedPriority) {
                match = false;
            }
            if (!search.isEmpty()) {
                if (!t.getTitle().toLowerCase().contains(search) && !t.getDescription().toLowerCase().contains(search)) {
                    match = false;
                }
            }

            if (match) {
                filteredTasks.add(t);
            }
        }

        updatePagination();
        showPage(0);
    }

    @FXML private void clearFilters() {
        cbProjects.setValue(null);
        cbStatus.setValue(null);
        cbPriority.setValue(null);
        tfSearch.clear();
        applyFilters();
    }

    private void updatePagination() {
        int size = filteredTasks.size();
        int pageCount = (size + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE;
        pagination.setPageCount(pageCount > 0 ? pageCount : 1);
        pagination.setCurrentPageIndex(0);
    }

    private void showPage(int page) {
        int from = page * ROWS_PER_PAGE;
        int to = Math.min(from + ROWS_PER_PAGE, filteredTasks.size());
        ObservableList<Task> pageItems = FXCollections.observableArrayList();
        if (from < filteredTasks.size()) {
            for (int i = from; i < to; i++) {
                pageItems.add(filteredTasks.get(i));
            }
        }
        tasksTable.setItems(pageItems);

        int total = filteredTasks.size();
        int pageCount = pagination.getPageCount();
        //lblPageInfo.setText("Strana " + (page + 1) + " / " + pageCount + "   |   Zobrazených " + total + " záznamov");
    }

    private void updateStatistics() {
        lblActiveProjectsCount.setText(String.valueOf(allProjects.size()));

        int openTasks = 0;
        for (Task t : allTasks) {
            if (t.getStatus() != Task.Status.DONE && t.getStatus() != Task.Status.CANCELLED) {
                openTasks++;
            }
        }
        lblOpenTasksCount.setText(String.valueOf(openTasks));
    }

    @FXML private void handleNewTask() {
        openTaskWindow(null);
    }

    @FXML private void handleTableClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            Task selected = tasksTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openTaskWindow(selected);
            }
        }
    }

    private void openTaskWindow(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TaskEdit.fxml"));
            Parent root = loader.load();
            TaskEditController ctrl = loader.getController();
            ctrl.setMainController(this);
            ctrl.setTask(task);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(task == null ? "Nová úloha" : "Upraviť úlohu");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tasksTable.getScene().getWindow());
            stage.resizableProperty().setValue(false);
            stage.showAndWait();

            if (ctrl.isDataChanged()) {
                refreshData();
            }
        } catch (IOException e) {
            showError("Chyba pri otváraní okna: " + e.getMessage());
        }
    }

    private void openProjectWindow(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ProjectEdit.fxml"));
            Parent root = loader.load();
            ProjectEditController ctrl = loader.getController();
            ctrl.setMainController(this);
            ctrl.setProject(project);

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 580, 420));
            stage.setTitle(project == null ? "Nový projekt" : "Upraviť projekt");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tasksTable.getScene().getWindow());
            stage.resizableProperty().setValue(false);
            stage.showAndWait();

            if (ctrl.isDataChanged()) {
                refreshData();
            }
        } catch (IOException e) {
            showError("Chyba pri otváraní okna projektu: " + e.getMessage());
        }
    }

    @FXML private void handleNewProject() {
        openProjectWindow(null);
    }

    public void refreshData() {
        loadProjects();
        loadTasks();
        applyFilters();
        updateStatistics();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Chyba");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
