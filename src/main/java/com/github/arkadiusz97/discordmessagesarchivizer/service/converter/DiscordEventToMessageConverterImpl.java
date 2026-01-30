package com.github.arkadiusz97.discordmessagesarchivizer.service.converter;

import com.github.arkadiusz97.discordmessagesarchivizer.entity.DiscordMessage;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.AttachmentData;
import org.springframework.stereotype.Service;

@Service
public class DiscordEventToMessageConverterImpl implements DiscordEventToMessageConverter {

    @Override
    public DiscordMessage convert(MessageCreateEvent event) {
        var messageData = event.getMessage().getData();
        return new DiscordMessage(
                messageData.id().asLong(),
                messageData.channelId().asLong(),
                messageData.guildId().toOptional().orElse(Id.of(-1L)).asLong(),messageData.author().id().asLong(),
                messageData.content(), messageData.timestamp(),
                messageData.attachments().stream().map(AttachmentData::url).toList()
        );
    }

}
