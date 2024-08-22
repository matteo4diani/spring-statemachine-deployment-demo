package dev.sashacorp.statemachine.machine.service;

import dev.sashacorp.statemachine.machine.model.application.Application;
import dev.sashacorp.statemachine.machine.model.events.AppEvents;
import dev.sashacorp.statemachine.machine.model.states.AppStates;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachineException;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.data.jpa.JpaStateMachineRepository;
import reactor.core.publisher.Mono;

@Slf4j
public class ApplicationStateMachineService
  implements CustomStateMachineService {

  @Autowired
  private StateMachinePersist<AppStates, AppEvents, String> stateMachinePersist;

  @Autowired
  private JpaStateMachineRepository jpaStateMachineRepository;

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
    log.info("Getting new machine from factory with id " + machineId);
    StateMachine<AppStates, AppEvents> stateMachine = stateMachineFactory.getStateMachine(
      machineId
    );
    try {
      StateMachineContext<AppStates, AppEvents> stateMachineContext = stateMachinePersist.read(
        machineId
      );
      stateMachine.stopReactively().block();
      stateMachine
        .getStateMachineAccessor()
        .doWithAllRegions(function ->
          function.resetStateMachineReactively(stateMachineContext).block()
        );
    } catch (Exception e) {
      log.error("Error handling context", e);
      throw new StateMachineException("Unable to read context from store", e);
    }
    stateMachines.put(machineId, stateMachine);

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
    final var stateMachine = this.acquireStateMachine(machineId);

    if (AppStates.DELETED.equals(stateMachine.getState().getId())) {
      this.stateMachines.remove(machineId);
      stateMachine.stopReactively().block();
      this.jpaStateMachineRepository.deleteById(machineId);
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
  public Set<String> getStateMachineIds() {
    return this.stateMachines.keySet();
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
    return this.stateMachines.get(machineId);
  }

  private void putStateMachine(String machineId) {
    this.stateMachines.put(
        machineId,
        this.stateMachineFactory.getStateMachine(machineId)
      );
  }

  private boolean doesNotContainStateMachine(String machineId) {
    return !this.stateMachines.containsKey(machineId);
  }

  private boolean containsNullStateMachine(String machineId) {
    return Objects.isNull(getStateMachine(machineId));
  }
}
