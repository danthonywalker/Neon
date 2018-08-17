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
package technology.yockto.neon.game.event.msg

import discord4j.core.DiscordClient
import discord4j.core.`object`.util.Snowflake
import org.reactivestreams.Publisher
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.publisher.Mono
import reactor.util.function.component1
import reactor.util.function.component2
import technology.yockto.neon.db.document.ChannelDocument
import technology.yockto.neon.game.GameType
import technology.yockto.neon.util.format
import technology.yockto.neon.web.rest.channel.EventListener
import technology.yockto.neon.web.rest.channel.EventRequest

@Suppress("KDocMissingDocumentation")
abstract class AbstractMessageEventListener(
    final override val gameType: GameType,
    final override val eventType: String
) : EventListener {

    @Autowired
    private lateinit var discordClients: Iterable<DiscordClient>

    final override fun apply(t: EventRequest, u: ChannelDocument): Publisher<*> {
        return Mono.justOrEmpty(getRawString(u))
            .map { it.format(dictionary = t.payload) }
            .zipWith(discordClients.first().getTextChannelById(Snowflake.of(u.id)))
            .flatMap { (formatted, textChannel) -> textChannel.createMessage(formatted) }
    }

    protected abstract fun getRawString(u: ChannelDocument): String?
}
