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
package technology.yockto.neon.discord.command

import discord4j.command.Command
import discord4j.command.CommandProvider
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import technology.yockto.neon.db.document.GuildDocument
import technology.yockto.neon.db.repository.GuildRepository

@Component
@Suppress("KDocMissingDocumentation")
class NeonCommandProvider @Autowired constructor(
    private val guildRepository: GuildRepository,
    private val neonCommands: List<NeonCommand>
) : CommandProvider {

    override fun provide(context: MessageCreateEvent): Mono<out Command> {
        val aliasMatcher: (Pair<String, String>) -> Mono<NeonCommand> = { (prefix, alias) ->
            // Aliases in prefix will not match for NeonCommand#getArguments stability
            Mono.justOrEmpty(neonCommands.firstOrNull { neonCommand ->

                neonCommand.aliases.takeIf { aliases ->
                    aliases.none { prefix.contains(it) }
                }?.contains(alias) ?: false
            })
        }

        return Mono.justOrEmpty(context.guildId)
            .filter { context.message.content.isPresent }
            .filterWhen { context.message.author.map(User::isBot).map(Boolean::not) }
            .map(Snowflake::asBigInteger)
            .flatMap(guildRepository::findById)
            .flatMapIterable(GuildDocument::prefixes)
            .switchIfEmpty(Mono.just("/"))
            .filter { context.message.content.get().startsWith(it) }
            .next()
            .map { Pair(it, context.message.content.get().replaceFirst(it, "")) }
            .map { it.copy(second = it.second.split(" ")[0]) }
            .flatMap(aliasMatcher)
    }
}
