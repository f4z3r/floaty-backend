package ch.floaty;

import ch.floaty.run.FloatyApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("local-h2")
@SpringBootTest(classes = FloatyApplication.class)
class FloatyApplicationTests {

	@Test
	void contextLoads() {
	}

}
