package br.edu.umc.loginseguro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LoginSeguroApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoginSeguroApplication.class, args);
    }
}
