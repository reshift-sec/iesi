package io.metadew.iesi.runtime.script.environment_strategy;

public class DefaultEnvironmentSelectionStrategy implements EnvironmentSelectionStrategy {

    @Override
    public boolean accepts(String environment) {
        return true;
    }
}
