package br.com.impactar;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ImpactarApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(br.com.impactar.ImpactarApiApplication.class, args);
	}
}