package dev.sashacorp.statemachine.machine;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import dev.sashacorp.statemachine.machine.configuration.DeploymentConfiguration;
import dev.sashacorp.statemachine.machine.configuration.DeploymentProperties;
import dev.sashacorp.statemachine.machine.configuration.KubernetesListenerConfiguration;
import dev.sashacorp.statemachine.machine.configuration.StateMachineConfiguration;
import dev.sashacorp.statemachine.machine.model.events.AppEvents;
import dev.sashacorp.statemachine.machine.model.states.AppStates;
import dev.sashacorp.statemachine.machine.service.ApplicationStateMachineService;
import dev.sashacorp.statemachine.machine.service.DeploymentGuards;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.boot.support.BootStateMachineMonitor;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
  classes = {
    StateMachineConfiguration.class,
    DeploymentConfiguration.class,
    KubernetesListenerConfiguration.class,
    DeploymentProperties.class,
  }
)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource("classpath:application-test.properties")
class ImpossibleTransitions_ApplicationStateMachineIT {

  @MockBean
  private BootStateMachineMonitor<AppStates, AppEvents> bootStateMachineMonitor;

  @SpyBean
  private ApplicationStateMachineService applicationStateMachineService;

  @SpyBean
  private DeploymentGuards deploymentGuards;

  private StateMachine<AppStates, AppEvents> stateMachine;

  @BeforeEach
  void setup() {
    final var machineId = UUID.randomUUID().toString();
    this.stateMachine =
      this.applicationStateMachineService.acquireStateMachine(machineId);

    this.applicationStateMachineService.setApplication(machineId);
  }

  @Test
  void impossibleTransitions_from_READY() throws Exception {
    final StateMachineTestPlan<AppStates, AppEvents> plan = StateMachineTestPlanBuilder
      .<AppStates, AppEvents>builder()
      .defaultAwaitTime(2)
      .stateMachine(stateMachine)
      .step()
      .expectStates(AppStates.READY)
      .and()
      .step()
      .sendEvent(AppEvents.NAMESPACE_STATUS_CHANGE)
      .expectStateChanged(0)
      .expectEventNotAccepted(1)
      .expectStates(AppStates.READY)
      .and()
      .step()
      .sendEvent(AppEvents.DELETE)
      .expectStateChanged(0)
      .expectEventNotAccepted(1)
      .expectStates(AppStates.READY)
      .and()
      .build();

    plan.test();
  }

  @Test
  void impossibleTransitions_from_DEPLOYING() throws Exception {
    applicationStateMachineService.sendEvents(
      stateMachine.getId(),
      AppEvents.DEPLOY
    );

    final StateMachineTestPlan<AppStates, AppEvents> plan = StateMachineTestPlanBuilder
      .<AppStates, AppEvents>builder()
      .defaultAwaitTime(2)
      .stateMachine(stateMachine)
      .step()
      .expectStates(AppStates.DEPLOYING)
      .and()
      .step()
      .sendEvent(AppEvents.DEPLOY)
      .expectStateChanged(0)
      .expectEventNotAccepted(1)
      .expectStates(AppStates.DEPLOYING)
      .and()
      .step()
      .sendEvent(AppEvents.DELETE)
      .expectStateChanged(0)
      .expectEventNotAccepted(1)
      .expectStates(AppStates.DEPLOYING)
      .and()
      .build();

    plan.test();
  }

  @Test
  void impossibleTransitions_from_DEPLOYED() throws Exception {
    doReturn(true).when(deploymentGuards).isFullyDeployedGuard(any());

    applicationStateMachineService.sendEvents(
      stateMachine.getId(),
      AppEvents.DEPLOY,
      AppEvents.NAMESPACE_STATUS_CHANGE
    );

    final StateMachineTestPlan<AppStates, AppEvents> plan = StateMachineTestPlanBuilder
      .<AppStates, AppEvents>builder()
      .defaultAwaitTime(2)
      .stateMachine(stateMachine)
      .step()
      .expectStates(AppStates.DEPLOYED)
      .and()
      .step()
      .sendEvent(AppEvents.DEPLOY)
      .expectStateChanged(0)
      .expectEventNotAccepted(1)
      .expectStates(AppStates.DEPLOYED)
      .and()
      .step()
      .sendEvent(AppEvents.NAMESPACE_STATUS_CHANGE)
      .expectStateChanged(0)
      .expectEventNotAccepted(1)
      .expectStates(AppStates.DEPLOYED)
      .and()
      .build();

    plan.test();
  }

