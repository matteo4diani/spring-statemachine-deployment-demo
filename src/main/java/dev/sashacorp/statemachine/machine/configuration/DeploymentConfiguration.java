package dev.sashacorp.statemachine.machine.configuration;

import dev.sashacorp.statemachine.machine.kubernetes.KubernetesClient;
import dev.sashacorp.statemachine.machine.service.ApplicationStateMachineService;
import dev.sashacorp.statemachine.machine.service.DeploymentActions;
import dev.sashacorp.statemachine.machine.service.DeploymentGuards;
import dev.sashacorp.statemachine.machine.service.DeploymentService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeploymentConfiguration {

  @Bean
  public KubernetesClient kubernetesClient(
    ApplicationEventPublisher applicationEventPublisher
  ) {
    return new KubernetesClient(applicationEventPublisher);
  }

  @Bean
  public DeploymentGuards deploymentGuards(KubernetesClient kubernetesClient) {
    return new DeploymentGuards(kubernetesClient);
  }

  @Bean
  public DeploymentActions deploymentActions(
    KubernetesClient kubernetesClient
  ) {
    return new DeploymentActions(kubernetesClient);
  }

  @Bean
  public DeploymentService deploymentService(
    ApplicationStateMachineService applicationStateMachineService,
    KubernetesClient kubernetesClient
  ) {
    return new DeploymentService(
      applicationStateMachineService,
      kubernetesClient
    );
  }
}
