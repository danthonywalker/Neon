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
package technology.yockto.neon.util

import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.`object`.util.Image.Format.GIF
import discord4j.core.`object`.util.Image.Format.PNG
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.MessageCreateSpec
import reactor.core.publisher.Mono
import reactor.util.function.component1
import reactor.util.function.component2
import java.time.Duration
import java.time.Instant

@Suppress("KDocMissingDocumentation")
fun MessageChannel.createMessage(event: MessageCreateEvent, spec: (EmbedCreateSpec) -> Unit): Mono<Message> {
    return Mono.zip(event.message.author, event.message.client.self).map { (author, self) ->
        val authorAvatarType = PNG.takeUnless { author.hasAnimatedAvatar() } ?: GIF
        val authorAvatarUrl = author.getAvatarUrl(authorAvatarType).orElse(author.defaultAvatarUrl)
        val selfAvatarUrl = self.getAvatarUrl(PNG).orElse(self.defaultAvatarUrl)
        val embedCreateSpec = EmbedCreateSpec()

        embedCreateSpec.setAuthor("${self.username}#${self.discriminator}", null, selfAvatarUrl)
        embedCreateSpec.setFooter("${author.username}#${author.discriminator}", authorAvatarUrl)
        embedCreateSpec.setThumbnail(selfAvatarUrl)
        embedCreateSpec.setTimestamp(Instant.now())

        embedCreateSpec.apply(spec) // Other properties are applied
    }.flatMap { createMessage(MessageCreateSpec().setEmbed(it)) }
}

@Suppress("KDocMissingDocumentation")
fun MessageCreateEvent.waitForReply(duration: Duration): Mono<MessageCreateEvent> {
    return client.eventDispatcher.on(MessageCreateEvent::class.java)
        .filter { (it.message.channelId == message.channelId) }
        .filter { (it.message.authorId == message.authorId) }
        .timeout(duration, Mono.empty())
        .next()
}

@Suppress("KDocMissingDocumentation", "UNCHECKED_CAST")
fun String.format(prefix: String = "", dictionary: Map<String, Any?>): String {
    return dictionary.asIterable().fold(this) { accumulator, (key, value) ->
        when (value) { // All expected types have pretty toStrings so only worry about Map
            is Map<*, *> -> accumulator.format("$prefix$key.", value as Map<String, Any?>)
            else -> accumulator.replace("\${$prefix$key}", value.toString())
        }
    }
}
