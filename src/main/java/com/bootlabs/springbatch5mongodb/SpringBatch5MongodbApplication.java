package com.bootlabs.springbatch5mongodb;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBatch5MongodbApplication {

	public static void main(String[] args) {
		System.exit(SpringApplication.exit(
				SpringApplication.run(SpringBatch5MongodbApplication.class, args)));
	}

}
