package com.debrajkundu.grabpic;

import com.debrajkundu.grabpic.storage.property.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties(StorageProperties.class)
@EnableScheduling
public class GrabpicApplication {

	public static void main(String[] args) {
		SpringApplication.run(GrabpicApplication.class, args);
	}

}
