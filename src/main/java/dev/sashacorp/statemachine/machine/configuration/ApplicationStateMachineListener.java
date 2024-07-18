package dev.sashacorp.statemachine.machine.configuration;

import dev.sashacorp.statemachine.machine.model.events.ApplicationEvents;
import dev.sashacorp.statemachine.machine.model.states.ApplicationStates;
import dev.sashacorp.statemachine.machine.service.ApplicationStateMachineService;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

@Slf4j
class ApplicationStateMachineListener
  extends StateMachineListenerAdapter<ApplicationStates, ApplicationEvents> {

  @Override
  public void stateChanged(
    State<ApplicationStates, ApplicationEvents> from,
    State<ApplicationStates, ApplicationEvents> to
  ) {
    log.info(
      "State changed from {} to {}",
      Objects.isNull(from) ? null : from.getId().name(),
      to.getId().name()
    );
  }
}
