package io.metadew.admin;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAdminServer
public class IesiRestAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(IesiRestAdminApplication.class, args);
	}

}
