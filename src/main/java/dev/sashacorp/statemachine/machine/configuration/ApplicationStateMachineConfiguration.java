package dev.sashacorp.statemachine.machine.configuration;

import java.util.EnumSet;

import dev.sashacorp.statemachine.machine.model.events.ApplicationEvents;
import dev.sashacorp.statemachine.machine.model.states.ApplicationStates;
import dev.sashacorp.statemachine.machine.service.ApplicationStateMachineService;
import dev.sashacorp.statemachine.machine.service.CustomStateMachineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@Configuration
@EnableStateMachineFactory
@Slf4j
public class ApplicationStateMachineConfiguration extends StateMachineConfigurerAdapter<ApplicationStates, ApplicationEvents> {
    @Bean
    public CustomStateMachineService stateMachineService(
            StateMachineFactory<ApplicationStates, ApplicationEvents> stateMachineFactory
    ) {
        return new ApplicationStateMachineService(stateMachineFactory);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<ApplicationStates, ApplicationEvents> config) throws Exception {
        config.withConfiguration()
              .autoStartup(false)
              .listener(new ApplicationStateMachineListener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<ApplicationStates, ApplicationEvents> states) throws Exception {
        states.withStates()
                .initial(ApplicationStates.READY)
                .states(EnumSet.allOf(ApplicationStates.class))
                .end(ApplicationStates.DELETED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<ApplicationStates, ApplicationEvents> transitions) throws Exception {
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
                    .guard(context -> true) // check K8s API against descriptor
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
                    .guard(context -> true); // check K8s API against descriptor
    }
}
