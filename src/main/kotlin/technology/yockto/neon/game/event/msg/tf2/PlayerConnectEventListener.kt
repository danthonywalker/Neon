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
package technology.yockto.neon.game.event.msg.tf2

import org.springframework.stereotype.Component
import technology.yockto.neon.db.document.ChannelDocument
import technology.yockto.neon.game.GameType.TEAM_FORTRESS_2
import technology.yockto.neon.game.event.msg.AbstractMessageEventListener

@Component
@Suppress("KDocMissingDocumentation")
class PlayerConnectEventListener : AbstractMessageEventListener(TEAM_FORTRESS_2, "PLAYER_CONNECT") {

    override fun getRawString(u: ChannelDocument): String? = u.teamFortress2.playerConnect
}
