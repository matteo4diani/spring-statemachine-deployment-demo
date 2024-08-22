package dev.sashacorp.statemachine.machine.service;

import dev.sashacorp.statemachine.machine.model.application.Application;
import dev.sashacorp.statemachine.machine.model.events.AppEvents;
import dev.sashacorp.statemachine.machine.model.states.AppStates;
import java.util.Optional;
import java.util.Set;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.service.StateMachineService;

public interface CustomStateMachineService
  extends StateMachineService<AppStates, AppEvents> {
  @Override
  StateMachine<AppStates, AppEvents> acquireStateMachine(String machineId);

  @Override
  void releaseStateMachine(String machineId);

  void setApplication(String machineId);

  StateMachine<AppStates, AppEvents> sendEvent(
    String machineId,
    AppEvents event
  );

  StateMachine<AppStates, AppEvents> sendEvents(
    String machineId,
    AppEvents... events
  );

  StateMachine<AppStates, AppEvents> restoreApplication(String machineId, AppStates state);
}
