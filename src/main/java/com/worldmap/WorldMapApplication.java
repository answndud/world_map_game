package com.worldmap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class WorldMapApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorldMapApplication.class, args);
	}
}
