package dev.sashacorp.statemachine.machine.configuration;

import dev.sashacorp.statemachine.machine.model.events.AppEvents;
import dev.sashacorp.statemachine.machine.model.states.AppStates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.data.jpa.JpaPersistingStateMachineInterceptor;
import org.springframework.statemachine.data.jpa.JpaStateMachineRepository;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;

@Configuration
public class JpaPersisterConfig {

  @Bean
  public StateMachineRuntimePersister<AppStates, AppEvents, String> stateMachineRuntimePersister(
    JpaStateMachineRepository jpaStateMachineRepository
  ) {
    return new JpaPersistingStateMachineInterceptor<>(
      jpaStateMachineRepository
    );
  }
}
