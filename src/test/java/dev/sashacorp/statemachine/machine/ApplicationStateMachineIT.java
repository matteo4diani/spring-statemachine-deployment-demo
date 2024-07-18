package dev.sashacorp.statemachine.machine;

import dev.sashacorp.statemachine.machine.configuration.ApplicationStateMachineConfiguration;
import dev.sashacorp.statemachine.machine.model.events.ApplicationEvents;
import dev.sashacorp.statemachine.machine.model.states.ApplicationStates;
import dev.sashacorp.statemachine.machine.service.ApplicationStateMachineService;
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

@SpringBootTest(classes = { ApplicationStateMachineConfiguration.class })
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ApplicationStateMachineIT {

  public static final String MACHINE_ID = "1";

  @MockBean
  private BootStateMachineMonitor<ApplicationStates, ApplicationEvents> bootStateMachineMonitor;

  @SpyBean
  private ApplicationStateMachineService applicationStateMachineService;

  private StateMachine<ApplicationStates, ApplicationEvents> stateMachine;

  @BeforeEach
  void setup() {
    this.stateMachine =
      this.applicationStateMachineService.acquireStateMachine(MACHINE_ID);
  }

  @Test
  void successfulDeployment_andDeletion() throws Exception {
    final StateMachineTestPlan<ApplicationStates, ApplicationEvents> plan = StateMachineTestPlanBuilder
      .<ApplicationStates, ApplicationEvents>builder()
      .defaultAwaitTime(2)
      .stateMachine(this.stateMachine)
      .step()
      .expectStates(ApplicationStates.READY)
      .and()
      .step()
      .sendEvent(ApplicationEvents.DEPLOY)
      .expectStateChanged(1)
      .expectStates(ApplicationStates.DEPLOYING)
      .and()
      .step()
      .sendEvent(ApplicationEvents.NAMESPACE_STATUS_CHANGE)
      .expectStateChanged(1)
      .expectStates(ApplicationStates.DEPLOYED)
      .and()
      .step()
      .sendEvent(ApplicationEvents.DELETE)
      .expectStateChanged(1)
      .expectStates(ApplicationStates.DELETING)
      .and()
      .step()
      .sendEvent(ApplicationEvents.NAMESPACE_STATUS_CHANGE)
      .expectStateChanged(1)
      .expectStates(ApplicationStates.DELETED)
      .and()
      .build();

    plan.test();
  }

  @Test
  void impossibleTransitions_from_READY() throws Exception {
    final StateMachineTestPlan<ApplicationStates, ApplicationEvents> plan = StateMachineTestPlanBuilder
      .<ApplicationStates, ApplicationEvents>builder()
      .defaultAwaitTime(2)
      .stateMachine(this.stateMachine)
      .step()
      .expectStates(ApplicationStates.READY)
      .and()
      .step()
      .sendEvent(ApplicationEvents.NAMESPACE_STATUS_CHANGE)
      .expectStateChanged(0)
      .expectEventNotAccepted(1)
      .expectStates(ApplicationStates.READY)
      .and()
      .step()
      .sendEvent(ApplicationEvents.DELETE)
      .expectStateChanged(0)
      .expectEventNotAccepted(1)
      .expectStates(ApplicationStates.READY)
      .and()
      .build();

    plan.test();
  }

  @Test
  void impossibleTransitions_from_DEPLOYING() throws Exception {
    applicationStateMachineService.sendEvents(
      MACHINE_ID,
      ApplicationEvents.DEPLOY
    );

    final StateMachineTestPlan<ApplicationStates, ApplicationEvents> plan = StateMachineTestPlanBuilder
      .<ApplicationStates, ApplicationEvents>builder()
      .defaultAwaitTime(2)
      .stateMachine(this.stateMachine)
      .step()
      .expectStates(ApplicationStates.DEPLOYING)
      .and()
      .step()
      .sendEvent(ApplicationEvents.DEPLOY)
      .expectStateChanged(0)
      .expectEventNotAccepted(1)
      .expectStates(ApplicationStates.DEPLOYING)
      .and()
      .step()
      .sendEvent(ApplicationEvents.DELETE)
      .expectStateChanged(0)
      .expectEventNotAccepted(1)
      .expectStates(ApplicationStates.DEPLOYING)
      .and()
      .build();

    plan.test();
  }

  @Test
  void impossibleTransitions_from_DEPLOYED() throws Exception {
    applicationStateMachineService.sendEvents(
      MACHINE_ID,
      ApplicationEvents.DEPLOY,
      ApplicationEvents.NAMESPACE_STATUS_CHANGE
    );

    final StateMachineTestPlan<ApplicationStates, ApplicationEvents> plan = StateMachineTestPlanBuilder
      .<ApplicationStates, ApplicationEvents>builder()
      .defaultAwaitTime(2)
      .stateMachine(this.stateMachine)
      .step()
      .expectStates(ApplicationStates.DEPLOYED)
      .and()
      .step()
      .sendEvent(ApplicationEvents.DEPLOY)
      .expectStateChanged(0)
      .expectEventNotAccepted(1)
      .expectStates(ApplicationStates.DEPLOYED)
      .and()
      .step()
      .sendEvent(ApplicationEvents.NAMESPACE_STATUS_CHANGE)
      .expectStateChanged(0)
      .expectEventNotAccepted(1)
      .expectStates(ApplicationStates.DEPLOYED)
      .and()
      .build();

    plan.test();
  }

  @Test
  void impossibleTransitions_from_DELETING() throws Exception {
    applicationStateMachineService.sendEvents(
      MACHINE_ID,
      ApplicationEvents.DEPLOY,
      ApplicationEvents.NAMESPACE_STATUS_CHANGE,
      ApplicationEvents.DELETE
    );

    final StateMachineTestPlan<ApplicationStates, ApplicationEvents> plan = StateMachineTestPlanBuilder
      .<ApplicationStates, ApplicationEvents>builder()
      .defaultAwaitTime(2)
      .stateMachine(this.stateMachine)
      .step()
      .expectStates(ApplicationStates.DELETING)
      .and()
      .step()
      .sendEvent(ApplicationEvents.DEPLOY)
      .expectStateChanged(0)
      .expectEventNotAccepted(1)
      .expectStates(ApplicationStates.DELETING)
      .and()
      .step()
      .sendEvent(ApplicationEvents.DELETE)
      .expectStateChanged(0)
      .expectEventNotAccepted(1)
      .expectStates(ApplicationStates.DELETING)
      .and()
      .build();

    plan.test();
  }

  @Test
  void impossibleTransitions_from_DELETED() throws Exception {
    applicationStateMachineService.sendEvents(
      MACHINE_ID,
      ApplicationEvents.DEPLOY,
      ApplicationEvents.NAMESPACE_STATUS_CHANGE,
      ApplicationEvents.DELETE,
      ApplicationEvents.NAMESPACE_STATUS_CHANGE
    );

    final StateMachineTestPlan<ApplicationStates, ApplicationEvents> plan = StateMachineTestPlanBuilder
      .<ApplicationStates, ApplicationEvents>builder()
      .defaultAwaitTime(2)
      .stateMachine(this.stateMachine)
      .step()
      .expectStates(ApplicationStates.DELETED)
      .and()
      .step()
      .sendEvent(ApplicationEvents.DEPLOY)
      .expectStateChanged(0)
      .expectEventNotAccepted(1)
      .expectStates(ApplicationStates.DELETED)
      .and()
      .step()
      .sendEvent(ApplicationEvents.DELETE)
      .expectStateChanged(0)
      .expectEventNotAccepted(1)
      .expectStates(ApplicationStates.DELETED)
      .and()
      .step()
      .sendEvent(ApplicationEvents.NAMESPACE_STATUS_CHANGE)
      .expectStateChanged(0)
      .expectEventNotAccepted(1)
      .expectStates(ApplicationStates.DELETED)
      .and()
      .build();

    plan.test();
  }
}
