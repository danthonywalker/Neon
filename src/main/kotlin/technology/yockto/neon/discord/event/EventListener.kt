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

import discord4j.core.event.domain.Event
import org.reactivestreams.Publisher
import org.springframework.core.GenericTypeResolver
import java.util.function.Function

@Suppress("KDocMissingDocumentation")
interface EventListener<T : Event> : Function<T, Publisher<*>> {

    @Suppress("UNCHECKED_CAST")
    val eventType: Class<T>
        get() = GenericTypeResolver.resolveTypeArgument(javaClass, EventListener::class.java) as Class<T>
}
