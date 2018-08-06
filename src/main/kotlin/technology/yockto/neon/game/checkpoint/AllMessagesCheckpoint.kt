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
package technology.yockto.neon.game.checkpoint

import discord4j.core.`object`.entity.Member
import discord4j.core.event.domain.message.MessageCreateEvent
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import technology.yockto.neon.db.document.ChannelDocument
import technology.yockto.neon.web.rest.channel.CheckpointResponse
import java.math.BigInteger

@Component
@Suppress("KDocMissingDocumentation")
class AllMessagesCheckpoint : CheckpointEventListener<MessageCreateEvent>() {

    override fun getPayload(event: MessageCreateEvent, channelDocument: ChannelDocument): Mono<CheckpointResponse> {
        // TODO Change by gameType and allow total customization of the payload utilizing channelDocument and event

        return Mono.justOrEmpty(event.member)
            .map(Member::getDisplayName)
            .zipWith(Mono.justOrEmpty(event.message.content))
            .map { "{orange}[DSM] {green}${it.t1}{normal}: ${it.t2}" }
            .map { CheckpointResponse("ALL_MESSAGES", it) }
    }

    override fun getChannelId(event: MessageCreateEvent): BigInteger = event.message.channelId.asBigInteger()
}
