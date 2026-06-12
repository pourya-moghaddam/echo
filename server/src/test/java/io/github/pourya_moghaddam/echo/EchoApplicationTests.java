package io.github.pourya_moghaddam.echo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class EchoApplicationTests {

	@Test
	void contextLoads_defaultContext_loadsSuccessfully() {
	}

}
