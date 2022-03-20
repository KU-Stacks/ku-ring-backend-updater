package com.kustacks.kuring.worker;

import com.kustacks.kuring.worker.notifier.mq.RabbitMQNotifierConsumer;
import com.kustacks.kuring.worker.updater.mq.RabbitMQUpdaterConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@Slf4j
@SpringBootApplication
public class WorkerApplication {

	private final RabbitMQUpdaterConsumer updaterConsumer;
	private final RabbitMQNotifierConsumer notifierConsumer;

	public WorkerApplication(RabbitMQUpdaterConsumer updaterConsumer,
							 RabbitMQNotifierConsumer notifierConsumer) {
		this.updaterConsumer = updaterConsumer;
		this.notifierConsumer = notifierConsumer;
	}

	public static void main(String[] args) {
		SpringApplication.run(WorkerApplication.class, args);
	}

	@PostConstruct
	public void setTimezone() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		log.info("UpdaterApplication TimeZone = {}", TimeZone.getDefault());
	}

	@Autowired
	ApplicationContext applicationContext;

	@Bean
	public ApplicationRunner applicationRunner() {
		return args -> {
			updaterConsumer.listen();
			notifierConsumer.listen();

			Environment env = applicationContext.getEnvironment();
			String hikariPoolSize = env.getProperty("spring.datasource.hikari.maximum-pool-size");
			log.info("hikariPoolSize = {}", hikariPoolSize);
		};
	}
}
