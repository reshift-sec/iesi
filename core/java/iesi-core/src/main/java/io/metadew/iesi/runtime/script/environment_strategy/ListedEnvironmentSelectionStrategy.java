package io.metadew.iesi.runtime.script.environment_strategy;

import java.util.List;

public class ListedEnvironmentSelectionStrategy implements EnvironmentSelectionStrategy {

    private final List<String> environments;

    public ListedEnvironmentSelectionStrategy(List<String> environments) {
        this.environments = environments;
    }

    @Override
    public boolean accepts(String environment) {
        return environments.contains(environment);
    }
}
