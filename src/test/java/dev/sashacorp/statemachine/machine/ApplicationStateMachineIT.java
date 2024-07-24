package dev.sashacorp.statemachine.machine;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import dev.sashacorp.statemachine.machine.configuration.DeploymentConfiguration;
import dev.sashacorp.statemachine.machine.configuration.KubernetesListenerConfiguration;
import dev.sashacorp.statemachine.machine.configuration.StateMachineConfiguration;
import dev.sashacorp.statemachine.machine.kubernetes.KubernetesClient;
import dev.sashacorp.statemachine.machine.model.events.AppEvents;
import dev.sashacorp.statemachine.machine.model.states.AppStates;
import dev.sashacorp.statemachine.machine.service.ApplicationStateMachineService;
import dev.sashacorp.statemachine.machine.service.DeploymentGuards;
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

@SpringBootTest(
  classes = {
    StateMachineConfiguration.class,
    DeploymentConfiguration.class,
    KubernetesListenerConfiguration.class,
  }
)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ApplicationStateMachineIT {

  public static final String MACHINE_ID = "1";

  @MockBean
  private BootStateMachineMonitor<AppStates, AppEvents> bootStateMachineMonitor;

  @SpyBean
  private ApplicationStateMachineService applicationStateMachineService;

  @SpyBean
  private KubernetesClient kubernetesClient;

  @SpyBean
  private DeploymentGuards deploymentGuards;

  private StateMachine<AppStates, AppEvents> stateMachine;

  @BeforeEach
  void setup() {
    this.stateMachine =
      this.applicationStateMachineService.acquireStateMachine(MACHINE_ID);

    this.applicationStateMachineService.setApplication(MACHINE_ID);
  }

  @Test
  void successfulDeployment_andDeletion() throws Exception {
    final StateMachineTestPlan<AppStates, AppEvents> plan = StateMachineTestPlanBuilder
      .<AppStates, AppEvents>builder()
      .defaultAwaitTime(20)
      .stateMachine(this.stateMachine)
      .step()
      .expectStates(AppStates.READY)
      .and()
      .step()
      .sendEvent(AppEvents.DEPLOY)
      .expectStateChanged(1)
      .expectStates(AppStates.DEPLOYING)
      .and()
      .step()
      .expectStateChanged(1)
      .expectStates(AppStates.DEPLOYED)
      .and()
      .step()
      .sendEvent(AppEvents.DELETE)
      .expectStateChanged(1)
      .expectStates(AppStates.DELETING)
      .and()
      .step()
      .sendEvent(AppEvents.NAMESPACE_STATUS_CHANGE)
      .expectStateChanged(1)
      .expectStates(AppStates.DELETED)
      .and()
      .build();

    plan.test();
  }

  @Test
  void impossibleTransitions_from_READY() throws Exception {
    final StateMachineTestPlan<AppStates, AppEvents> plan = StateMachineTestPlanBuilder
      .<AppStates, AppEvents>builder()
      .defaultAwaitTime(2)
      .stateMachine(this.stateMachine)
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
    applicationStateMachineService.sendEvents(MACHINE_ID, AppEvents.DEPLOY);

    final StateMachineTestPlan<AppStates, AppEvents> plan = StateMachineTestPlanBuilder
      .<AppStates, AppEvents>builder()
      .defaultAwaitTime(2)
      .stateMachine(this.stateMachine)
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
    doReturn(true).when(deploymentGuards).isFullyDeployed(any());

    applicationStateMachineService.sendEvents(
      MACHINE_ID,
      AppEvents.DEPLOY,
      AppEvents.NAMESPACE_STATUS_CHANGE
    );

    final StateMachineTestPlan<AppStates, AppEvents> plan = StateMachineTestPlanBuilder
      .<AppStates, AppEvents>builder()
      .defaultAwaitTime(2)
      .stateMachine(this.stateMachine)
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
    doReturn(true).when(deploymentGuards).isFullyDeployed(any());

    applicationStateMachineService.sendEvents(
      MACHINE_ID,
      AppEvents.DEPLOY,
      AppEvents.NAMESPACE_STATUS_CHANGE,
      AppEvents.DELETE
    );

    final StateMachineTestPlan<AppStates, AppEvents> plan = StateMachineTestPlanBuilder
      .<AppStates, AppEvents>builder()
      .defaultAwaitTime(2)
      .stateMachine(this.stateMachine)
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
      .build();

    plan.test();
  }

  @Test
  void impossibleTransitions_from_DELETED() throws Exception {
    doReturn(true).when(deploymentGuards).isFullyDeployed(any());

    applicationStateMachineService.sendEvents(
      MACHINE_ID,
      AppEvents.DEPLOY,
      AppEvents.NAMESPACE_STATUS_CHANGE,
      AppEvents.DELETE,
      AppEvents.NAMESPACE_STATUS_CHANGE
    );

    final StateMachineTestPlan<AppStates, AppEvents> plan = StateMachineTestPlanBuilder
      .<AppStates, AppEvents>builder()
      .defaultAwaitTime(2)
      .stateMachine(this.stateMachine)
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
}
