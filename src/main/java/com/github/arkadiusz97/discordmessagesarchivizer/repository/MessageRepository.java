package com.github.arkadiusz97.discordmessagesarchivizer.repository;

import com.github.arkadiusz97.discordmessagesarchivizer.entity.DiscordMessage;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface MessageRepository extends ElasticsearchRepository<DiscordMessage, Long> {
}
