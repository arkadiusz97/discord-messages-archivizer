package com.github.arkadiusz97.discordmessagesarchivizer.service.messages;

import com.github.arkadiusz97.discordmessagesarchivizer.entity.DiscordMessage;
import com.github.arkadiusz97.discordmessagesarchivizer.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MessagesServiceImplTest {

    @InjectMocks
    private MessagesServiceImpl messagesServiceImpl;

    @Mock
    private MessageRepository messageRepository;

    @Test
    public void shouldSaveMessage() {
        DiscordMessage discordMessageIn = mock(DiscordMessage.class);
        DiscordMessage discordMessageOut = mock(DiscordMessage.class);
        when(messageRepository.save(discordMessageIn)).thenReturn(discordMessageOut);

        DiscordMessage resultDiscordMessage = messagesServiceImpl.save(discordMessageIn);

        assertEquals(discordMessageOut, resultDiscordMessage);
    }

}