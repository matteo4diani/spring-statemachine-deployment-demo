package dev.sashacorp.statemachine.machine.service;

import java.util.Set;

import dev.sashacorp.statemachine.machine.model.events.ApplicationEvents;
import dev.sashacorp.statemachine.machine.model.states.ApplicationStates;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.service.StateMachineService;

public interface CustomStateMachineService extends StateMachineService<ApplicationStates, ApplicationEvents> {
    @Override
    StateMachine<ApplicationStates, ApplicationEvents> acquireStateMachine(String machineId);

    @Override
    void releaseStateMachine(String machineId);

    Set<String> getAllStateMachines();
}
