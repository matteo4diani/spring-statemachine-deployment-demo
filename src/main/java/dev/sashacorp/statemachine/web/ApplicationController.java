package dev.sashacorp.statemachine.web;

import static java.text.MessageFormat.format;

import dev.sashacorp.statemachine.machine.model.events.ApplicationEvents;
import dev.sashacorp.statemachine.machine.model.states.ApplicationStates;
import dev.sashacorp.statemachine.machine.service.CustomStateMachineService;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
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
    return ResponseEntity.of(
      Optional.of(
        format(
          "State machine with id [{0}] has status [{1}]",
          stateMachine.getId(),
          stateMachine.getState().getId()
        )
      )
    );
  }

  @PostMapping("/{id}")
  public ResponseEntity<String> deployApplication(
    @PathVariable("id") String id
  ) {
    final var stateMachine = stateMachineService.acquireStateMachine(id);
    stateMachine
      .sendEvent(
        Mono.just(MessageBuilder.withPayload(ApplicationEvents.DEPLOY).build())
      )
      .subscribe();
    return ResponseEntity.of(
      Optional.of(
        format(
          "Created state machine with id [{0}] and status [{1}]",
          stateMachine.getId(),
          stateMachine.getState().getId()
        )
      )
    );
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteApplication(
    @PathVariable("id") String id
  ) {
    final var stateMachine = stateMachineService
      .acquireExistingStateMachine(id)
      .orElseThrow();
    stateMachine
      .sendEvent(
        Mono.just(MessageBuilder.withPayload(ApplicationEvents.DELETE).build())
      )
      .subscribe();
    return ResponseEntity.of(
      Optional.of(
        format(
          "Deleting state machine with id [{0}] and status [{1}]",
          stateMachine.getId(),
          stateMachine.getState().getId()
        )
      )
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
    stateMachine
      .sendEvent(Mono.just(MessageBuilder.withPayload(event).build()))
      .subscribe();
    return ResponseEntity.of(
      Optional.of(
        format(
          "State machine with id [{0}] is now in status [{1}]",
          stateMachine.getId(),
          stateMachine.getState().getId()
        )
      )
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
}
