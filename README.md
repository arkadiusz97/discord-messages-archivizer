# Discord messages archivizer
Discord messages archivizer is a Discord's bot for archvizing messages sent on a Discord's server in Elasticsearch index.
Saved messages can be viewed in Kibana.

## Getting started
### Configure your bot instance on Discord site
Before run, you need to create and configure your bot instance in Discord Developers portal https://discord.com/developers/applications
Then give your bot permission, which allows viewing messages.
In the end, install bot on selected Discord server.
For more information, please visit https://discord.com/developers/docs/quick-start/getting-started

### Build and run locally
At first, you need to set the mandatory environment variable **DISCORD_BOT_TOKEN_MESSAGES_ARCHIVIZER**.
Then run command:
```
docker compose up -d
```

## Example improvements, which can be done
* Save more fields from a received message from Discord's API.
* Add integration tests.
* Consider retry only in case of specific exceptions in DiscordMessagesHandlerImpl.handle.
* Add properties for configure backoff delay and max attempts for saving messages.
* Configure SSH options for Elasticsearch and Kibana.

### License
The application is licensed under the MIT license.