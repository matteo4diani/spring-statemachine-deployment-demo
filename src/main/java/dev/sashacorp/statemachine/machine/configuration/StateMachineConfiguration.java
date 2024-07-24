package dev.sashacorp.statemachine.machine.configuration;

import dev.sashacorp.statemachine.machine.model.events.AppEvents;
import dev.sashacorp.statemachine.machine.model.states.AppStates;
import dev.sashacorp.statemachine.machine.service.ApplicationStateMachineService;
import dev.sashacorp.statemachine.machine.service.DeploymentActions;
import dev.sashacorp.statemachine.machine.service.DeploymentGuards;
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
public class StateMachineConfiguration
  extends StateMachineConfigurerAdapter<AppStates, AppEvents> {

  private final BootStateMachineMonitor<AppStates, AppEvents> stateMachineMonitor;
  private final DeploymentGuards deploymentGuards;
  private final DeploymentActions deploymentActions;

  public StateMachineConfiguration(
    BootStateMachineMonitor<AppStates, AppEvents> stateMachineMonitor,
    DeploymentGuards deploymentGuards,
    DeploymentActions deploymentActions
  ) {
    this.stateMachineMonitor = stateMachineMonitor;
    this.deploymentGuards = deploymentGuards;
    this.deploymentActions = deploymentActions;
  }

  @Bean
  public ApplicationStateMachineService applicationStateMachineService(
    StateMachineFactory<AppStates, AppEvents> stateMachineFactory
  ) {
    return new ApplicationStateMachineService(stateMachineFactory);
  }

  @Override
  public void configure(
    StateMachineConfigurationConfigurer<AppStates, AppEvents> config
  ) throws Exception {
    config.withMonitoring().monitor(this.stateMachineMonitor);

    config.withConfiguration().autoStartup(false);
  }

  @Override
  public void configure(
    StateMachineStateConfigurer<AppStates, AppEvents> states
  ) throws Exception {
    states
      .withStates()
      .initial(AppStates.READY)
      .end(AppStates.DELETED)
      .state(
        AppStates.DEPLOYING,
        context ->
          this.deploymentActions.deployAction(context.getStateMachine().getId())
      )
      .state(
        AppStates.DELETING,
        context ->
          this.deploymentActions.deleteAction(context.getStateMachine().getId())
      )
      .state(AppStates.DEPLOYED);
  }

  @Override
  public void configure(
    StateMachineTransitionConfigurer<AppStates, AppEvents> transitions
  ) throws Exception {
    transitions
      .withExternal()
      .source(AppStates.READY)
      .target(AppStates.DEPLOYING)
      .event(AppEvents.DEPLOY)
      .guard(context -> true) // check max number and exists
      .and()
      .withExternal()
      .source(AppStates.DEPLOYING)
      .target(AppStates.DEPLOYED)
      .event(AppEvents.NAMESPACE_STATUS_CHANGE)
      .guard(context ->
        this.deploymentGuards.isFullyDeployed(
            context.getStateMachine().getExtendedState()
          )
      ) // check K8s API against descriptor
      .and()
      .withExternal()
      .source(AppStates.DEPLOYED)
      .target(AppStates.DELETING)
      .event(AppEvents.DELETE)
      .and()
      .withExternal()
      .source(AppStates.DELETING)
      .target(AppStates.DELETED)
      .event(AppEvents.NAMESPACE_STATUS_CHANGE)
      .guard(context ->
        this.deploymentGuards.isFullyDeleted(
            context.getStateMachine().getExtendedState()
          )
      ); // check K8s API against descriptor
  }
}
