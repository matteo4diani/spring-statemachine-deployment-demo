package dev.sashacorp.statemachine.machine.model.application;

import static dev.sashacorp.statemachine.machine.model.application.ApplicationComponents.QUERY_SERVICE;
import static dev.sashacorp.statemachine.machine.model.application.ApplicationComponents.RUNTIME_BUNDLE;
import static dev.sashacorp.statemachine.machine.model.application.ApplicationComponents.UI;

import java.util.Set;

public record Application(String name, Set<ApplicationComponents> components) {
  public Set<ApplicationComponents> components() {
    return Set.of(RUNTIME_BUNDLE, QUERY_SERVICE, UI);
  }
}
