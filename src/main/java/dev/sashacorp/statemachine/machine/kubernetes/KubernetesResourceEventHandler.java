package dev.sashacorp.statemachine.machine.kubernetes;

import dev.sashacorp.statemachine.machine.model.events.ApplicationEvents;
import dev.sashacorp.statemachine.machine.service.ApplicationStateMachineService;

public class KubernetesResourceEventHandler implements ResourceEventHandler {

  private final ApplicationStateMachineService applicationStateMachineService;

  public KubernetesResourceEventHandler(
    ApplicationStateMachineService applicationStateMachineService
  ) {
    this.applicationStateMachineService = applicationStateMachineService;
  }

  @Override
  public void onAdd(V1Pod pod) {
    sendNamespaceStatusChangeEvent(pod);
  }

  @Override
  public void onUpdate(V1Pod oldPod, V1Pod newPod) {
    sendNamespaceStatusChangeEvent(newPod);
  }

  @Override
  public void onDelete(V1Pod pod, boolean deletedFinalStateUnknown) {
    sendNamespaceStatusChangeEvent(pod);
  }

  private void sendNamespaceStatusChangeEvent(V1Pod newPod) {
    this.applicationStateMachineService.sendEvent(
        newPod.namespace(),
        ApplicationEvents.NAMESPACE_STATUS_CHANGE
      );
  }
}
