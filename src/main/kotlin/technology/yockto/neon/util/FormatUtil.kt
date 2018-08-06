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

import technology.yockto.neon.web.rest.channel.EventRequest

@Suppress("KDocMissingDocumentation")
fun EventRequest.format(raw: String): String = format(raw, "", payload)

@Suppress("UNCHECKED_CAST")
private fun format(raw: String, prefix: String, dictionary: Map<String, Any>): String {
    return dictionary.asIterable().fold(raw) { accumulator, (key, value) ->
        when (value) { // All expected types have pretty toStrings so only worry about Map
            is Map<*, *> -> format(accumulator, "$prefix$key.", value as Map<String, Any>)
            else -> accumulator.replace("\${$prefix$key}", value.toString())
        }
    }
}
