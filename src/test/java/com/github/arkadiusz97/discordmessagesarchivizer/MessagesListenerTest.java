package com.github.arkadiusz97.discordmessagesarchivizer;

import com.github.arkadiusz97.discordmessagesarchivizer.entity.DiscordMessage;
import com.github.arkadiusz97.discordmessagesarchivizer.service.DiscordEventToMessageConverter;
import com.github.arkadiusz97.discordmessagesarchivizer.service.MessagesListener;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.AttachmentData;
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.UserData;
import discord4j.discordjson.possible.Possible;
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

        var message = mock(Message.class);
        var messageData = mock(MessageData.class);
        var userData = mock(UserData.class);
        var attachmentData = mock(AttachmentData.class);
        var attachmentUrl = "attachment-url";
/*        when(attachmentData.url()).thenReturn(attachmentUrl);
        when(userData.id()).thenReturn(Id.of(authorId));
        when(message.getData()).thenReturn(messageData);
        when(messageData.content()).thenReturn(content);
        when(messageData.channelId()).thenReturn(Id.of(channelId));
        when(messageData.id()).thenReturn(Id.of(messageId));
        when(messageData.guildId()).thenReturn(Possible.of(Id.of(guildId)));
        when(messageData.author()).thenReturn(userData);
        when(messageData.timestamp()).thenReturn(timestamp);
        when(messageData.attachments()).thenReturn(List.of(attachmentData));*/

        var event = mock(MessageCreateEvent.class);
        //when(event.getMessage()).thenReturn(message);

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
