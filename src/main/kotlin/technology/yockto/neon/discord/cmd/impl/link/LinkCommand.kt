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
package technology.yockto.neon.discord.cmd.impl.link

import discord4j.core.event.domain.message.MessageCreateEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import technology.yockto.neon.discord.cmd.NeonCommand
import technology.yockto.neon.discord.cmd.impl.HelpCommand

@Component
@Suppress("KDocMissingDocumentation")
class LinkCommand @Autowired constructor(
    private val helpCommand: HelpCommand
) : NeonCommand {

    override val helpDescription: String = "Create, read, update, and delete Neon's links to this guild's " +
        "text channels for Discord <-> gaming communication. Use `help link [alias]` for more information!"
    override val helpTitle: String = "Link Command"

    override val names: Set<String> = setOf("l", "link")
    override val parent: NeonCommand? = null

    override fun execute(event: MessageCreateEvent, context: List<String>?): Mono<Void> {
        return helpCommand.executeHelp(event, this).then()
    }
}
