package dev.sashacorp.statemachine.machine.service;

import dev.sashacorp.statemachine.machine.model.events.ApplicationEvents;
import dev.sashacorp.statemachine.machine.model.states.ApplicationStates;
import java.util.Optional;
import java.util.Set;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.service.StateMachineService;

public interface CustomStateMachineService
  extends StateMachineService<ApplicationStates, ApplicationEvents> {
  @Override
  StateMachine<ApplicationStates, ApplicationEvents> acquireStateMachine(
    String machineId
  );

  Optional<StateMachine<ApplicationStates, ApplicationEvents>> acquireExistingStateMachine(
    String machineId
  );

  @Override
  void releaseStateMachine(String machineId);

  Set<String> getStateMachineIds();

  Optional<ApplicationStates> getState(String machineId);

  StateMachine<ApplicationStates, ApplicationEvents> sendEvent(
    String machineId,
    ApplicationEvents event
  );
}
