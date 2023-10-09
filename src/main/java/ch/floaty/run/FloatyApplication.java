package ch.floaty.run;

import ch.floaty.config.FloatyApplicationConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(FloatyApplicationConfig.class)
public class FloatyApplication {

	public static void main(String[] args) {
		SpringApplication.run(FloatyApplication.class, args);
	}

}
