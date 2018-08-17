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
package technology.yockto.neon.discord.cmd.impl

import discord4j.command.ProviderContext
import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.message.MessageCreateEvent
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import technology.yockto.neon.discord.cmd.NeonCommand
import technology.yockto.neon.discord.cmd.NeonCommandProvider
import technology.yockto.neon.util.Node
import technology.yockto.neon.util.createMessage
import java.awt.Color

@Component
@Suppress("KDocMissingDocumentation")
class HelpCommand @Autowired constructor(
    private val listableBeanFactory: ListableBeanFactory
) : NeonCommand {

    override val helpDescription: String = "Use `help [alias]` for more information!"
    override val helpTitle: String = "Help Command"

    override val names: Set<String> = setOf("h", "help")
    override val parent: NeonCommand? = null

    override fun execute(event: MessageCreateEvent, context: List<String>?): Mono<Void> {
        val neonCommandProvider = listableBeanFactory.getBean(NeonCommandProvider::class.java)

        return Mono.just(listableBeanFactory.getBeansOfType(NeonCommand::class.java))
            .flatMapIterable(Map<*, NeonCommand>::values)
            .filter { (it.parent == null) }
            .filter { it.names.contains(context!!.getOrNull(0)) }
            .switchIfEmpty(Mono.just(this))
            // Get the NeonCommand the hierarchy in context refers to
            .map { neonCommandProvider.getCommand(it, context!!, 1) }
            .map(ProviderContext<*>::getCommand)
            .cast(NeonCommand::class.java)
            .flatMap { executeHelp(event, it) }
            .then()
    }

    fun executeHelp(event: MessageCreateEvent, neonCommand: NeonCommand): Mono<Message> {
        // External method allows other NeonCommands to execute their own help
        return event.message.channel.flatMap { messageChannel ->
            messageChannel.createMessage(event) { spec ->

                val neonCommands = if (neonCommand == this) {
                    listableBeanFactory.getBeansOfType(NeonCommand::class.java).values
                        .filter { (it.parent == null) }
                } else { // HelpCommand will display root NeonCommand instances and other instances lists children
                    listableBeanFactory.getBean(NeonCommandProvider::class.java).getChildren(neonCommand).children
                        .map(Node<NeonCommand>::data)
                }

                val aliases = neonCommands.flatMap(NeonCommand::names)
                    .takeIf(List<*>::isNotEmpty)
                    ?.sorted() // Alphabetizes for better readability
                    ?.reduce { aliases, alias -> "$aliases, $alias" }

                aliases?.let { spec.addField("Available Aliases", it, false) }
                spec.setDescription(neonCommand.helpDescription)
                spec.setTitle(neonCommand.helpTitle)
                spec.setColor(Color.PINK)
            }
        }
    }
}
