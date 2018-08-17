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

@Suppress("KDocMissingDocumentation")
data class TeamFortress2(
    val onPluginStart: String? = ":wrench: | Loaded **NeonSM** Plugin!",
    val onPluginEnd: String? = ":wrench: | Unloaded **NeonSM** Plugin!",
    val onMapStart: String? = ":map: | Started Map **\${name}**!",
    val onMapEnd: String? = ":map: | Ended Map **\${name}**!",
    val onClientConnected: String? = ":wave: | **\${name}** Connected!",
    val onClientDisconnect: String? = ":wave: | **\${name}** Disconnected!",
    val playerSay: String? = ":speech_balloon: | **\${client.name}**: \${text}"
)
