package dev.heinisch.menumaestro;

import dev.heinisch.menumaestro.properties.JwtProperties;
import dev.heinisch.menumaestro.properties.PasswordResetProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
@EnableConfigurationProperties({JwtProperties.class, PasswordResetProperties.class})
public class BackendApplication {

	public static void main(String[] args) {

		SpringApplication.run(BackendApplication.class, args);
	}

}
