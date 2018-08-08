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
package technology.yockto.neon.discord.event

import discord4j.core.event.domain.guild.GuildCreateEvent
import org.reactivestreams.Publisher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import technology.yockto.neon.db.document.GuildDocument
import technology.yockto.neon.db.repository.GuildRepository

@Component
@Suppress("KDocMissingDocumentation")
class GuildCreateDatabaseEventListener @Autowired constructor(
    private val guildRepository: GuildRepository
) : EventListener<GuildCreateEvent> {

    override fun apply(t: GuildCreateEvent): Publisher<*> {
        return Mono.just(t.guild.id.asBigInteger())
            .filterWhen { guildRepository.existsById(it).map(Boolean::not) }
            .map { GuildDocument(it) } // No parameters should be configured
            .flatMap { guildRepository.save(it) }
    }
}
