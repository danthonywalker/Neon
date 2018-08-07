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
package technology.yockto.neon.web.rest.channel

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import technology.yockto.neon.db.document.ChannelDocument
import technology.yockto.neon.db.repository.ChannelRepository
import java.math.BigInteger
import java.time.Duration
import java.time.Instant
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue

@RestController
@RequestMapping("/channels/{id}")
@Suppress("KDocMissingDocumentation")
class ChannelRestController @Autowired constructor(
    @Value("\${NEON_QUEUE_TIMEOUT:10000}") private val queueTimeout: Int,
    private val eventListeners: Iterable<EventListener>,
    private val channelRepository: ChannelRepository
) {

    private val checkpoints = ConcurrentHashMap<BigInteger, Instant>()
    private val privateQueues = ConcurrentHashMap<BigInteger, Queue<CheckpointResponse>>()
    val queues: Map<BigInteger, Queue<CheckpointResponse>> @Bean("queues") get() = privateQueues

    @PostMapping("/checkpoint")
    fun postCheckpoint(@PathVariable("id") id: BigInteger): Flux<CheckpointResponse> {
        val queue = privateQueues.computeIfAbsent(id) { LinkedBlockingQueue() }
        val currentTime = Instant.now() // Sets the time to remove mapped queue
        val checkpoint = currentTime.plusMillis(queueTimeout.toLong())
        checkpoints[id] = checkpoint

        val cleanup = Mono.delay(Duration.between(currentTime, checkpoint))
            .filter { checkpoints.remove(id, checkpoint) }
            .filter { privateQueues.remove(id, queue) }
            .flatMapIterable { queue }

        val checkpointResponses = ArrayList<CheckpointResponse>(queue.size)
        var checkpointResponse = queue.poll()

        while (checkpointResponse != null) {
            checkpointResponses.add(checkpointResponse)
            checkpointResponse = queue.poll()
        }

        return Flux.first(Flux.fromIterable(checkpointResponses), cleanup)
    }

    @PostMapping("/events")
    fun postEvent(@PathVariable("id") id: BigInteger, request: EventRequest): Mono<Void> {
        val executors: (ChannelDocument) -> Flux<Void> = { channelDocument ->
            Flux.fromIterable(eventListeners) // Ordering is not considered
                .flatMap { it.execute(id, request, channelDocument) }
        }

        return Mono.just(id)
            .filter(queues::containsKey)
            .flatMap(channelRepository::findById)
            .flatMapMany(executors)
            .then()
    }
}
