package com.github.arkadiusz97.discordmessagesarchivizer.service;

import com.github.arkadiusz97.discordmessagesarchivizer.service.converter.DiscordEventToMessageConverter;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MessagesListener {

    private final GatewayDiscordClient client;
    private final RabbitTemplate RabbitTemplate;
    private final String queueName;
    private final DiscordEventToMessageConverter converter;

    public MessagesListener(RabbitTemplate rabbitTemplate, GatewayDiscordClient client,
            @Value("${app.queue-name}") String queueName, DiscordEventToMessageConverter converter) {
        this.RabbitTemplate = rabbitTemplate;
        this.client = client;
        this.queueName = queueName;
        this.converter = converter;
    }

    @PostConstruct
    public void init() {
        client.on(MessageCreateEvent.class).subscribe(event -> {
            RabbitTemplate.convertAndSend(queueName, converter.convert(event));
        });
    }

    @PreDestroy
    public void shutdown() {
        if (client != null) {
            client.logout().block();
        }
    }

}
