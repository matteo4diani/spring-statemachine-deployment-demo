package dev.sashacorp.statemachine.web;

import static java.text.MessageFormat.format;

import dev.sashacorp.statemachine.machine.model.events.AppEvents;
import dev.sashacorp.statemachine.machine.model.states.AppStates;
import dev.sashacorp.statemachine.machine.service.ApplicationStateMachineService;
import dev.sashacorp.statemachine.machine.service.CustomStateMachineService;
import dev.sashacorp.statemachine.machine.service.DeploymentService;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("applications")
public class ApplicationController {

  private final ApplicationStateMachineService stateMachineService;

  private final DeploymentService deploymentService;

  public ApplicationController(
    ApplicationStateMachineService stateMachineService,
    DeploymentService deploymentService
  ) {
    this.stateMachineService = stateMachineService;
    this.deploymentService = deploymentService;
  }

  @GetMapping("/{id}")
  public ResponseEntity<String> getApplication(@PathVariable("id") String id) {
    return getStringResponseEntity(
      "State machine with id [{0}] has status [{1}]",
      this.stateMachineService.acquireStateMachine(id)
    );
  }

  @PostMapping("/{id}")
  public ResponseEntity<String> deployApplication(
    @PathVariable("id") String id
  ) {
    return getStringResponseEntity(
      "Created state machine with id [{0}] and status [{1}]",
      this.deploymentService.deployApplication(id)
    );
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteApplication(
    @PathVariable("id") String id
  ) {
    return getStringResponseEntity(
      "Deleting state machine with id [{0}] and status [{1}]",
      this.deploymentService.undeployApplication(id)
    );
  }

  @PostMapping("/events/{id}")
  public ResponseEntity<String> sendEvent(
    @PathVariable("id") String id,
    @RequestParam("event") AppEvents event
  ) {
    return getStringResponseEntity(
      "State machine with id [{0}] is now in status [{1}]",
      this.stateMachineService.sendEvent(id, event)
    );
  }

  @PostMapping("/clean/{id}")
  public ResponseEntity<String> cleanApplication(
    @PathVariable("id") String id
  ) {
    this.stateMachineService.releaseStateMachine(id);

    return ResponseEntity.of(
      Optional.of(format("Released state machine with id [{0}]", id))
    );
  }

  private ResponseEntity<String> getStringResponseEntity(
    String messagePattern,
    StateMachine<AppStates, AppEvents> stateMachine
  ) {
    return ResponseEntity.of(
      Optional.of(
        format(
          messagePattern,
          stateMachine.getId(),
          stateMachine.getState().getId()
        )
      )
    );
  }
}
