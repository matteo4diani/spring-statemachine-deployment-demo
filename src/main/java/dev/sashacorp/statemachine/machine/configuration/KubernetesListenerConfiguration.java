package dev.sashacorp.statemachine.machine.configuration;

import dev.sashacorp.statemachine.machine.kubernetes.KubernetesListener;
import dev.sashacorp.statemachine.machine.service.ApplicationStateMachineService;
import org.springframework.context.annotation.Bean;

public class KubernetesListenerConfiguration {

  @Bean
  public KubernetesListener kubernetesListener(
    ApplicationStateMachineService applicationStateMachineService
  ) {
    return new KubernetesListener(applicationStateMachineService);
  }
}
