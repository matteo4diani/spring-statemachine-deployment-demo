package dev.sashacorp.statemachine.machine.configuration;

import dev.sashacorp.statemachine.machine.model.events.ApplicationEvents;
import dev.sashacorp.statemachine.machine.model.states.ApplicationStates;
import dev.sashacorp.statemachine.machine.service.ApplicationService;
import dev.sashacorp.statemachine.machine.service.ApplicationStateMachineService;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.boot.support.BootStateMachineMonitor;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@Configuration
@EnableStateMachineFactory
@Slf4j
public class ApplicationStateMachineConfiguration
  extends StateMachineConfigurerAdapter<ApplicationStates, ApplicationEvents> {

  private final BootStateMachineMonitor<ApplicationStates, ApplicationEvents> stateMachineMonitor;
  private final ApplicationService applicationService;

  public ApplicationStateMachineConfiguration(
    BootStateMachineMonitor<ApplicationStates, ApplicationEvents> stateMachineMonitor,
    ApplicationService applicationService
  ) {
    this.stateMachineMonitor = stateMachineMonitor;
    this.applicationService = applicationService;
  }

  @Bean
  public ApplicationStateMachineService applicationStateMachineService(
    StateMachineFactory<ApplicationStates, ApplicationEvents> stateMachineFactory
  ) {
    return new ApplicationStateMachineService(stateMachineFactory);
  }

  @Override
  public void configure(
    StateMachineConfigurationConfigurer<ApplicationStates, ApplicationEvents> config
  ) throws Exception {
    config.withMonitoring().monitor(stateMachineMonitor);

    config.withConfiguration().autoStartup(false);
  }

  @Override
  public void configure(
    StateMachineStateConfigurer<ApplicationStates, ApplicationEvents> states
  ) throws Exception {
    states
      .withStates()
      .initial(ApplicationStates.READY)
      .end(ApplicationStates.DELETED)
      .states(
        Set.of(
          ApplicationStates.DEPLOYING,
          ApplicationStates.DEPLOYED,
          ApplicationStates.DELETING
        )
      );
  }

  @Override
  public void configure(
    StateMachineTransitionConfigurer<ApplicationStates, ApplicationEvents> transitions
  ) throws Exception {
    transitions
      .withExternal()
      .source(ApplicationStates.READY)
      .target(ApplicationStates.DEPLOYING)
      .event(ApplicationEvents.DEPLOY)
      .guard(context -> true) // check max number and exists
      .and()
      .withExternal()
      .source(ApplicationStates.DEPLOYING)
      .target(ApplicationStates.DEPLOYED)
      .event(ApplicationEvents.NAMESPACE_STATUS_CHANGE)
      .guard(context ->
        this.applicationService.isFullyDeployed(
            context.getStateMachine().getId()
          )
      ) // check K8s API against descriptor
      .and()
      .withExternal()
      .source(ApplicationStates.DEPLOYED)
      .target(ApplicationStates.DELETING)
      .event(ApplicationEvents.DELETE)
      .and()
      .withExternal()
      .source(ApplicationStates.DELETING)
      .target(ApplicationStates.DELETED)
      .event(ApplicationEvents.NAMESPACE_STATUS_CHANGE)
      .guard(context ->
        this.applicationService.isFullyDeleted(
            context.getStateMachine().getId()
          )
      ); // check K8s API against descriptor
  }
}
