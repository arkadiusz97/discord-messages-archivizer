package com.github.arkadiusz97.discordmessagesarchivizer.service.handler;

import com.github.arkadiusz97.discordmessagesarchivizer.entity.DiscordMessage;
import com.github.arkadiusz97.discordmessagesarchivizer.service.messages.MessagesService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DiscordMessagesHandlerImplTest {

    @InjectMocks
    private DiscordMessagesHandlerImpl discordMessagesHandlerImpl;

    @Mock
    private MessagesService messagesService;

    @Mock
    private Message message;

    @Mock
    private Channel channel;

    @Mock
    private MessageProperties messageProperties;

    @Mock
    private DiscordMessage discordMessage;

    private final long deliveryTag = 1L;

    @Test
    public void shouldAcknowledgeMessageWhenSavedSuccessfully() throws IOException {
        when(messagesService.save(discordMessage)).thenReturn(discordMessage);
        when(message.getMessageProperties()).thenReturn(messageProperties);
        when(messageProperties.getDeliveryTag()).thenReturn(deliveryTag);

        discordMessagesHandlerImpl.handle(discordMessage, message, channel);

        verify(messagesService).save(discordMessage);
        verify(channel).basicAck(deliveryTag, false);
    }

    @Test
    public void shouldNotAcknowledgeMessageWhenNotSavedSuccessfully() {
        when(messagesService.save(discordMessage)).thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class,
                () -> discordMessagesHandlerImpl.handle(discordMessage, message, channel)
        );

        verifyNoInteractions(channel);
    }

    @Test
    public void shouldHandleFatalError() throws IOException {
        var exception = mock(Exception.class);
        when(message.getMessageProperties()).thenReturn(messageProperties);
        when(messageProperties.getDeliveryTag()).thenReturn(deliveryTag);

        discordMessagesHandlerImpl.handleFatalError(exception, discordMessage, message, channel);

        verify(channel).basicNack(deliveryTag, false, false);
    }

}
