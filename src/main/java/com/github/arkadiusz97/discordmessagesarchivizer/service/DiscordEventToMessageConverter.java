package com.github.arkadiusz97.discordmessagesarchivizer.service;

import com.github.arkadiusz97.discordmessagesarchivizer.entity.DiscordMessage;
import discord4j.core.event.domain.message.MessageCreateEvent;

public interface DiscordEventToMessageConverter {
    DiscordMessage convert(MessageCreateEvent event);
}
