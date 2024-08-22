package dev.sashacorp.statemachine.machine.model.application;

import static dev.sashacorp.statemachine.machine.model.application.AppComponents.QUERY_SERVICE;
import static dev.sashacorp.statemachine.machine.model.application.AppComponents.RUNTIME_BUNDLE;
import static dev.sashacorp.statemachine.machine.model.application.AppComponents.UI;

import java.util.Set;

public class Application {

  private String id;
  private Set<AppComponents> components;

  public static final String APPLICATION = "application";

  public Application() {}

  public Application(String id) {
    this(id, null);
  }

  public Application(String id, Set<AppComponents> components) {
    this.id = id;
    this.components = components;
  }

  public Set<AppComponents> components() {
    return Set.of(RUNTIME_BUNDLE, QUERY_SERVICE, UI);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }

  public Set<AppComponents> getComponents() {
    return components;
  }

  public void setComponents(Set<AppComponents> components) {
    this.components = components;
  }
}
