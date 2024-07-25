package dev.sashacorp.statemachine.machine.service;

import dev.sashacorp.statemachine.machine.kubernetes.KubernetesClient;
import dev.sashacorp.statemachine.machine.kubernetes.model.PodStatus;
import dev.sashacorp.statemachine.machine.kubernetes.model.V1Pod;
import dev.sashacorp.statemachine.machine.model.application.AppComponents;
import dev.sashacorp.statemachine.machine.model.application.Application;
import dev.sashacorp.statemachine.machine.model.events.AppEvents;
import dev.sashacorp.statemachine.machine.model.states.AppStates;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.statemachine.StateContext;

public class DeploymentGuards {

  private final KubernetesClient kubernetesClient;

  public DeploymentGuards(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  public boolean isFullyDeployedGuard(
    StateContext<AppStates, AppEvents> context
  ) {
    final Application app = getAppFromContext(context);

    final List<V1Pod> pods =
      this.kubernetesClient.getNamespacedComponents(app.id());

    final boolean isAnyPodPending = pods
      .stream()
      .anyMatch(pod -> PodStatus.PENDING.equals(pod.status()));

    if (isAnyPodPending) {
      return false;
    }

    final Set<AppComponents> components = pods
      .stream()
      .map(V1Pod::type)
      .collect(Collectors.toSet());

    final boolean isEveryPodRunning = pods
      .stream()
      .allMatch(pod -> PodStatus.RUNNING.equals(pod.status()));

    return isEveryPodRunning && components.containsAll(app.components());
  }

  public boolean isFullyDeletedGuard(
    StateContext<AppStates, AppEvents> context
  ) {
    final Application app = getAppFromContext(context);

    final List<V1Pod> pods =
      this.kubernetesClient.getNamespacedComponents(app.id());

    return pods.isEmpty();
  }

  public boolean isNotFullyDeployedGuard(
    StateContext<AppStates, AppEvents> context
  ) {
    return !isFullyDeployedGuard(context);
  }

  private Application getAppFromContext(
    StateContext<AppStates, AppEvents> context
  ) {
    return context
      .getExtendedState()
      .get(Application.APPLICATION, Application.class);
  }
}
