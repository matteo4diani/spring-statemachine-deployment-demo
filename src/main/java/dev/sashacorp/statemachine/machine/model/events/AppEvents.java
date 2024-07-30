package dev.sashacorp.statemachine.machine.model.events;

public enum AppEvents {
  DEPLOY(ApplicationEventTypes.USER_COMMAND),
  DELETE(ApplicationEventTypes.USER_COMMAND),
  KUBERNETES_STATUS_CHANGE(ApplicationEventTypes.KUBERNETES_EVENT);

  private final ApplicationEventTypes eventType;

  AppEvents(ApplicationEventTypes eventType) {
    this.eventType = eventType;
  }

  public boolean isKubernetesEvent() {
    return ApplicationEventTypes.KUBERNETES_EVENT.equals(this.eventType);
  }

  public boolean isUserCommand() {
    return ApplicationEventTypes.USER_COMMAND.equals(this.eventType);
  }

  public ApplicationEventTypes eventType() {
    return this.eventType;
  }
}
