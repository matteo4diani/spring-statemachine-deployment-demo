package dev.sashacorp.statemachine.machine.model.application;

public enum AppComponents {
  RUNTIME_BUNDLE("runtime-bundle"),
  QUERY_SERVICE("query-service"),
  UI("ui");

  private final String id;

  AppComponents(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
