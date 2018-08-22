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
package technology.yockto.neon.discord.cmd.impl.profile

import discord4j.core.`object`.util.Image.Format.PNG
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.util.function.component1
import reactor.util.function.component2
import technology.yockto.neon.db.document.MemberDocument
import technology.yockto.neon.db.repository.MemberRepository
import technology.yockto.neon.discord.cmd.NeonCommand
import technology.yockto.neon.util.MemberId
import technology.yockto.neon.util.createMessage
import java.awt.Color

@Component
@Suppress("KDocMissingDocumentation")
class ProfileCommand @Autowired constructor(
    private val memberRepository: MemberRepository
) : NeonCommand {

    override val helpDescription: String = "Get your profile for this " +
        "guild, or, use `profile [id]` for the profile of another user!"
    override val helpTitle: String = "Profile Command"

    override val names: Set<String> = setOf("p", "profile")
    override val parent: NeonCommand? = null

    override fun execute(event: MessageCreateEvent, context: List<String>?): Mono<Void> {
        return Mono.justOrEmpty(context!!.getOrNull(0)?.toBigIntegerOrNull())
            .switchIfEmpty(Mono.justOrEmpty(event.message.authorId).map(Snowflake::asBigInteger))
            .zipWith(Mono.justOrEmpty(event.guildId).map(Snowflake::asBigInteger))
            .map { (userId, guildId) -> MemberId(userId, guildId) }
            .flatMap { memberId ->

                memberRepository.findAllByGuildId(memberId.guildId)
                    .collectList() // Items will be sorted later on
                    .map { Pair(memberId, it) }
            }.zipWith(event.message.channel)
            .flatMap { (pair, channel) ->

                val guildId = event.guildId.orElseThrow()
                val userId = Snowflake.of(pair.first.userId)
                event.client.getMemberById(guildId, userId).flatMap { member ->
                    channel.createMessage(event) { spec ->

                        val memberAvatarUrl = member.getAvatarUrl(PNG).orElse(member.defaultAvatarUrl)
                        val document = pair.second.firstOrNull { it.id == pair.first } ?: MemberDocument(pair.first)
                        val topCurrent = pair.second.sortedBy { it.totalCredits - it.spentCredits }
                        val topTotal = pair.second.sortedBy(MemberDocument::totalCredits)
                        val topSpent = pair.second.sortedBy(MemberDocument::spentCredits)

                        val current = "(#${topCurrent.indexOf(document).takeUnless { it == -1 } ?: 0})"
                        spec.addField("Credits", "${document.totalCredits - document.spentCredits} $current", true)
                        val earned = "(#${topTotal.indexOf(document).takeUnless { it == -1 } ?: 0})"
                        spec.addField("Total Credits Earned", "${document.totalCredits} $earned", true)
                        val spent = "(#${topSpent.indexOf(document).takeUnless { it == -1 } ?: 0})"
                        spec.addField("Total Credits Spent", "${document.spentCredits} $spent", true)

                        spec.setAuthor("${member.displayName}#${member.discriminator}", null, memberAvatarUrl)
                        spec.setThumbnail(memberAvatarUrl)
                        spec.setColor(Color.MAGENTA)
                    }
                }
            }.then()
    }
}
