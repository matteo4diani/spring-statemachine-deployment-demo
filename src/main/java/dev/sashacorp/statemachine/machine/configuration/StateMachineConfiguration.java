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
  private final DeploymentProperties deploymentProperties;

  public StateMachineConfiguration(
    BootStateMachineMonitor<AppStates, AppEvents> stateMachineMonitor,
    DeploymentGuards deploymentGuards,
    DeploymentActions deploymentActions,
    DeploymentProperties deploymentProperties
  ) {
    this.stateMachineMonitor = stateMachineMonitor;
    this.deploymentGuards = deploymentGuards;
    this.deploymentActions = deploymentActions;
    this.deploymentProperties = deploymentProperties;
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
      .state(AppStates.DEPLOYING, this.deploymentActions::deployAction)
      .state(AppStates.DELETING, this.deploymentActions::deleteAction)
      .state(AppStates.DEPLOYMENT_FAILED)
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
      .event(AppEvents.KUBERNETES_STATUS_CHANGE)
      .guard(this.deploymentGuards::isFullyDeployedGuard)
      .and()
      .withExternal()
      .source(AppStates.DEPLOYING)
      .target(AppStates.DEPLOYMENT_FAILED)
      .timerOnce(this.deploymentProperties.getTimeout())
      .guard(this.deploymentGuards::isNotFullyDeployedGuard)
      .and()
      .withExternal()
      .source(AppStates.DEPLOYMENT_FAILED)
      .target(AppStates.DEPLOYED)
      .event(AppEvents.KUBERNETES_STATUS_CHANGE)
      .guard(this.deploymentGuards::isFullyDeployedGuard)
      .and()
      .withExternal()
      .source(AppStates.DEPLOYED)
      .target(AppStates.DELETING)
      .event(AppEvents.DELETE)
      .and()
      .withExternal()
      .source(AppStates.DELETING)
      .target(AppStates.DELETED)
      .event(AppEvents.KUBERNETES_STATUS_CHANGE)
      .guard(this.deploymentGuards::isFullyDeletedGuard);
  }
}
