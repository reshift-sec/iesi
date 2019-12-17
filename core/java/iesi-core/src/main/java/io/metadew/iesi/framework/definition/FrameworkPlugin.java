package io.metadew.iesi.framework.definition;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FrameworkPlugin {

    private String name;
    private Path path;

    //Constructors
    public FrameworkPlugin() {

    }

    public FrameworkPlugin(String name, String path) {
        this.name = name;
        this.path = Paths.get(path);
    }

    public FrameworkPlugin(String name, Path path) {
        this.name = name;
        this.path = path;
    }

    //Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

}