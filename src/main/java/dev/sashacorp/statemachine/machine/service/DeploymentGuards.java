package dev.sashacorp.statemachine.machine.service;

import dev.sashacorp.statemachine.machine.kubernetes.KubernetesClient;
import dev.sashacorp.statemachine.machine.kubernetes.model.PodStatus;
import dev.sashacorp.statemachine.machine.kubernetes.model.V1Pod;
import dev.sashacorp.statemachine.machine.model.application.AppComponents;
import dev.sashacorp.statemachine.machine.model.application.Application;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.statemachine.ExtendedState;

public class DeploymentGuards {

  private final KubernetesClient kubernetesClient;

  public DeploymentGuards(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  public boolean isFullyDeployed(ExtendedState extendedState) {
    final Application app = extendedState.get(
      Application.APPLICATION,
      Application.class
    );
    final List<V1Pod> pods = kubernetesClient.getNamespacedComponents(app.id());

    final boolean hasAnyPodsPending = pods
      .stream()
      .anyMatch(pod -> PodStatus.PENDING.equals(pod.status()));

    if (hasAnyPodsPending) {
      return false;
    }

    final Set<AppComponents> components = pods
      .stream()
      .map(V1Pod::type)
      .collect(Collectors.toSet());

    final boolean hasAllPodsRunning = pods
      .stream()
      .allMatch(pod -> PodStatus.RUNNING.equals(pod.status()));

    return hasAllPodsRunning && components.containsAll(app.components());
  }

  public boolean isFullyDeleted(ExtendedState extendedState) {
    return true;
  }
}
