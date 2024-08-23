package dev.sashacorp.statemachine.machine.kubernetes;

import dev.sashacorp.statemachine.machine.model.events.AppEvents;
import dev.sashacorp.statemachine.machine.service.ApplicationStateMachineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;

@Slf4j(topic="⛴️ Kubernetes Resource Handler")
public class KubernetesListener {

  private final ApplicationStateMachineService applicationStateMachineService;

  public KubernetesListener(
    ApplicationStateMachineService applicationStateMachineService
  ) {
    this.applicationStateMachineService = applicationStateMachineService;
  }

  @EventListener
  public void handleKubernetesEvent(KubernetesEvent kubernetesEvent) {
    log.info("📬 Received Kubernetes event for namespace {}", kubernetesEvent.namespace());

    this.applicationStateMachineService.sendEvent(
        kubernetesEvent.namespace(),
        AppEvents.KUBERNETES_STATUS_CHANGE
      );
  }
}
