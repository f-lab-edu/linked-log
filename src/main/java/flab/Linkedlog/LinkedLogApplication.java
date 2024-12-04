package flab.Linkedlog;

import flab.Linkedlog.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
@EnableJpaAuditing
public class LinkedLogApplication {

	public static void main(String[] args) {
		SpringApplication.run(LinkedLogApplication.class, args);

	}

}
