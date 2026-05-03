package com.github.arkadiusz97.discordmessagesarchivizer.service;

import com.github.arkadiusz97.discordmessagesarchivizer.entity.DiscordMessage;
import com.github.arkadiusz97.discordmessagesarchivizer.service.handler.DiscordMessagesHandler;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueListener {

    private final DiscordMessagesHandler discordMessagesHandler;

    @RabbitListener(queues = "${app.queue-name}")
    public void listen(DiscordMessage in, Message message, Channel channel) throws Exception {
        discordMessagesHandler.handle(in, message, channel);
    }

}
