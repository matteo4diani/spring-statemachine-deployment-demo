package dev.sashacorp.statemachine.machine.configuration;

import dev.sashacorp.statemachine.machine.kubernetes.KubernetesClient;
import dev.sashacorp.statemachine.machine.kubernetes.KubernetesResourceEventHandler;
import dev.sashacorp.statemachine.machine.service.ApplicationService;
import dev.sashacorp.statemachine.machine.service.ApplicationStateMachineService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

  @Bean
  public KubernetesClient kubernetesClient() {
    return new KubernetesClient();
  }

  @Bean
  public KubernetesResourceEventHandler kubernetesResourceEventHandler(
    ApplicationStateMachineService applicationStateMachineService
  ) {
    return new KubernetesResourceEventHandler(applicationStateMachineService);
  }

  @Bean
  public ApplicationService applicationService(
    KubernetesClient kubernetesClient
  ) {
    return new ApplicationService(kubernetesClient);
  }
}
