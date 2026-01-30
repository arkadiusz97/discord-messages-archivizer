package com.github.arkadiusz97.discordmessagesarchivizer.service;

import com.github.arkadiusz97.discordmessagesarchivizer.entity.DiscordMessage;
import com.github.arkadiusz97.discordmessagesarchivizer.service.handler.DiscordMessagesHandler;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueueListenerTest {

    @Mock
    private DiscordMessagesHandler discordMessagesHandler;

    @Mock
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @InjectMocks
    private QueueListener queueListener;

    @Test
    void shouldHandleMessage() throws Exception {
        DiscordMessage discordMessage = mock(DiscordMessage.class);
        Message message = mock(Message.class);
        Channel channel = mock(Channel.class);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(threadPoolTaskExecutor).execute(any(Runnable.class));

        queueListener.listen(discordMessage, message, channel);

        verify(threadPoolTaskExecutor).execute(any(Runnable.class));
        verify(discordMessagesHandler).handle(discordMessage, message, channel);
    }

    @Test
    void shouldThrowException_whenMessagesHandlerThrows() throws Exception {
        DiscordMessage discordMessage = mock(DiscordMessage.class);
        Message message = mock(Message.class);
        Channel channel = mock(Channel.class);

        doThrow(RuntimeException.class).when(discordMessagesHandler).handle(discordMessage, message, channel);
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(threadPoolTaskExecutor).execute(any(Runnable.class));

        assertThrows(RuntimeException.class, () -> queueListener.listen(discordMessage, message, channel));
        verify(threadPoolTaskExecutor).execute(any(Runnable.class));
    }

}
