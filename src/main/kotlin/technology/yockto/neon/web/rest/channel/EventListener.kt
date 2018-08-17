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

import org.reactivestreams.Publisher
import technology.yockto.neon.db.document.ChannelDocument
import technology.yockto.neon.game.GameType
import java.util.function.BiFunction

@Suppress("KDocMissingDocumentation")
interface EventListener : BiFunction<EventRequest, ChannelDocument, Publisher<*>> {

    val gameType: GameType
    val eventType: String
}
