package com.github.arkadiusz97.discordmessagesarchivizer.service.messages;

import com.github.arkadiusz97.discordmessagesarchivizer.entity.DiscordMessage;
import com.github.arkadiusz97.discordmessagesarchivizer.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MessagesServiceImpl implements MessagesService {

    private final MessageRepository messageRepository;

    @Override
    public DiscordMessage save(DiscordMessage discordMessage) {
        return messageRepository.save(discordMessage);
    }

}
