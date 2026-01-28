package com.github.arkadiusz97.discordmessagesarchivizer;

import com.github.arkadiusz97.discordmessagesarchivizer.entity.DiscordMessage;
import com.github.arkadiusz97.discordmessagesarchivizer.service.DiscordEventToMessageConverter;
import com.github.arkadiusz97.discordmessagesarchivizer.service.MessagesListener;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessagesListenerTest {

    @InjectMocks
    private MessagesListener messagesListener;

    @Mock
    private GatewayDiscordClient gatewayDiscordClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private DiscordEventToMessageConverter converter;

    @Test
    public void shouldSubscribeMessageCreateEventAndLogoutAtTheEnd() {
        var queue = "test-queue";
        ReflectionTestUtils.setField(messagesListener, "queueName", queue);
        var content = "message";
        var channelId = 1L;
        var messageId = 2L;
        var guildId = 3L;
        var authorId = 4L;
        var timestamp = "timestamp";
        var attachmentUrl = "attachment-url";

        var event = mock(MessageCreateEvent.class);

        when(converter.convert(event)).thenReturn(new DiscordMessage(messageId, channelId, guildId, authorId, content, timestamp, List.of(attachmentUrl)));

        when(gatewayDiscordClient.on(MessageCreateEvent.class)).thenReturn(Flux.just(event));

        var logout = mock(Mono.class);
        when(gatewayDiscordClient.logout()).thenReturn(logout);

        messagesListener.init();
        messagesListener.shutdown();

        ArgumentCaptor<DiscordMessage> captor = ArgumentCaptor.forClass(DiscordMessage.class);
        verify(rabbitTemplate).convertAndSend(eq(queue), captor.capture());

        var sent = captor.getValue();
        assertThat(sent.content()).isEqualTo(content);
        assertThat(sent.channelId()).isEqualTo(channelId);
        assertThat(sent.messageId()).isEqualTo(messageId);
        assertThat(sent.guildId()).isEqualTo(guildId);
        assertThat(sent.authorId()).isEqualTo(authorId);
        assertThat(sent.timestamp()).isEqualTo(timestamp);
        assertThat(sent.attachmentsUrls()).hasSize(1);
        assertThat(sent.attachmentsUrls().getFirst()).isEqualTo(attachmentUrl);

        verify(logout, times(1)).block();
    }

    @Test
    public void shouldDoNothingWhenClientIsNullDuringShutdown() {
        var rabbitTemplate = mock(RabbitTemplate.class);
        var logout = mock(Mono.class);

        var messagesListener = new MessagesListener(rabbitTemplate, null, "test-queue", converter);

        messagesListener.shutdown();

        verify(logout, never()).block();
    }

}
