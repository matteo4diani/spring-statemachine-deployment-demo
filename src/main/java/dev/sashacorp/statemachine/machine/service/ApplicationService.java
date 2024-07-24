package dev.sashacorp.statemachine.machine.service;

import dev.sashacorp.statemachine.machine.kubernetes.KubernetesClient;

public class ApplicationService {

  private final KubernetesClient kubernetesClient;

  public ApplicationService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  public boolean isFullyDeployed(String namespace) {
    return true;
  }

  public boolean isFullyDeleted(String namespace) {
    return true;
  }
}