  @Test
  void impossibleTransitions_from_DELETING() throws Exception {
    doReturn(true).when(deploymentGuards).isFullyDeployedGuard(any());
    doReturn(false).when(deploymentGuards).isNotFullyDeployedGuard(any());
    doReturn(false).when(deploymentGuards).isFullyDeletedGuard(any());
    applicationStateMachineService.sendEvents(
      stateMachine.getId(),
      AppEvents.DEPLOY,
      AppEvents.NAMESPACE_STATUS_CHANGE,
      AppEvents.DELETE
    );

    final StateMachineTestPlan<AppStates, AppEvents> plan = StateMachineTestPlanBuilder
      .<AppStates, AppEvents>builder()
      .defaultAwaitTime(2)
      .stateMachine(stateMachine)
      .step()
      .expectStates(AppStates.DELETING)
      .and()
      .step()
      .sendEvent(AppEvents.DEPLOY)
      .expectStateChanged(0)
      .expectEventNotAccepted(1)
      .expectStates(AppStates.DELETING)
      .and()
      .step()
      .sendEvent(AppEvents.DELETE)
      .expectStateChanged(0)
      .expectEventNotAccepted(1)
      .expectStates(AppStates.DELETING)
      .and()
      .step()
      .sendEvent(AppEvents.NAMESPACE_STATUS_CHANGE)
      .expectStateChanged(0)
      .expectStates(AppStates.DELETING)
      .and()
      .build();

    plan.test();
  }

  @Test
  void impossibleTransitions_from_DELETED() throws Exception {
    doReturn(true).when(deploymentGuards).isFullyDeployedGuard(any());
    doReturn(true).when(deploymentGuards).isFullyDeletedGuard(any());

    applicationStateMachineService.sendEvents(
      stateMachine.getId(),
      AppEvents.DEPLOY,
      AppEvents.NAMESPACE_STATUS_CHANGE,
      AppEvents.DELETE,
      AppEvents.NAMESPACE_STATUS_CHANGE
    );

    final StateMachineTestPlan<AppStates, AppEvents> plan = StateMachineTestPlanBuilder
      .<AppStates, AppEvents>builder()
      .defaultAwaitTime(2)
      .stateMachine(stateMachine)
      .step()
      .expectStates(AppStates.DELETED)
      .and()
      .step()
      .sendEvent(AppEvents.DEPLOY)
      .expectStateChanged(0)
      .expectEventNotAccepted(1)
      .expectStates(AppStates.DELETED)
      .and()
      .step()
      .sendEvent(AppEvents.DELETE)
      .expectStateChanged(0)
      .expectEventNotAccepted(1)
      .expectStates(AppStates.DELETED)
      .and()
      .step()
      .sendEvent(AppEvents.NAMESPACE_STATUS_CHANGE)
      .expectStateChanged(0)
      .expectEventNotAccepted(1)
      .expectStates(AppStates.DELETED)
      .and()
      .build();

    plan.test();
  }

  @Test
  void impossibleTransitions_from_DEPLOYMENT_FAILED() throws Exception {
    doReturn(true).when(deploymentGuards).isNotFullyDeployedGuard(any());
    doReturn(false).when(deploymentGuards).isFullyDeployedGuard(any());

    applicationStateMachineService.sendEvents(
      stateMachine.getId(),
      AppEvents.DEPLOY
    );

    final StateMachineTestPlan<AppStates, AppEvents> plan = StateMachineTestPlanBuilder
      .<AppStates, AppEvents>builder()
      .defaultAwaitTime(5)
      .stateMachine(stateMachine)
      .step()
      .expectStates(AppStates.DEPLOYING)
      .and()
      .step()
      .expectStateChanged(1)
      .expectStates(AppStates.DEPLOYMENT_FAILED)
      .and()
      .step()
      .sendEvent(AppEvents.DEPLOY)
      .expectStateChanged(0)
      .expectEventNotAccepted(1)
      .expectStates(AppStates.DEPLOYMENT_FAILED)
      .and()
      .step()
      .sendEvent(AppEvents.NAMESPACE_STATUS_CHANGE)
      .expectStateChanged(0)
      .expectStates(AppStates.DEPLOYMENT_FAILED)
      .and()
      .step()
      .sendEvent(AppEvents.DELETE)
      .expectStateChanged(0)
      .expectEventNotAccepted(1)
      .expectStates(AppStates.DEPLOYMENT_FAILED)
      .and()
      .build();

    plan.test();
  }
}
