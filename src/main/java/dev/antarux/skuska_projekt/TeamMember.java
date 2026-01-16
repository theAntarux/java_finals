package dev.antarux.skuska_projekt;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class TeamMember {
    private final LongProperty id = new SimpleLongProperty(this, "id");
    private final StringProperty username = new SimpleStringProperty(this, "username");
    private final StringProperty fullName = new SimpleStringProperty(this, "fullName");
    private final StringProperty email = new SimpleStringProperty(this, "email");
    private final StringProperty role = new SimpleStringProperty(this, "role");
    private final BooleanProperty active = new SimpleBooleanProperty(this, "active", true);
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>(this, "createdAt");

    // CONSTRUCTOR

    public TeamMember() {}
    public TeamMember(String username, String fullName, String email) {
        setUsername(username);
        setFullName(fullName);
        setEmail(email);
        setActive(true);
    }

    // METHODS

    public long getId() { return id.get(); }
    public void setId(long id) { this.id.set(id); }
    public LongProperty idProperty() { return id; }

    public String getUsername() { return username.get(); }
    public void setUsername(String username) { this.username.set(username); }
    public StringProperty usernameProperty() { return username; }

    public String getFullName() { return fullName.get(); }
    public void setFullName(String fullName) { this.fullName.set(fullName); }
    public StringProperty fullNameProperty() { return fullName; }

    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }
    public StringProperty emailProperty() { return email; }

    public String getRole() { return role.get(); }
    public void setRole(String role) { this.role.set(role); }
    public StringProperty roleProperty() { return role; }

    public boolean isActive() { return active.get(); }
    public void setActive(boolean active) { this.active.set(active); }
    public BooleanProperty activeProperty() { return active; }

    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }

    @Override public String toString() {
        return getUsername() + " (" + getFullName() + ")";
    }
}
