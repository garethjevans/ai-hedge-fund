package org.garethjevans.ai.mcp.financialdatasets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
public class FinancialDatasetsMcpServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinancialDatasetsMcpServerApplication.class, args);
	}

}
