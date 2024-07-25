package dev.sashacorp.statemachine.machine.service;

import dev.sashacorp.statemachine.machine.kubernetes.KubernetesClient;
import dev.sashacorp.statemachine.machine.kubernetes.model.PodStatus;
import dev.sashacorp.statemachine.machine.kubernetes.model.V1Pod;
import dev.sashacorp.statemachine.machine.model.application.AppComponents;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeploymentActions {

  public static final int DELAY = 100;
  private final KubernetesClient kubernetesClient;

  public DeploymentActions(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  public void deployAction(String id) {
    log.info("Running deployment action for app [{}]", id);

    try {
      var runtimeBundle = V1Pod.newRuntimeBundle(id);
      var queryService = V1Pod.newQueryService(id);
      var ui = V1Pod.newUi(id);

      Thread.sleep(DELAY);

      kubernetesClient.putNamespacedComponent(id, runtimeBundle);

      Thread.sleep(DELAY);

      kubernetesClient.putNamespacedComponent(id, queryService);

      Thread.sleep(DELAY);

      kubernetesClient.putNamespacedComponent(id, ui);

      Thread.sleep(DELAY);

      runtimeBundle = runtimeBundle.updateStatus(PodStatus.RUNNING);
      queryService = queryService.updateStatus(PodStatus.RUNNING);
      ui = ui.updateStatus(PodStatus.RUNNING);

      kubernetesClient.putNamespacedComponents(
        id,
        List.of(runtimeBundle, queryService, ui)
      );
    } catch (InterruptedException cause) {
      throw new RuntimeException(cause);
    }

    log.info("Completed deployment action for app [{}]", id);
  }

  public void deleteAction(String id) {
    log.info("Running deletion action for app [{}]", id);

    try {
      Thread.sleep(DELAY);

      kubernetesClient.removeNamespacedComponent(id, AppComponents.UI);

      Thread.sleep(DELAY);

      kubernetesClient.removeNamespacedComponent(
        id,
        AppComponents.QUERY_SERVICE
      );

      Thread.sleep(DELAY);

      kubernetesClient.removeNamespacedComponent(
        id,
        AppComponents.RUNTIME_BUNDLE
      );
    } catch (InterruptedException cause) {
      throw new RuntimeException(cause);
    }

    log.info("Completed deletion action for app [{}]", id);
  }
}
