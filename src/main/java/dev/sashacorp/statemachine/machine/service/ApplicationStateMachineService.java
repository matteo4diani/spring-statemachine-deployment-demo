package dev.sashacorp.statemachine.machine.service;

import dev.sashacorp.statemachine.machine.model.events.ApplicationEvents;
import dev.sashacorp.statemachine.machine.model.states.ApplicationStates;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

public class ApplicationStateMachineService
  implements CustomStateMachineService {

  private final StateMachineFactory<ApplicationStates, ApplicationEvents> stateMachineFactory;

  private final ConcurrentHashMap<String, StateMachine<ApplicationStates, ApplicationEvents>> stateMachines = new ConcurrentHashMap<>();

  public ApplicationStateMachineService(
    StateMachineFactory<ApplicationStates, ApplicationEvents> stateMachineFactory
  ) {
    this.stateMachineFactory = stateMachineFactory;
  }

  @Override
  public StateMachine<ApplicationStates, ApplicationEvents> acquireStateMachine(
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
  public Optional<StateMachine<ApplicationStates, ApplicationEvents>> acquireExistingStateMachine(
    String machineId
  ) {
    if (doesNotContainStateMachine(machineId)) return Optional.empty();

    if (containsNullStateMachine(machineId)) return Optional.empty();

    return Optional.ofNullable(getStateMachine(machineId));
  }

  @Override
  public StateMachine<ApplicationStates, ApplicationEvents> acquireStateMachine(
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

    final var stateMachine = stateMachines.remove(machineId);

    if (Objects.isNull(stateMachine)) return;

    stateMachine.stopReactively().block();
  }

  @Override
  public Set<String> getStateMachineIds() {
    return stateMachines.keySet();
  }

  @Override
  public Optional<ApplicationStates> getState(String machineId) {
    if (doesNotContainStateMachine(machineId)) return Optional.empty();

    if (containsNullStateMachine(machineId)) return Optional.empty();

    return Optional.ofNullable(getStateMachine(machineId).getState().getId());
  }

  @Override
  public void releaseStateMachine(String machineId, boolean stop) {
    releaseStateMachine(machineId);
  }

  private StateMachine<ApplicationStates, ApplicationEvents> getStateMachine(
    String machineId
  ) {
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
