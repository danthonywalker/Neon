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
package technology.yockto.neon.discord.cmd

import discord4j.command.util.AbstractCommandDispatcher
import discord4j.core.event.domain.message.MessageCreateEvent
import org.reactivestreams.Publisher
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
@Suppress("KDocMissingDocumentation")
class NeonCommandDispatcher : AbstractCommandDispatcher() {

    override fun getPrefixes(event: MessageCreateEvent): Publisher<String> = Mono.just("/")
}
