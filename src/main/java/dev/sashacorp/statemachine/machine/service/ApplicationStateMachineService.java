package dev.sashacorp.statemachine.machine.service;

import dev.sashacorp.statemachine.machine.model.application.Application;
import dev.sashacorp.statemachine.machine.model.events.AppEvents;
import dev.sashacorp.statemachine.machine.model.states.AppStates;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import reactor.core.publisher.Mono;

@Slf4j
public class ApplicationStateMachineService
  implements CustomStateMachineService {

  private final StateMachineFactory<AppStates, AppEvents> stateMachineFactory;

  private final ConcurrentHashMap<String, StateMachine<AppStates, AppEvents>> stateMachines = new ConcurrentHashMap<>();

  public ApplicationStateMachineService(
    StateMachineFactory<AppStates, AppEvents> stateMachineFactory
  ) {
    this.stateMachineFactory = stateMachineFactory;
  }

  @Override
  public StateMachine<AppStates, AppEvents> acquireStateMachine(
    String machineId
  ) {
    if (doesNotContainStateMachine(machineId)) {
      putStateMachine(machineId);
    }

    if (containsNullStateMachine(machineId)) {
      putStateMachine(machineId);
    }

    getStateMachine(machineId).startReactively().block();

    return getStateMachine(machineId);
  }

  @Override
  public Optional<StateMachine<AppStates, AppEvents>> acquireExistingStateMachine(
    String machineId
  ) {
    if (doesNotContainStateMachine(machineId)) {
      return Optional.empty();
    }

    if (containsNullStateMachine(machineId)) {
      return Optional.empty();
    }

    return Optional.ofNullable(getStateMachine(machineId));
  }

  @Override
  public StateMachine<AppStates, AppEvents> acquireStateMachine(
    String machineId,
    boolean start
  ) {
    return acquireStateMachine(machineId);
  }

  @Override
  public void releaseStateMachine(String machineId) {
    if (doesNotContainStateMachine(machineId)) {
      return;
    }

    final var stateMachine = getStateMachine(machineId);

    if (Objects.isNull(stateMachine)) {
      stateMachines.remove(machineId);
      return;
    }

    if (AppStates.DELETED.equals(stateMachine.getState().getId())) {
      stateMachines.remove(machineId);
      stateMachine.stopReactively().block();
    }
  }

  @Override
  public void releaseStateMachine(String machineId, boolean stop) {
    releaseStateMachine(machineId);
  }

  @Override
  public void setApplication(String machineId) {
    final var application = new Application(machineId);
    final var stateMachine = acquireExistingStateMachine(machineId)
      .orElseThrow();

    stateMachine
      .getExtendedState()
      .getVariables()
      .put(Application.APPLICATION, application);
  }

  @Override
  public Application getApplication(String machineId) {
    final var stateMachine = acquireExistingStateMachine(machineId)
      .orElseThrow();

    return stateMachine
      .getExtendedState()
      .get(Application.APPLICATION, Application.class);
  }

  @Override
  public Set<String> getStateMachineIds() {
    return stateMachines.keySet();
  }

  @Override
  public StateMachine<AppStates, AppEvents> sendEvent(
    String machineId,
    AppEvents event
  ) {
    final var stateMachine = acquireExistingStateMachine(machineId)
      .orElseThrow();

    stateMachine
      .sendEvent(Mono.just(MessageBuilder.withPayload(event).build()))
      .subscribe();

    log.info("Event [{}] sent to machine [{}]", event, machineId);

    return stateMachine;
  }

  @Override
  public StateMachine<AppStates, AppEvents> sendEvents(
    String machineId,
    AppEvents... events
  ) {
    final var stateMachine = acquireExistingStateMachine(machineId)
      .orElseThrow();

    for (AppEvents event : events) {
      sendEvent(machineId, event);
    }

    log.info("Events {} sent to machine [{}]", events, machineId);

    return stateMachine;
  }

  private StateMachine<AppStates, AppEvents> getStateMachine(String machineId) {
    return stateMachines.get(machineId);
  }

  private void putStateMachine(String machineId) {
    stateMachines.put(
      machineId,
      stateMachineFactory.getStateMachine(machineId)
    );
  }

  private boolean doesNotContainStateMachine(String machineId) {
    return !stateMachines.containsKey(machineId);
  }

  private boolean containsNullStateMachine(String machineId) {
    return Objects.isNull(getStateMachine(machineId));
  }
}
