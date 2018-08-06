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

import discord4j.core.event.domain.Event
import org.reactivestreams.Publisher
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.publisher.Mono
import technology.yockto.neon.db.document.ChannelDocument
import technology.yockto.neon.db.repository.ChannelRepository
import technology.yockto.neon.discord.EventListener
import technology.yockto.neon.game.GameType.UNSPECIFIED
import technology.yockto.neon.web.rest.channel.CheckpointResponse
import java.math.BigInteger
import java.util.Queue

@Suppress("KDocMissingDocumentation")
abstract class CheckpointEventListener<T : Event> : EventListener<T> {

    @Autowired
    private lateinit var queues: Map<BigInteger, Queue<CheckpointResponse>>

    @Autowired
    private lateinit var channelRepository: ChannelRepository

    final override fun apply(t: T): Publisher<*> {
        val channelId = getChannelId(t)

        return Mono.just(channelId)
            .filter(queues::containsKey)
            .flatMap(channelRepository::findById)
            .filter { it.gameType != UNSPECIFIED }
            .flatMap { getPayload(t, it) }
            .doOnNext { queues[channelId]?.offer(it) }
    }

    protected abstract fun getPayload(event: T, channelDocument: ChannelDocument): Mono<CheckpointResponse>
    protected abstract fun getChannelId(event: T): BigInteger
}
