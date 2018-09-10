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

import com.fasterxml.jackson.databind.ObjectMapper
import discord4j.core.event.domain.Event
import org.reactivestreams.Publisher
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.publisher.Mono
import technology.yockto.neon.db.document.ChannelDocument
import technology.yockto.neon.db.repository.ChannelRepository
import technology.yockto.neon.discord.event.EventListener
import technology.yockto.neon.game.GameType
import technology.yockto.neon.web.rest.channel.CheckpointResponse
import java.math.BigInteger
import java.util.Queue

@Suppress("KDocMissingDocumentation")
abstract class AbstractCheckpointEventListener<T : Event>(
    private val gameType: GameType,
    private val type: String
) : EventListener<T> {

    @Autowired
    private lateinit var channelRepository: ChannelRepository

    @Autowired
    private lateinit var beanFactory: BeanFactory

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Suppress("UNCHECKED_CAST")
    final override fun apply(t: T): Publisher<*> {
        @Suppress("UNCHECKED_CAST") // This is needed because of a circular bean dependency chain
        val queues = beanFactory.getBean("queues") as Map<BigInteger, Queue<CheckpointResponse>>
        val channelId = getChannelId(t)

        return Mono.just(channelId)
            .filter(queues::containsKey)
            .flatMap(channelRepository::findById)
            .filter { (it.gameType == gameType) }
            .flatMap { getPayload(t, it) }
            .map { objectMapper.convertValue(it, Map::class.java) }
            .map { CheckpointResponse(type, it as Map<String, Any?>) }
            .doOnNext { queues[channelId]?.offer(it) }
    }

    protected abstract fun getPayload(event: T, channelDocument: ChannelDocument): Mono<Any>
    protected abstract fun getChannelId(event: T): BigInteger
}
