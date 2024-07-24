package dev.sashacorp.statemachine.machine.kubernetes.model;

import dev.sashacorp.statemachine.machine.model.application.AppComponents;

public record V1Pod(String namespace, AppComponents type, PodStatus status) {
  public V1Pod updateStatus(PodStatus status) {
    return new V1Pod(this.namespace(), this.type(), status);
  }

  public static V1Pod newRuntimeBundle(String namespace) {
    return new V1Pod(
      namespace,
      AppComponents.RUNTIME_BUNDLE,
      PodStatus.PENDING
    );
  }

  public static V1Pod newQueryService(String namespace) {
    return new V1Pod(namespace, AppComponents.QUERY_SERVICE, PodStatus.PENDING);
  }

  public static V1Pod newUi(String namespace) {
    return new V1Pod(namespace, AppComponents.UI, PodStatus.PENDING);
  }
}
