package dev.sashacorp.statemachine.machine.service;

import dev.sashacorp.statemachine.machine.kubernetes.KubernetesClient;
import dev.sashacorp.statemachine.machine.model.events.AppEvents;
import dev.sashacorp.statemachine.machine.model.states.AppStates;
import org.springframework.statemachine.StateMachine;

public class DeploymentService {

  private final ApplicationStateMachineService stateMachineService;
  private final KubernetesClient kubernetesClient;

  public DeploymentService(
    ApplicationStateMachineService stateMachineService,
    KubernetesClient kubernetesClient
  ) {
    this.stateMachineService = stateMachineService;
    this.kubernetesClient = kubernetesClient;
  }

  public StateMachine<AppStates, AppEvents> deployApplication(String id) {
    final var stateMachine = stateMachineService.acquireStateMachine(id);

    stateMachineService.setApplication(id);

    stateMachineService.sendEvent(stateMachine.getId(), AppEvents.DEPLOY);

    return stateMachine;
  }

  public StateMachine<AppStates, AppEvents> undeployApplication(String id) {
    return stateMachineService.sendEvent(id, AppEvents.DELETE);
  }
}
