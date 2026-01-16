package dev.antarux.skuska_projekt;

import javafx.beans.property.*;

public class Tag {
    private final LongProperty id = new SimpleLongProperty(this, "id");
    private final StringProperty name = new SimpleStringProperty(this, "name");
    private final StringProperty colorHex = new SimpleStringProperty(this, "colorHex");
    private final StringProperty description = new SimpleStringProperty(this, "description");

    // CONSTRUCTOR

    public Tag() {}
    public Tag(String name) {
        setName(name);
    }
    public Tag(String name, String colorHex) {
        setName(name);
        setColorHex(colorHex);
    }

    // METHODS

    public long getId() {
        return id.get();
    }
    public void setId(long id) { this.id.set(id); }
    public LongProperty idProperty() {
        return id;
    }

    public String getName() {
        return name.get();
    }
    public void setName(String name) {
        this.name.set(name);
    }
    public StringProperty nameProperty() {
        return name;
    }

    public String getColorHex() {
        return colorHex.get();
    }
    public void setColorHex(String colorHex) {
        this.colorHex.set(colorHex);
    }
    public StringProperty colorHexProperty() {
        return colorHex;
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

    @Override public String toString() {
        return getName();
    }
}