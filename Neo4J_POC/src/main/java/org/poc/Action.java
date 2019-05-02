package org.poc;

import java.util.List;

public class Action {

    private String name;
    private String description;
    private String type;
    private String depends_on;
    private List<ActionParameter> parameters;

    public Action() {

    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDepends_on() {
        return depends_on;
    }

    public void setDepends_on(String depends_on) {
        this.depends_on = depends_on;
    }

    public List<ActionParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<ActionParameter> parameters) {
        this.parameters = parameters;
    }
}
