package io.metadew.iesi.framework.definition;

public class FrameworkPlugin {

    private String name;
    private String path;

    //Constructors
    public FrameworkPlugin() {

    }

    public FrameworkPlugin(String name, String path) {
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}