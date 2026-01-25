package com.github.arkadiusz97.discordmessagesarchivizer.service;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import com.github.arkadiusz97.discordmessagesarchivizer.entity.DiscordMessage;
import com.github.arkadiusz97.discordmessagesarchivizer.repository.MessageRepository;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiscordMessagesHandler implements MessagesHandler {

    private final MessageRepository messageRepository;

    public void handle(DiscordMessage in, Message message, Channel channel) throws Exception {
        log.debug("Received message: {}", in.toString());
        try {
            messageRepository.save(in);
        } catch (ElasticsearchException e) {
            handleElasticsearchException(e, message, channel);
        } catch (RuntimeException e) {
            log.warn("Elasticsearch unreachable or network failure, will retry", e);
            negativeAcknowledge(message, channel);
        } catch (Exception e) {
            log.error("Non-retriable error, discarding message: {}", e.getMessage(), e);
            acknowledge(message, channel);
        }
        acknowledge(message, channel);
    }

    private void handleElasticsearchException(ElasticsearchException e, Message message, Channel channel)
            throws IOException {
        int status = e.status();
        if (isTransient(status)) {
            log.warn("Transient ES error, will retry. Nack message: {}", e.getMessage());
            negativeAcknowledge(message, channel);
        } else {
            log.error("Non-retriable http error {}, discarding message: {}", status, e.getMessage(), e);
            acknowledge(message, channel);
        }
    }

    private void negativeAcknowledge(Message message, Channel channel) throws IOException {
        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
    }

    private void acknowledge(Message message, Channel channel) throws IOException {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
    private boolean isTransient(int status) {
        return status == 500 || status == 503 || status == 429;
    }
}
