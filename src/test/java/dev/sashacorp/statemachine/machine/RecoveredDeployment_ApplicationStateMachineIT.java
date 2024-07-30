package dev.sashacorp.statemachine.machine;

import dev.sashacorp.statemachine.machine.configuration.DeploymentConfiguration;
import dev.sashacorp.statemachine.machine.configuration.DeploymentProperties;
import dev.sashacorp.statemachine.machine.configuration.KubernetesListenerConfiguration;
import dev.sashacorp.statemachine.machine.configuration.StateMachineConfiguration;
import dev.sashacorp.statemachine.machine.model.events.AppEvents;
import dev.sashacorp.statemachine.machine.model.states.AppStates;
import dev.sashacorp.statemachine.machine.service.ApplicationStateMachineService;
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
@TestPropertySource(
  properties = {
    "statemachine.deployment.timeout=100", "statemachine.deployment.delay=100",
  }
)
class RecoveredDeployment_ApplicationStateMachineIT {

  @MockBean
  private BootStateMachineMonitor<AppStates, AppEvents> bootStateMachineMonitor;

  @SpyBean
  private ApplicationStateMachineService applicationStateMachineService;

  private StateMachine<AppStates, AppEvents> stateMachine;

  @BeforeEach
  void setup() {
    final var machineId = UUID.randomUUID().toString();
    this.stateMachine =
      this.applicationStateMachineService.acquireStateMachine(machineId);

    this.applicationStateMachineService.setApplication(machineId);
  }

  @Test
  void unsuccessfulDeployment_recovery_andDeletion() throws Exception {
    final StateMachineTestPlan<AppStates, AppEvents> plan = StateMachineTestPlanBuilder
      .<AppStates, AppEvents>builder()
      .defaultAwaitTime(10)
      .stateMachine(stateMachine)
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
      .expectStates(AppStates.DEPLOYMENT_FAILED)
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
      .expectStateChanged(1)
      .expectStates(AppStates.DELETED)
      .and()
      .build();

    plan.test();
  }
}
