package dev.sashacorp.statemachine.machine.model.application;

public enum ApplicationComponents {
  RUNTIME_BUNDLE("runtime-bundle"),
  QUERY_SERVICE("query-service"),
  UI("ui");

  private final String id;

  ApplicationComponents(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
