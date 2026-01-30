package com.github.arkadiusz97.discordmessagesarchivizer.service.messages;

import com.github.arkadiusz97.discordmessagesarchivizer.entity.DiscordMessage;

public interface MessagesService {

    DiscordMessage save(DiscordMessage discordMessage);

}
