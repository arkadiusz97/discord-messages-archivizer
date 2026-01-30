package com.github.arkadiusz97.discordmessagesarchivizer.service.converter;

import com.github.arkadiusz97.discordmessagesarchivizer.entity.DiscordMessage;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.AttachmentData;
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.UserData;
import discord4j.discordjson.possible.Possible;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DiscordEventToMessageConverterImplTest {

    private final DiscordEventToMessageConverterImpl discordEventToMessageConverterImpl;

    public DiscordEventToMessageConverterImplTest() {
        this.discordEventToMessageConverterImpl = new DiscordEventToMessageConverterImpl();
    }

    @ParameterizedTest
    @MethodSource("shouldConvertMessageCreateEventToDiscordMessageData")
    public void shouldConvertMessageCreateEventToDiscordMessage(boolean guildIdPresent) {
        var content = "message";
        var channelId = 1L;
        var messageId = 2L;
        var guildId = 3L;
        var authorId = 4L;
        var timestamp = "timestamp";
        var attachmentUrl = "attachment-url";

        var message = mock(Message.class);
        var messageData = mock(MessageData.class);
        var userData = mock(UserData.class);
        var attachmentData = mock(AttachmentData.class);
        var messageCreateEvent = mock(MessageCreateEvent.class);

        when(messageCreateEvent.getMessage()).thenReturn(message);
        when(attachmentData.url()).thenReturn(attachmentUrl);
        when(userData.id()).thenReturn(Id.of(authorId));
        when(message.getData()).thenReturn(messageData);
        when(messageData.content()).thenReturn(content);
        when(messageData.channelId()).thenReturn(Id.of(channelId));
        when(messageData.id()).thenReturn(Id.of(messageId));
        if (guildIdPresent) {
            when(messageData.guildId()).thenReturn(Possible.of(Id.of(guildId)));
        } else {
            when(messageData.guildId()).thenReturn(Possible.absent());
        }
        when(messageData.author()).thenReturn(userData);
        when(messageData.timestamp()).thenReturn(timestamp);
        when(messageData.attachments()).thenReturn(List.of(attachmentData));

        DiscordMessage discordMessage = discordEventToMessageConverterImpl.convert(messageCreateEvent);

        assertThat(discordMessage.content()).isEqualTo(content);
        assertThat(discordMessage.channelId()).isEqualTo(channelId);
        assertThat(discordMessage.messageId()).isEqualTo(messageId);
        if (guildIdPresent) {
            assertThat(discordMessage.guildId()).isEqualTo(guildId);
        } else {
            assertThat(discordMessage.guildId()).isEqualTo(-1L);
        }
        assertThat(discordMessage.authorId()).isEqualTo(authorId);
        assertThat(discordMessage.timestamp()).isEqualTo(timestamp);
        assertThat(discordMessage.attachmentsUrls()).hasSize(1);
        assertThat(discordMessage.attachmentsUrls().getFirst()).isEqualTo(attachmentUrl);

    }

    private static Stream<Arguments> shouldConvertMessageCreateEventToDiscordMessageData() {
        return Stream.of(
                Arguments.of(false),
                Arguments.of(true)
        );
    }

}
