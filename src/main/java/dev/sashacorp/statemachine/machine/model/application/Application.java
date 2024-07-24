package dev.sashacorp.statemachine.machine.model.application;

import static dev.sashacorp.statemachine.machine.model.application.AppComponents.QUERY_SERVICE;
import static dev.sashacorp.statemachine.machine.model.application.AppComponents.RUNTIME_BUNDLE;
import static dev.sashacorp.statemachine.machine.model.application.AppComponents.UI;

import java.util.Set;

public record Application(String id, Set<AppComponents> components) {
  public static final String APPLICATION = "application";

  public Application(String id) {
    this(id, null);
  }

  @Override
  public Set<AppComponents> components() {
    return Set.of(RUNTIME_BUNDLE, QUERY_SERVICE, UI);
  }
}
