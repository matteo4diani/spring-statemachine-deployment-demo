package dev.sashacorp.statemachine.machine.service;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import dev.sashacorp.statemachine.machine.model.events.ApplicationEvents;
import dev.sashacorp.statemachine.machine.model.states.ApplicationStates;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

public class ApplicationStateMachineService implements CustomStateMachineService {
    private final StateMachineFactory<ApplicationStates, ApplicationEvents> stateMachineFactory;

    private final ConcurrentHashMap<String, StateMachine<ApplicationStates, ApplicationEvents>> stateMachines = new ConcurrentHashMap<>();

    public ApplicationStateMachineService(StateMachineFactory<ApplicationStates, ApplicationEvents> stateMachineFactory) {
        this.stateMachineFactory = stateMachineFactory;
    }

    @Override
    public StateMachine<ApplicationStates, ApplicationEvents> acquireStateMachine(String machineId) {
        if (!stateMachines.containsKey(machineId)) {
            stateMachines.put(machineId, stateMachineFactory.getStateMachine(machineId));
        }

        if (Objects.isNull(getStateMachine(machineId))) {
            stateMachines.put(machineId, stateMachineFactory.getStateMachine(machineId));
        }

        getStateMachine(machineId).startReactively().block();

        return getStateMachine(machineId);
    }

    private StateMachine<ApplicationStates, ApplicationEvents> getStateMachine(String machineId) {
        return stateMachines.get(machineId);
    }

    @Override
    public StateMachine<ApplicationStates, ApplicationEvents> acquireStateMachine(String machineId, boolean start) {
        return acquireStateMachine(machineId);
    }

    @Override
    public void releaseStateMachine(String machineId) {
        if (!stateMachines.containsKey(machineId)) {
            return;
        }

        if (Objects.isNull(getStateMachine(machineId))) {
            stateMachines.remove(machineId);
        }

        getStateMachine(machineId).stopReactively().block();

        stateMachines.remove(machineId);
    }

    @Override
    public Set<String> getAllStateMachines() {
        return stateMachines.keySet();
    }

    @Override
    public void releaseStateMachine(String machineId, boolean stop) {
        releaseStateMachine(machineId);
    }
}
