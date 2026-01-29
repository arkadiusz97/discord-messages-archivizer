package com.github.arkadiusz97.discordmessagesarchivizer.service;

import com.github.arkadiusz97.discordmessagesarchivizer.entity.DiscordMessage;
import com.github.arkadiusz97.discordmessagesarchivizer.repository.MessageRepository;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiscordMessagesHandler implements MessagesHandler {

    private final MessageRepository messageRepository;

    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 30_000))
    public void handle(DiscordMessage in, Message message, Channel channel) throws IOException {
        log.debug("Received message {}", in);
        messageRepository.save(in);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        log.debug("Message {} saved successfully", in);
    }

    @Recover
    public void handleFatalErrors(Exception e, DiscordMessage in, Message message, Channel channel) throws IOException {
        log.error("Fatal error occurred after failed recover attempts for message {}", in, e);
        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
    }

}
