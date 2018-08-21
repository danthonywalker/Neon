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
package technology.yockto.neon.discord.event

import discord4j.core.event.domain.message.MessageCreateEvent
import org.reactivestreams.Publisher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import technology.yockto.neon.db.document.MemberDocument
import technology.yockto.neon.db.repository.MemberRepository
import technology.yockto.neon.util.MemberId
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Component
@Suppress("KDocMissingDocumentation")
class CreditEarnedEvent @Autowired constructor(
    private val memberRepository: MemberRepository
) : EventListener<MessageCreateEvent> {

    private val credits = ConcurrentHashMap<MemberId, AtomicInteger>()

    init { // Periodically saves the credits
        Flux.interval(Duration.ofMinutes(1))
            .map { credits.toMap() } // Copy
            .doOnNext { credits.clear() }
            .flatMap { credits ->

                memberRepository.findAllById(credits.keys)
                    .map { it.copy(totalCredits = it.totalCredits + credits[it.id]!!.get()) }
                    .transform { memberRepository.saveAll(it) }
                    .map(MemberDocument::id)
                    .collectList()
                    .map { Pair(credits.keys.toMutableList(), it) }
                    .doOnNext { (keys, processed) -> keys.removeAll(processed) }
                    .flatMapIterable(Pair<Iterable<MemberId>, *>::first)
                    .map { MemberDocument(it, totalCredits = credits[it]!!.toLong()) }
                    .transform { memberRepository.saveAll(it) }
            }.subscribe()
    }

    override fun apply(t: MessageCreateEvent): Publisher<*> {
        val defaultValue: (MemberId) -> AtomicInteger = { AtomicInteger(0) }

        return Mono.justOrEmpty(t.member)
            .map { MemberId(it.id.asBigInteger(), it.guildId.asBigInteger()) }
            .map { credits.computeIfAbsent(it, defaultValue) }
            // TODO Allow guilds to customize maximum credits
            .map { it.set(Math.min(it.get() + 1, 2)) }
    }
}
