package car.leasing;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import java.math.BigDecimal;

@Configuration
public class RabbitConfig {
    private static final Logger log = LoggerFactory.getLogger(RabbitConfig.class);
    @Bean
    public MessageConverter jsonConverter() {
        ObjectMapper objectMapper = new ObjectMapper()
                .enable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN);
        objectMapper.configOverride(BigDecimal.class)
                .setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.STRING));
        return new Jackson2JsonMessageConverter(objectMapper);

    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonConverter());
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("✅ Сообщение {} доставлено в брокер",
                        correlationData != null ? correlationData.getId() : "");
            } else {
                log.error("❌ Сообщение {} НЕ доставлено. Причина: {}",
                        correlationData != null ? correlationData.getId() : "", cause);
            }
        });
        return rabbitTemplate;
    }
    @Bean
    public TopicExchange carEx() { return new TopicExchange("car.ex"); }
    @Bean
    public Queue carQueue() { return new Queue("carQueue"); }
    @Bean
    public Binding carBinding() { return BindingBuilder.bind(carQueue()).to(carEx()).with("car.#"); }
    @Bean
    public TopicExchange clientEx() { return new TopicExchange("client.ex"); }
    @Bean
    public Queue clientQueue() { return new Queue("clientQueue"); }
    @Bean
    public Binding clientBinding() { return BindingBuilder.bind(clientQueue()).to(clientEx()).with("client.#"); }
    @Bean
    public TopicExchange contractEx() { return new TopicExchange("contract.ex"); }
    @Bean
    public Queue contractQueue() { return new Queue("contractQueue"); }
    @Bean
    public Binding contractBinding() { return BindingBuilder.bind(contractQueue()).to(contractEx()).with("contract.#"); }
    @Bean
    public TopicExchange paymentEx() { return new TopicExchange("payment.ex"); }
    @Bean
    public Queue paymentQueue() { return new Queue("paymentQueue"); }
    @Bean
    public Binding paymentBinding() { return BindingBuilder.bind(paymentQueue()).to(paymentEx()).with("payment.#"); }


    @Bean
    public MessageRecoverer messageRecoverer() {
        return (message, cause) -> {
            System.out.println(message.getMessageProperties().getReceivedRoutingKey());
            log.error("3 попытки исчерпаны. Причина: ", cause);
        };
    }
    @Bean
    public RetryOperationsInterceptor retryInterceptor(MessageRecoverer messageRecoverer) {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(messageRecoverer)
                .build();
    }
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            RetryOperationsInterceptor retryInterceptor) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonConverter());
        factory.setAdviceChain(retryInterceptor);
        return factory;
    }
}
