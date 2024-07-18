package dev.sashacorp.statemachine.web;

import static java.text.MessageFormat.format;

import dev.sashacorp.statemachine.machine.model.events.ApplicationEvents;
import dev.sashacorp.statemachine.machine.model.states.ApplicationStates;
import dev.sashacorp.statemachine.machine.service.CustomStateMachineService;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("applications")
public class ApplicationController {

  private final CustomStateMachineService stateMachineService;

  public ApplicationController(CustomStateMachineService stateMachineService) {
    this.stateMachineService = stateMachineService;
  }

  @GetMapping
  public ResponseEntity<Set<String>> findAllApplications() {
    return ResponseEntity.of(
      Optional.ofNullable(stateMachineService.getStateMachineIds())
    );
  }

  @GetMapping("/{id}")
  public ResponseEntity<String> getApplication(@PathVariable("id") String id) {
    final var stateMachine = stateMachineService
      .acquireExistingStateMachine(id)
      .orElseThrow();

    return getStringResponseEntity(
      "State machine with id [{0}] has status [{1}]",
      stateMachine
    );
  }

  @PostMapping("/{id}")
  public ResponseEntity<String> deployApplication(
    @PathVariable("id") String id
  ) {
    final var stateMachine = stateMachineService.acquireStateMachine(id);

    sendEvent(stateMachine, ApplicationEvents.DEPLOY);

    return getStringResponseEntity(
      "Created state machine with id [{0}] and status [{1}]",
      stateMachine
    );
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteApplication(
    @PathVariable("id") String id
  ) {
    final var stateMachine = stateMachineService
      .acquireExistingStateMachine(id)
      .orElseThrow();

    sendEvent(stateMachine, ApplicationEvents.DELETE);

    return getStringResponseEntity(
      "Deleting state machine with id [{0}] and status [{1}]",
      stateMachine
    );
  }

  @PostMapping("/events/{id}")
  public ResponseEntity<String> sendEvent(
    @PathVariable("id") String id,
    @RequestParam("event") ApplicationEvents event
  ) {
    final var stateMachine = stateMachineService
      .acquireExistingStateMachine(id)
      .orElseThrow();

    sendEvent(stateMachine, event);

    return getStringResponseEntity(
      "State machine with id [{0}] is now in status [{1}]",
      stateMachine
    );
  }

  @PostMapping("/clean/{id}")
  public ResponseEntity<String> cleanApplication(
    @PathVariable("id") String id
  ) {
    final var stateMachine = stateMachineService
      .acquireExistingStateMachine(id)
      .orElseThrow();

    if (ApplicationStates.DELETED.equals(stateMachine.getState().getId())) {
      stateMachineService.releaseStateMachine(id);
    }

    return ResponseEntity.of(
      Optional.of(
        format("Released state machine with id [{0}]", stateMachine.getId())
      )
    );
  }

  private void sendEvent(
    StateMachine<ApplicationStates, ApplicationEvents> stateMachine,
    ApplicationEvents event
  ) {
    stateMachine
      .sendEvent(Mono.just(MessageBuilder.withPayload(event).build()))
      .subscribe();
  }

  private ResponseEntity getStringResponseEntity(
    String messagePattern,
    StateMachine stateMachine
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
