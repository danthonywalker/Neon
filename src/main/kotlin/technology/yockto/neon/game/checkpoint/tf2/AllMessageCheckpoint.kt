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
package technology.yockto.neon.game.checkpoint.tf2

import discord4j.core.`object`.entity.Member
import discord4j.core.event.domain.message.MessageCreateEvent
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import technology.yockto.neon.db.document.ChannelDocument
import technology.yockto.neon.game.GameType.TEAM_FORTRESS_2
import technology.yockto.neon.game.checkpoint.AbstractCheckpointEventListener
import java.math.BigInteger

@Component
@Suppress("KDocMissingDocumentation")
class AllMessageCheckpoint : AbstractCheckpointEventListener<MessageCreateEvent>(TEAM_FORTRESS_2, "NEONSM_MESSAGE") {

    override fun getPayload(event: MessageCreateEvent, channelDocument: ChannelDocument): Mono<Any> {
        // TODO Allow customization of the sending message by utilizing channelDocument and the event

        return Mono.justOrEmpty(event.member)
            .filter { !it.isBot }
            .map(Member::getDisplayName)
            .zipWith(Mono.justOrEmpty(event.message.content))
            .map { Payload("{orange}[Discord] {green}${it.t1}{normal}: ${it.t2}") }
    }

    override fun getChannelId(event: MessageCreateEvent): BigInteger = event.message.channelId.asBigInteger()

    private data class Payload(val message: String)
}
