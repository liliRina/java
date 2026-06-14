package web_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

//@SpringBootApplication(exclude = {
//		DataSourceAutoConfiguration.class,
//		DataSourceTransactionManagerAutoConfiguration.class,
//		HibernateJpaAutoConfiguration.class
//})
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableKafka
public class KaktusApplication {
	public static void main(String[] args) {
		SpringApplication.run(KaktusApplication.class, args);
	}
	@Bean
	public ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
			ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
			ConsumerFactory<Object, Object> kafkaConsumerFactory,
			DefaultErrorHandler errorHandler) {

		ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
		configurer.configure(factory, kafkaConsumerFactory);
		factory.setCommonErrorHandler(errorHandler);
		return factory;
	}
	@Bean
	public DefaultErrorHandler errorHandler() {
		FixedBackOff fixedBackOff = new FixedBackOff(1000L, 1);
		DefaultErrorHandler handler = new DefaultErrorHandler(fixedBackOff);

		handler.setRetryListeners((record, ex, deliveryAttempt) -> {
			System.err.printf("Ошибка: %s, попытка %d%n", record.value(), deliveryAttempt);
		});

		return handler;
	}

}
//cd /opt/kafka/bin  ./kafka-console-producer.sh --bootstrap-server localhost:9092 --topic weather
//docker exec -it kafka bash
//{"temperature": 22, "humidity": 65}