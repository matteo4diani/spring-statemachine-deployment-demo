package dev.sashacorp.statemachine.machine.service;

import dev.sashacorp.statemachine.machine.kubernetes.KubernetesClient;
import dev.sashacorp.statemachine.machine.model.events.AppEvents;
import dev.sashacorp.statemachine.machine.model.states.AppStates;
import org.springframework.statemachine.StateMachine;

public class DeploymentService {

  private final ApplicationStateMachineService stateMachineService;

  public DeploymentService(ApplicationStateMachineService stateMachineService) {
    this.stateMachineService = stateMachineService;
  }

  public StateMachine<AppStates, AppEvents> deployApplication(String id) {
    final var stateMachine = this.stateMachineService.acquireStateMachine(id);

    this.stateMachineService.sendEvent(stateMachine.getId(), AppEvents.DEPLOY);

    return stateMachine;
  }

  public StateMachine<AppStates, AppEvents> undeployApplication(String id) {
    return this.stateMachineService.sendEvent(id, AppEvents.DELETE);
  }
}
