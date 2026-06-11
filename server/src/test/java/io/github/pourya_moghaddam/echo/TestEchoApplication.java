package io.github.pourya_moghaddam.echo;

import org.springframework.boot.SpringApplication;

public class TestEchoApplication {

	public static void main(String[] args) {
		SpringApplication.from(EchoApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
