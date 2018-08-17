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

import discord4j.command.CommandProvider
import discord4j.command.ProviderContext
import discord4j.core.event.domain.message.MessageCreateEvent
import org.reactivestreams.Publisher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import technology.yockto.neon.util.Node

@Component
@Suppress("KDocMissingDocumentation")
class NeonCommandProvider @Autowired constructor(
    private val neonCommands: List<NeonCommand>
) : CommandProvider<List<String>> {

    override fun provide(
        context: MessageCreateEvent,
        commandName: String,
        commandStartIndex: Int,
        commandEndIndex: Int
    ): Publisher<ProviderContext<List<String>>> {

        val arguments = context.message.content.orElseThrow()
            .substring(commandStartIndex, commandEndIndex)
            .split(' ')

        return Flux.fromIterable(neonCommands)
            .filter { (it.parent == null) }
            .filter { it.names.contains(commandName) }
            .singleOrEmpty()
            .map { getCommand(it, arguments) }
    }

    fun getChildren(neonCommand: NeonCommand): Node<NeonCommand> {
        return neonCommands.filter { (it.parent == neonCommand) }
            .fold(Node(neonCommand)) { root, child ->
                // Root children is all filtered elements and root is all children's parents
                root.copy(children = root.children + getChildren(child).copy(parent = root))
            }
    }

    fun getCommand(root: NeonCommand, arguments: List<String>, index: Int = 0): ProviderContext<List<String>> {
        return getChildren(root).getCommand(arguments, index) // Builds hierarchy so children are also included
    }

    private fun Node<NeonCommand>.getCommand(arguments: List<String>, index: Int): ProviderContext<List<String>> {
        return children.firstOrNull { it.data.names.contains(arguments.getOrNull(index)) }
            ?.getCommand(arguments, index + 1) // An argument matched an alias so shift up
            ?: ProviderContext.of(data, arguments.subList(index, arguments.size))
    }
}
