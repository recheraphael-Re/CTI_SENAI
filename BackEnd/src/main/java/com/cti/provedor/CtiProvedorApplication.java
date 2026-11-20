package com.cti.provedor;

import com.cti.provedor.config.AppProperties;
import com.cti.provedor.config.ContasCti;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class CtiProvedorApplication {

	public static void main(String[] args) {
		if (ContasCti.executar(args)) return;
		SpringApplication.run(CtiProvedorApplication.class, args);
	}

}
