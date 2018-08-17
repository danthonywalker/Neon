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

import discord4j.core.DiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.util.function.component1
import reactor.util.function.component2
import technology.yockto.neon.discord.cmd.NeonCommand

@Component
@Suppress("KDocMissingDocumentation")
class LogoutCommand @Autowired constructor(
    private val beanFactory: BeanFactory
) : NeonCommand {

    override val helpDescription: String = "Disconnects the bot from " +
        "Discord. Only the bot owner may execute this command."
    override val helpTitle: String = "Logout Command"

    override val names: Set<String> = setOf("logout")
    override val parent: NeonCommand? = null

    override fun execute(event: MessageCreateEvent, context: List<String>?): Mono<Void> {
        return event.message.author.zipWith(event.client.applicationInfo)
            .filter { (author, applicationInfo) -> (author.id == applicationInfo.ownerId) }
            .flatMapIterable { beanFactory.getBean("discordClients", Iterable::class.java) }
            .cast(DiscordClient::class.java)
            .map(DiscordClient::logout)
            .then()
    }
}
