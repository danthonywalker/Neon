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

import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.TextChannel
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.MessageCreateSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.util.function.component1
import reactor.util.function.component2
import reactor.util.function.component3
import technology.yockto.neon.db.document.ChannelDocument
import technology.yockto.neon.db.repository.ChannelRepository
import technology.yockto.neon.util.createEmbed
import java.time.Duration
import java.util.Base64

@Component
@Suppress("KDocMissingDocumentation")
class LinkCommand @Autowired constructor(
    private val channelRepository: ChannelRepository
) : NeonCommand {

    override fun execute(event: MessageCreateEvent): Mono<Void> {
        return Mono.zip(event.guild, event.message.channel.ofType(TextChannel::class.java), event.message.author)
            .filter { (guild, _, author) -> (guild.ownerId == author.id) }
            .filterWhen { (_, channel, _) -> channelRepository.existsById(channel.id.asBigInteger()).map(Boolean::not) }
            .flatMap { (_, channel, author) ->

                channelRepository.save(ChannelDocument(channel.id.asBigInteger(), author.id.asBigInteger()))
                    .map { Base64.getEncoder().encodeToString("${it.id}:${it.password}".toByteArray()) }
                    .flatMap { token ->
                        event.createEmbed {

                            it.setTitle("Successfully Generated Token")
                            it.setDescription("A token has been successfully generated for ${channel.mention} and " +
                                "will be privately messaged immediately. If you did not receive a notification, " +
                                "please allow direct messages from server members temporarily and try **/modify**")
                        }.flatMap { channel.createMessage(MessageCreateSpec().setEmbed(it)).thenReturn(token) }
                    }.flatMap { token ->
                        event.createEmbed {

                            it.setTitle("Generated Token")
                            it.setDescription("Your generated token for ${channel.mention} is **$token**. If your " +
                                "token is lost or compromised, use **/modify** in ${channel.mention} immediately.\n" +
                                "This message will be automatically deleted in 1 minute for privacy and security.")
                        }.flatMap { channel.createMessage(MessageCreateSpec().setEmbed(it)) }
                    }.delayElement(Duration.ofMinutes(1))
                    .flatMap(Message::delete)
            }.then()
    }

    override val aliases: Set<String> = setOf("link")
}
