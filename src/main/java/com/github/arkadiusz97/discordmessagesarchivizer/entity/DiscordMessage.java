package com.github.arkadiusz97.discordmessagesarchivizer.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Document(indexName = "discord-message")
public record DiscordMessage(@Id Long messageId, Long channelId, Long guildId, Long authorId, String content,
    String timestamp, @Field(type = FieldType.Keyword) List<String> attachmentsUrls) {
}
