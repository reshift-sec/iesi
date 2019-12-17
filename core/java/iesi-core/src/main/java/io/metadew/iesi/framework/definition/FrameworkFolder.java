package io.metadew.iesi.framework.definition;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FrameworkFolder {

    private String name;
    private String path;
    private Path absolutePath;
    private String label;
    private String description;
    private String permissions;

    //Constructors
    public FrameworkFolder() {

    }

    public FrameworkFolder(String name, String path, String absolutePath, String label, String description, String permissions) {
        this.name = name;
        this.path = path;
        this.absolutePath = Paths.get(absolutePath);
        this.label = label;
        this.description = description;
        this.permissions = permissions;
    }

    //Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public Path getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = Paths.get(absolutePath);
    }
    public void setAbsolutePath(Path absolutePath) {
        this.absolutePath = absolutePath;
    }


}