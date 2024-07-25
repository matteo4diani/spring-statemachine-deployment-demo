package dev.sashacorp.statemachine.machine.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Setter
@Getter
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "statemachine.deployment")
public class DeploymentProperties {

  private Long timeout;
  private Long delay;
}
