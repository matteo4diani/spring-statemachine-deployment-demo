package dev.sashacorp.statemachine.machine.kubernetes;

import dev.sashacorp.statemachine.machine.kubernetes.model.PodStatus;
import dev.sashacorp.statemachine.machine.kubernetes.model.V1Pod;
import dev.sashacorp.statemachine.machine.model.application.AppComponents;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.context.ApplicationEventPublisher;

public class KubernetesClient {

  private final ConcurrentMap<String, List<V1Pod>> kubernetes = new ConcurrentHashMap<>();
  private final ApplicationEventPublisher applicationEventPublisher;

  public KubernetesClient(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  public List<V1Pod> getNamespacedComponents(String namespace) {
    return this.kubernetes.getOrDefault(namespace, Collections.emptyList());
  }

  public void putNamespacedComponent(String namespace, V1Pod pod) {
    if (!this.kubernetes.containsKey(namespace)) {
      this.kubernetes.put(namespace, new ArrayList<>());
    }

    this.kubernetes.get(namespace).add(pod);

    this.applicationEventPublisher.publishEvent(
        KubernetesEvent.buildEvent(namespace)
      );
  }

  public void putNamespacedComponents(String namespace, List<V1Pod> pods) {
    this.kubernetes.put(namespace, new ArrayList<>(pods));

    this.applicationEventPublisher.publishEvent(
        KubernetesEvent.buildEvent(namespace)
      );
  }

  public void removeNamespacedComponent(
    String namespace,
    AppComponents component
  ) {
    if (!this.kubernetes.containsKey(namespace)) {
      return;
    }

    this.kubernetes.get(namespace)
      .removeIf(pod -> component.equals(pod.type()));

    this.applicationEventPublisher.publishEvent(
        KubernetesEvent.buildEvent(namespace)
      );
  }

  public void removeAllNamespacedComponents(String id) {
    this.kubernetes.get(id).clear();
    this.applicationEventPublisher.publishEvent(
            KubernetesEvent.buildEvent(id)
    );
  }

  public void simulatePreExistingKubernetesDeployment(String machineId) {
    var runtimeBundle = V1Pod.newRuntimeBundle(machineId);
    var queryService = V1Pod.newQueryService(machineId);
    var ui = V1Pod.newUi(machineId);

    runtimeBundle = runtimeBundle.updateStatus(PodStatus.RUNNING);
    queryService = queryService.updateStatus(PodStatus.RUNNING);
    ui = ui.updateStatus(PodStatus.RUNNING);

    this.kubernetes.put(
            machineId,
            List.of(runtimeBundle, queryService, ui)
    );
  }

  public boolean contains(String namespace) {
    return this.kubernetes.containsKey(namespace);
  }
}
