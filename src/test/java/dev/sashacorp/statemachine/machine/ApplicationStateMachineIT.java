package dev.sashacorp.statemachine.machine;

import dev.sashacorp.statemachine.machine.configuration.ApplicationStateMachineConfiguration;
import dev.sashacorp.statemachine.machine.model.events.ApplicationEvents;
import dev.sashacorp.statemachine.machine.model.states.ApplicationStates;
import dev.sashacorp.statemachine.machine.service.ApplicationStateMachineService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(classes = ApplicationStateMachineConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class ApplicationStateMachineIT {

  @SpyBean
  private ApplicationStateMachineService applicationStateMachineService;

  @Test
  void successfulDeployment_and_deletion() throws Exception {
    final var machine =
      this.applicationStateMachineService.acquireStateMachine("1");
    final StateMachineTestPlan<ApplicationStates, ApplicationEvents> plan = StateMachineTestPlanBuilder
      .<ApplicationStates, ApplicationEvents>builder()
      .defaultAwaitTime(2)
      .stateMachine(machine)
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
}
