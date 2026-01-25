package com.github.arkadiusz97.discordmessagesarchivizer.service;

import com.github.arkadiusz97.discordmessagesarchivizer.entity.DiscordMessage;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;

public interface MessagesHandler {
    void handle(DiscordMessage in, Message message, Channel channel) throws Exception;
}
