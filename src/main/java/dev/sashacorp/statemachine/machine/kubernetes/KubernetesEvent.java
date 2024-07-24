package dev.sashacorp.statemachine.machine.kubernetes;

public record KubernetesEvent(String namespace) {
  public static KubernetesEvent buildEvent(String namespace) {
    return new KubernetesEvent(namespace);
  }
}
