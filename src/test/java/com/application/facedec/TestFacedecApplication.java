package com.application.facedec;

import org.springframework.boot.SpringApplication;

public class TestFacedecApplication {

	public static void main(String[] args) {
		SpringApplication.from(FacedecApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
