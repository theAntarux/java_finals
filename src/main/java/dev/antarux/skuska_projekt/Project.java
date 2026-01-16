package dev.antarux.skuska_projekt;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Project {
    private final LongProperty id = new SimpleLongProperty(this, "id");
    private final StringProperty name = new SimpleStringProperty(this, "name");
    private final StringProperty description = new SimpleStringProperty(this, "description");
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>(this, "createdAt");
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>(this, "updatedAt");
    private final BooleanProperty active = new SimpleBooleanProperty(this, "active", true);
    private final StringProperty projectKey = new SimpleStringProperty(this, "projectKey");

    // CONSTRUCTORS
    public Project() {}
    public Project(String name, String projectKey) {
        setName(name);
        setProjectKey(projectKey);
        setActive(true);
    }

    // METHODS

    public long getId() { return id.get(); }
    public void setId(long id) { this.id.set(id); }
    public LongProperty idProperty() { return id; }

    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    public StringProperty descriptionProperty() { return description; }

    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt.set(updatedAt); }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }

    public boolean isActive() { return active.get(); }
    public void setActive(boolean active) { this.active.set(active); }
    public BooleanProperty activeProperty() { return active; }

    public String getProjectKey() { return projectKey.get(); }
    public void setProjectKey(String projectKey) { this.projectKey.set(projectKey);}
    public StringProperty projectKeyProperty() { return projectKey; }

    @Override public String toString() {
        return getProjectKey() + " - " + getName();
    }
}
