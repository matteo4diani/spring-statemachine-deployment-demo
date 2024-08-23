package dev.sashacorp.statemachine.machine.service;

import java.util.List;
import java.util.Objects;

import dev.sashacorp.statemachine.machine.kubernetes.KubernetesClient;
import dev.sashacorp.statemachine.machine.kubernetes.model.PodStatus;
import dev.sashacorp.statemachine.machine.kubernetes.model.V1Pod;
import dev.sashacorp.statemachine.machine.model.application.Application;
import dev.sashacorp.statemachine.machine.model.events.AppEvents;
import dev.sashacorp.statemachine.machine.model.states.AppStates;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.data.jpa.JpaStateMachineRepository;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import reactor.core.publisher.Mono;

@Slf4j(topic="🤖 State Machine Service")
public class ApplicationStateMachineService
  implements CustomStateMachineService {

  @Autowired
  private StateMachinePersist<AppStates, AppEvents, String> stateMachinePersist;

  @Autowired
  private JpaStateMachineRepository jpaStateMachineRepository;

  private final StateMachineFactory<AppStates, AppEvents> stateMachineFactory;

  @Autowired
  private KubernetesClient kubernetesClient;

  public ApplicationStateMachineService(
    StateMachineFactory<AppStates, AppEvents> stateMachineFactory
  ) {
    this.stateMachineFactory = stateMachineFactory;
  }

  @Override
  public StateMachine<AppStates, AppEvents> acquireStateMachine(
    String machineId
  ) {
    log.info("🚧 Building state machine from factory with id " + machineId);

    final var stateMachine = stateMachineFactory.getStateMachine(
      machineId
    );

    try {
      log.info("🔎 Trying to read existing state machine context from persistence with id " + machineId);

      StateMachineContext<AppStates, AppEvents> stateMachineContext = stateMachinePersist.read(
              machineId
      );

      if (!Objects.isNull(stateMachineContext)) {
        log.info("✅ Found existing state machine context from persistence with id " + machineId);

        stateMachine.stopReactively().block();
        stateMachine
                .getStateMachineAccessor()
                .doWithAllRegions(function ->
                                          function.resetStateMachineReactively(stateMachineContext).block()
                );

        log.info("♻️Restored existing state machine context from persistence with id " + machineId);
      } else {
        log.info("❌️No existing context found with id " + machineId);
        log.info("🚀 New machine created with id " + machineId);
        this.setApplication(stateMachine);
      }

      stateMachine.startReactively().block();

    } catch (Exception e) {
      log.error("🔥 Error during state machine acquisition with id " + machineId);
    }

    return stateMachine;
  }

  @Override
  public StateMachine<AppStates, AppEvents> restoreApplication(String machineId, AppStates state) {
    log.info("💀 Restoring pre-existing app as state machine with id {} and state {}", machineId, state);

    final var stateMachine = stateMachineFactory.getStateMachine(
            machineId
    );

    this.setApplication(stateMachine);

    final var extendedState = new DefaultExtendedState();

    extendedState.getVariables().put(Application.APPLICATION, new Application(machineId));

    final var context = new DefaultStateMachineContext<AppStates, AppEvents>(
            state,
            null,
            null,
            extendedState,
            null,
            machineId
    );


    try {

      this.stateMachinePersist.write(context, machineId);

      stateMachine.stopReactively().block();

      stateMachine.getStateMachineAccessor().doWithAllRegions(
              stateMachineAccess -> stateMachineAccess.resetStateMachineReactively(context).block()
      );

      log.info("🚀 Pre-existing app restored as state machine with id {} and state {}", machineId, stateMachine.getState().getId());

    } catch (Exception e) {
      log.error("🔥 Error during pre-existing app state machine restoration with id {} and state {}", machineId, state);
    }

    this.kubernetesClient.simulatePreExistingKubernetesDeployment(machineId);

    return acquireStateMachine(machineId);
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

    stateMachine.stopReactively().block();
    this.jpaStateMachineRepository.deleteById(machineId);

  }

  @Override
  public void releaseStateMachine(String machineId, boolean stop) {
    releaseStateMachine(machineId);
  }

  @Override
  public void setApplication(String machineId) {
    final var application = new Application(machineId);
    final var stateMachine = acquireStateMachine(machineId);

    stateMachine
      .getExtendedState()
      .getVariables()
      .put(Application.APPLICATION, application);
  }

  public void setApplication(StateMachine<AppStates, AppEvents> stateMachine) {
    final var application = new Application(stateMachine.getId());

    stateMachine
            .getExtendedState()
            .getVariables()
            .put(Application.APPLICATION, application);
  }

  @Override
  public StateMachine<AppStates, AppEvents> sendEvent(
    String machineId,
    AppEvents event
  ) {
    log.info("📨 Trying to send event [{}] to machine [{}]", event, machineId);
    final var stateMachine = acquireStateMachine(machineId);

    stateMachine
      .sendEvent(Mono.just(MessageBuilder.withPayload(event).build()))
      .blockFirst();

    log.info("💌 Event [{}] sent to machine [{}]", event, machineId);

    return stateMachine;
  }

  @Override
  public StateMachine<AppStates, AppEvents> sendEvents(
    String machineId,
    AppEvents... events
  ) {
    final var stateMachine = acquireStateMachine(machineId);

    for (AppEvents event : events) {
      sendEvent(machineId, event);
    }

    log.info("Events {} sent to machine [{}]", events, machineId);

    return stateMachine;
  }
}
