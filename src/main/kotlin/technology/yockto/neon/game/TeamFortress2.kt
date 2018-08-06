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
package technology.yockto.neon.game

import java.math.BigInteger

@Suppress("KDocMissingDocumentation")
data class TeamFortress2(
    val onLogActionChannelId: BigInteger? = null,

    val genericOnPluginStart: String? = ":wrench: | Loaded **NeonSM** Plugin!",
    val genericOnPluginEnd: String? = ":wrench: | Unloaded **NeonSM** Plugin!",
    val genericOnMapStart: String? = ":map: | Started Map **\${name}**!",
    val genericOnMapEnd: String? = ":map: | Ended Map **\${name}**!",
    val genericOnClientConnected: String? = ":wave: | **\${name}** Connected!",
    val genericOnClientDisconnect: String? = ":wave: | **\${name}** Disconnected!",
    val genericPlayerSay: String? = ":speech_balloon: | **\${client.name}**: \${text}"
)
