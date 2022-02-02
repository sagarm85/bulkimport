package com.example.demo;

import com.example.demo.Import.util.ApiConstants;
import java.util.concurrent.Executor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@EnableAsync
public class ImportApplication {



	public static void main(String[] args) {
		SpringApplication.run(ImportApplication.class, args);
	}

	@Bean(ApiConstants.IMPORT_PROCESSOR_THREAD)
	public Executor threadPoolTaskExecutor() {
		final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(ApiConstants.CORE_POOL_SIZE);
		executor.setMaxPoolSize(ApiConstants.MAX_POOL_SIZE);
		executor.setQueueCapacity(ApiConstants.QUEUE_CAPACITY);
		return executor;
	}

}
