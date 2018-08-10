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

import discord4j.core.`object`.util.Image.Format.PNG
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.EmbedCreateSpec
import reactor.core.publisher.Mono
import reactor.util.function.component1
import reactor.util.function.component2
import technology.yockto.neon.web.rest.channel.EventRequest
import java.math.RoundingMode.HALF_UP
import java.time.Instant

@Suppress("KDocMissingDocumentation")
fun Double.round(scale: Int): Double = toBigDecimal().setScale(scale, HALF_UP).toDouble()

@Suppress("KDocMissingDocumentation")
fun EventRequest.format(raw: String): String = format(raw, "", payload)

@Suppress("KDocMissingDocumentation")
fun MessageCreateEvent.createEmbed(spec: (EmbedCreateSpec) -> Unit): Mono<EmbedCreateSpec> {
    return Mono.zip(message.author, message.client.self).map { (author, self) ->
        val authorAvatarUrl = author.getAvatarUrl(PNG).orElse(author.defaultAvatarUrl)
        val selfAvatarUrl = self.getAvatarUrl(PNG).orElse(self.defaultAvatarUrl)
        val embedCreateSpec = EmbedCreateSpec()

        embedCreateSpec.setAuthor("${self.username}#${self.discriminator}", null, selfAvatarUrl)
        embedCreateSpec.setFooter("${author.username}#${author.discriminator}", authorAvatarUrl)
        embedCreateSpec.setThumbnail(selfAvatarUrl)
        embedCreateSpec.setTimestamp(Instant.now())
        embedCreateSpec.apply(spec::invoke)
    }
}

@Suppress("UNCHECKED_CAST")
private fun format(raw: String, prefix: String, dictionary: Map<String, Any>): String {
    return dictionary.asIterable().fold(raw) { accumulator, (key, value) ->
        when (value) { // All expected types have pretty toStrings so only worry about Map
            is Map<*, *> -> format(accumulator, "$prefix$key.", value as Map<String, Any>)
            else -> accumulator.replace("\${$prefix$key}", value.toString())
        }
    }
}
