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

import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.TextChannel
import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.message.MessageCreateEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.util.function.component1
import reactor.util.function.component2
import technology.yockto.neon.db.document.ChannelDocument
import technology.yockto.neon.db.repository.ChannelRepository
import technology.yockto.neon.discord.cmd.NeonCommand
import technology.yockto.neon.game.GameType
import technology.yockto.neon.util.createMessage
import technology.yockto.neon.util.waitForReply
import java.awt.Color
import java.time.Duration
import java.util.Base64
import java.util.UUID

@Component
@Suppress("KDocMissingDocumentation")
class CreateLinkCommand @Autowired constructor(
    private val channelRepository: ChannelRepository,
    linkCommand: LinkCommand
) : NeonCommand {

    override val helpDescription: String = "Creates a new Neon link to the guild text channel this command was " +
        "executed in for Discord <-> gaming communication. Only guild owners may use this command and it cannot be " +
        "used on preexisting links. To update an existing link, use `link update` in the linked guild text channel."
    override val names: Set<String> = setOf("create")

    override val helpTitle: String = "Link Command | Create"
    override val parent: NeonCommand? = linkCommand

    override fun execute(event: MessageCreateEvent, context: List<String>?): Mono<Void> {
        return Mono.zip(event.guild, event.message.channel.ofType(TextChannel::class.java))
            .filter { (guild, _) -> (guild.ownerId == event.message.authorId.orElse(null)) }
            .filterWhen { (_, channel) -> channelRepository.existsById(channel.id.asBigInteger()).map(Boolean::not) }
            .flatMap { (_, channel) ->

                channel.createMessage(event) {
                    val gameTypes = GameType.values().foldIndexed("") { index, message, gameType ->
                        "$message\n${index + 1} : ${gameType.alias}" // Increment index for clarity
                    }

                    it.setDescription("In order for Neon to properly communicate, the game type must be configured. " +
                        "Please reply with the __***number***__ corresponding to the desired game type.\n$gameTypes")
                    it.setTitle("Selecting Game Type")
                    it.setColor(Color.YELLOW)

                }.then(event.waitForReply(Duration.ofMinutes(1)))
            }.map { Pair(it, it.message.content.map(String::toIntOrNull).orElse(null)?.minus(1)) }
            .map { (event, index) -> Pair(event, index?.let(GameType.values()::getOrNull)) }
            .filter { (_, gameType) -> (gameType != null) }
            .flatMap { (event, gameType) ->
                val password = UUID.randomUUID()
                val id = event.message.channelId.asBigInteger()
                channelRepository.save(ChannelDocument(id, password, gameType!!))
                    .map { Base64.getEncoder().encodeToString("${it.id}:${it.password}".toByteArray()) }
                    .zipWith(event.message.channel.cast(TextChannel::class.java))
                    .flatMap { (token, channel) ->

                        channel.createMessage(event) {
                            it.setTitle("Successfully Generated Token")
                            it.setDescription("A token has been successfully generated for ${channel.mention} and " +
                                "will be privately messaged immediately. If you did not receive a message, please " +
                                "allow direct messages from server members temporarily and try **/info update**.")
                        }.thenReturn(Pair(token, channel))

                    }.zipWith(event.message.author.flatMap(User::getPrivateChannel))
                    .flatMap { (pair, channel) ->

                        channel.createMessage(event) {
                            it.setTitle("Generated Token")
                            it.setDescription("Your generated token for ${pair.second.mention} is **${pair.first}**\n" +
                                "If the token is lost or compromised, use **/info update** in ${pair.second.mention} " +
                                "immediately.\nThis message will automatically be deleted in 1 minute for security.")
                        }

                    }.delayElement(Duration.ofMinutes(1))
                    .flatMap(Message::delete)
            }
    }
}
