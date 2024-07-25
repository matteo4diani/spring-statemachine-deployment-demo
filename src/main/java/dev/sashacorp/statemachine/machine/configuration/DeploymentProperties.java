package dev.sashacorp.statemachine.machine.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@EnableConfigurationProperties(DeploymentProperties.class)
@ConfigurationProperties(prefix = "statemachine.deployment")
public class DeploymentProperties {

  private Long timeout;
  private Long delay;
}
