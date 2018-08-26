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
package technology.yockto.neon.db.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import technology.yockto.neon.util.MemberId

@Document("members")
@Suppress("KDocMissingDocumentation")
data class MemberDocument(
    @Id val id: MemberId,
    val creditsEarned: Long = 0,
    val creditsSpent: Long = 0
)
