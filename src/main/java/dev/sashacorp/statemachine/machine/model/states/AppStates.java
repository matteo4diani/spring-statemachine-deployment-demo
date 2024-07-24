package dev.sashacorp.statemachine.machine.model.states;

public enum AppStates {
  READY(ApplicationStateTypes.INITIAL),
  DEPLOYING(ApplicationStateTypes.INTERMEDIATE),
  DEPLOYED(ApplicationStateTypes.INTERMEDIATE),
  DEPLOY_FAILED(ApplicationStateTypes.RECOVERABLE_ERROR),
  DEPLOY_INCOMPLETE(ApplicationStateTypes.RECOVERABLE_ERROR),
  DELETING(ApplicationStateTypes.INTERMEDIATE),
  DELETED(ApplicationStateTypes.TERMINAL);

  private final ApplicationStateTypes stateType;

  AppStates(ApplicationStateTypes stateType) {
    this.stateType = stateType;
  }

  public ApplicationStateTypes stateType() {
    return this.stateType;
  }
}