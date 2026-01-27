package com.github.arkadiusz97.discordmessagesarchivizer.config;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableRetry
@EnableResilientMethods
public class Config {

    @Bean
    public JacksonJsonMessageConverter jsonConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter converter, @Value("${app.queue-prefetch-count}") Integer queuePrefetchCount) {
        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPrefetchCount(queuePrefetchCount);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setMessageConverter(converter);
        return factory;
    }

    @Bean
    public Queue mainQueue(@Value("${app.queue-name}") String queueName) {
        return QueueBuilder.durable(queueName)
                .build();
    }

    @Bean
    public GatewayDiscordClient gatewayDiscordClient(@Value("${app.discord-bot-token}") String discordBotToken) {
        return DiscordClientBuilder.create(discordBotToken)
                .build()
                .login()
                .block();
    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutor(
            @Value("${app.thread-pool-size-for-messages-handler}") int threadPoolSizeForMessagesHandler
    ) {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setVirtualThreads(true);
        exec.setThreadNamePrefix("discord-messages-archivizer-handler-");
        exec.setCorePoolSize(threadPoolSizeForMessagesHandler);
        return exec;
    }

}
