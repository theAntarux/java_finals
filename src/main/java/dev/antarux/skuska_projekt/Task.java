package dev.antarux.skuska_projekt;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Task {
    private final LongProperty id = new SimpleLongProperty(this, "id");
    private final LongProperty projectId = new SimpleLongProperty(this, "projectId");
    private final StringProperty title = new SimpleStringProperty(this, "title");
    private final StringProperty description = new SimpleStringProperty(this, "description");

    private final ObjectProperty<Priority> priority = new SimpleObjectProperty<>(this, "priority", Priority.MEDIUM);
    private final ObjectProperty<Status> status = new SimpleObjectProperty<>(this, "status", Status.TODO);

    private final DoubleProperty estimatedHours = new SimpleDoubleProperty(this, "estimatedHours");

    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>(this, "createdAt");
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>(this, "updatedAt");
    private final ObjectProperty<LocalDateTime> closedAt = new SimpleObjectProperty<>(this, "closedAt");

    private final LongProperty createdById = new SimpleLongProperty(this, "createdById");

    /*
        Yeaaah quick note about this one, this is the only way I could figure out on how to store actual given task data,
        basically saves my VPS usage, also since it is observable, it is auto compatible with the tableview component
     */
    private final ObservableList<TeamMember> assignees = FXCollections.observableArrayList();
    private final ObservableList<Tag> tags = FXCollections.observableArrayList();

    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    public enum Status {
        TODO, IN_PROGRESS, REVIEW, DONE, CANCELLED
    }

    // CONSTRUCTOR

    public Task() {}

    // METHODS

    public long getId() {
        return id.get();
    }
    public void setId(long id) {
        this.id.set(id);
    }
    public LongProperty idProperty() {
        return id;
    }

    public long getProjectId() {
        return projectId.get();
    }
    public void setProjectId(long projectId) {
        this.projectId.set(projectId);
    }
    public LongProperty projectIdProperty() {
        return projectId;
    }

    public String getTitle() {
        return title.get();
    }
    public void setTitle(String title) {
        this.title.set(title);
    }
    public StringProperty titleProperty() {
        return title;
    }

    public String getDescription() {
        return description.get();
    }
    public void setDescription(String description) {
        this.description.set(description);
    }
    public StringProperty descriptionProperty() {
        return description;
    }

    public Priority getPriority() {
        return priority.get();
    }
    public void setPriority(Priority priority) {
        this.priority.set(priority);
    }
    public ObjectProperty<Priority> priorityProperty() {
        return priority;
    }

    public Status getStatus() {
        return status.get();
    }
    public void setStatus(Status status) {
        this.status.set(status);
    }
    public ObjectProperty<Status> statusProperty() {
        return status;
    }

    public double getEstimatedHours() {
        return estimatedHours.get();
    }
    public void setEstimatedHours(double hours) {
        this.estimatedHours.set(hours);
    }
    public DoubleProperty estimatedHoursProperty() {
        return estimatedHours;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }
    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt.get();
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt.set(updatedAt);
    }
    public ObjectProperty<LocalDateTime> updatedAtProperty() {
        return updatedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt.get();
    }
    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt.set(closedAt);
    }
    public ObjectProperty<LocalDateTime> closedAtProperty() {
        return closedAt;
    }

    public long getCreatedById() {
        return createdById.get();
    }
    public void setCreatedById(long createdById) {
        this.createdById.set(createdById);
    }
    public LongProperty createdByIdProperty() {
        return createdById;
    }

    public ObservableList<TeamMember> getAssignees() {
        return assignees;
    }
    public ObservableList<Tag> getTags() {
        return tags;
    }

    @Override public String toString() {
        return "#" + getId() + " " + getTitle();
    }
}
