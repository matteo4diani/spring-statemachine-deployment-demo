package dev.sashacorp.statemachine.machine.kubernetes;

import dev.sashacorp.statemachine.machine.model.events.AppEvents;
import dev.sashacorp.statemachine.machine.service.ApplicationStateMachineService;
import org.springframework.context.event.EventListener;

public class KubernetesListener {

  private final ApplicationStateMachineService applicationStateMachineService;

  public KubernetesListener(
    ApplicationStateMachineService applicationStateMachineService
  ) {
    this.applicationStateMachineService = applicationStateMachineService;
  }

  @EventListener
  public void handleKubernetesEvent(KubernetesEvent kubernetesEvent) {
    this.applicationStateMachineService.sendEvent(
        kubernetesEvent.namespace(),
        AppEvents.NAMESPACE_STATUS_CHANGE
      );
  }
}
