package dev.sashacorp.statemachine.machine.kubernetes;

import dev.sashacorp.statemachine.machine.kubernetes.model.V1Pod;
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
    return kubernetes.getOrDefault(namespace, Collections.emptyList());
  }

  public void putNamespacedComponent(String namespace, V1Pod pod) {
    if (!kubernetes.containsKey(namespace)) {
      kubernetes.put(namespace, new ArrayList<>());
    }

    kubernetes.get(namespace).add(pod);
    applicationEventPublisher.publishEvent(
      KubernetesEvent.buildEvent(namespace)
    );
  }

  public void putNamespacedComponents(String namespace, List<V1Pod> pods) {
    kubernetes.put(namespace, pods);
    applicationEventPublisher.publishEvent(
      KubernetesEvent.buildEvent(namespace)
    );
  }
}
