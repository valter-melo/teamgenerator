package com.boraver.teamgenerator;

import com.boraver.teamgenerator.security.AppSecurityProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppSecurityProps.class)
public class TeamgeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(TeamgeneratorApplication.class, args);
	}

}
