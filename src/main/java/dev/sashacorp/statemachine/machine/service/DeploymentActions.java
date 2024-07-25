package dev.sashacorp.statemachine.machine.service;

import dev.sashacorp.statemachine.machine.configuration.DeploymentProperties;
import dev.sashacorp.statemachine.machine.kubernetes.KubernetesClient;
import dev.sashacorp.statemachine.machine.kubernetes.model.PodStatus;
import dev.sashacorp.statemachine.machine.kubernetes.model.V1Pod;
import dev.sashacorp.statemachine.machine.model.application.AppComponents;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeploymentActions {

  private final KubernetesClient kubernetesClient;
  private final DeploymentProperties deploymentProperties;

  public DeploymentActions(
    KubernetesClient kubernetesClient,
    DeploymentProperties deploymentProperties
  ) {
    this.kubernetesClient = kubernetesClient;
    this.deploymentProperties = deploymentProperties;
  }

  public void deployAction(String id) {
    CompletableFuture.runAsync(() -> deploy(id));
  }

  public void deleteAction(String id) {
    CompletableFuture.runAsync(() -> delete(id));
  }

  private void deploy(String id) {
    log.info("Running deployment action for app [{}]", id);

    try {
      var runtimeBundle = V1Pod.newRuntimeBundle(id);
      var queryService = V1Pod.newQueryService(id);
      var ui = V1Pod.newUi(id);

      addComponentAndSleepForDelay(id, runtimeBundle);

      addComponentAndSleepForDelay(id, queryService);

      addComponentAndSleepForDelay(id, ui);

      sleepForDelay();

      runtimeBundle = runtimeBundle.updateStatus(PodStatus.RUNNING);
      queryService = queryService.updateStatus(PodStatus.RUNNING);
      ui = ui.updateStatus(PodStatus.RUNNING);

      this.kubernetesClient.putNamespacedComponents(
          id,
          List.of(runtimeBundle, queryService, ui)
        );
    } catch (InterruptedException cause) {
      throw new RuntimeException(cause);
    }

    log.info("Completed deployment action for app [{}]", id);
  }

  private void delete(String id) {
    log.info("Running deletion action for app [{}]", id);

    try {
      removeComponentAndSleepForDelay(id, AppComponents.UI);

      removeComponentAndSleepForDelay(id, AppComponents.QUERY_SERVICE);

      removeComponentAndSleepForDelay(id, AppComponents.RUNTIME_BUNDLE);
    } catch (InterruptedException cause) {
      throw new RuntimeException(cause);
    }

    log.info("Completed deletion action for app [{}]", id);
  }

  private void addComponent(String id, V1Pod runtimeBundle) {
    this.kubernetesClient.putNamespacedComponent(id, runtimeBundle);
  }

  private void addComponentAndSleepForDelay(String id, V1Pod runtimeBundle)
    throws InterruptedException {
    sleepForDelay();

    addComponent(id, runtimeBundle);
  }

  private void removeComponent(String id, AppComponents component) {
    this.kubernetesClient.removeNamespacedComponent(id, component);
  }

  private void removeComponentAndSleepForDelay(
    String id,
    AppComponents component
  ) throws InterruptedException {
    sleepForDelay();

    removeComponent(id, component);
  }

  private void sleepForDelay() throws InterruptedException {
    Thread.sleep(this.deploymentProperties.getDelay());
  }
}
