/*
 * This file is part of Neon.
 *
 * Neon is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Neon is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Neon.  If not, see <https://www.gnu.org/licenses/>.
 */
package technology.yockto.neon.discord

import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.event.domain.Event
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.stream.IntStream
import kotlin.streams.toList

@Configuration
@Suppress("KDocMissingDocumentation")
class DiscordConfiguration {

    @Bean
    fun getDiscordClients(
        @Value("\${DISCORD_SHARD_COUNT:1}") discordShardCount: Int,
        @Value("\${DISCORD_TOKEN}") discordToken: String,
        eventListeners: List<EventListener<*>>
    ): Iterable<DiscordClient> {

        @Suppress("UNCHECKED_CAST")
        fun DiscordClient.registerEventListeners() = eventListeners.map { it as EventListener<Event> }.forEach {
            eventDispatcher.on(it.eventType).flatMap(it::apply).subscribe() // Registers listeners to all shards
        }

        val discordClientBuilder = DiscordClientBuilder(discordToken).setShardCount(discordShardCount)

        return IntStream.range(0, discordShardCount)
            .mapToObj(discordClientBuilder::setShardIndex)
            .map(DiscordClientBuilder::build)
            .peek(DiscordClient::registerEventListeners)
            .peek { it.login().subscribe() }
            .toList()
    }
}
