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
package technology.yockto.neon.game.event.sm.generic

import com.fasterxml.jackson.databind.ObjectMapper
import discord4j.core.DiscordClient
import discord4j.core.`object`.util.Snowflake
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import technology.yockto.neon.db.document.ChannelDocument
import technology.yockto.neon.game.GameType.TEAM_FORTRESS_2
import technology.yockto.neon.util.format
import technology.yockto.neon.web.rest.channel.EventListener
import technology.yockto.neon.web.rest.channel.EventRequest

@Component
@Suppress("KDocMissingDocumentation")
class GenericEventListener @Autowired constructor(
    private val discordClients: Iterable<DiscordClient>,
    private val objectMapper: ObjectMapper
) : EventListener {

    override fun execute(request: EventRequest, channelDocument: ChannelDocument): Mono<Void> {
        // TODO Possibly clean this up. Maybe allow "type" to lookup / execute required method

        return Mono.just(request)
            .filter { it.type == "GENERIC" }
            .map { objectMapper.convertValue(it.payload, EventRequest::class.java) }
            .flatMap { Mono.justOrEmpty(getRawString(channelDocument, request)?.let(it::format)) }
            .flatMapMany { message ->
                Flux.fromIterable(discordClients)
                    .flatMap { it.getMessageChannelById(Snowflake.of(channelDocument.id)) }
                    .flatMap { it.createMessage(message) }
            }.then()
    }

    private fun getRawString(channelDocument: ChannelDocument, request: EventRequest) = when (request.type) {
        "ON_PLUGIN_START" -> getOnPluginStart(channelDocument)
        "ON_PLUGIN_END" -> getOnPluginEnd(channelDocument)
        "ON_MAP_START" -> getOnMapStart(channelDocument)
        "ON_MAP_END" -> getOnMapEnd(channelDocument)
        "ON_CLIENT_CONNECTED" -> getOnClientConnected(channelDocument)
        "ON_CLIENT_DISCONNECT" -> getOnClientDisconnect(channelDocument)
        "PLAYER_SAY" -> getPlayerSay(channelDocument)
        else -> null
    }

    private fun getOnPluginStart(channelDocument: ChannelDocument) = when (channelDocument.gameType) {
        TEAM_FORTRESS_2 -> channelDocument.teamFortress2.genericOnPluginStart
        else -> null
    }

    private fun getOnPluginEnd(channelDocument: ChannelDocument) = when (channelDocument.gameType) {
        TEAM_FORTRESS_2 -> channelDocument.teamFortress2.genericOnPluginEnd
        else -> null
    }

    private fun getOnMapStart(channelDocument: ChannelDocument) = when (channelDocument.gameType) {
        TEAM_FORTRESS_2 -> channelDocument.teamFortress2.genericOnMapStart
        else -> null
    }

    private fun getOnMapEnd(channelDocument: ChannelDocument) = when (channelDocument.gameType) {
        TEAM_FORTRESS_2 -> channelDocument.teamFortress2.genericOnMapEnd
        else -> null
    }

    private fun getOnClientConnected(channelDocument: ChannelDocument) = when (channelDocument.gameType) {
        TEAM_FORTRESS_2 -> channelDocument.teamFortress2.genericOnClientConnected
        else -> null
    }

    private fun getOnClientDisconnect(channelDocument: ChannelDocument) = when (channelDocument.gameType) {
        TEAM_FORTRESS_2 -> channelDocument.teamFortress2.genericOnClientDisconnect
        else -> null
    }

    private fun getPlayerSay(channelDocument: ChannelDocument) = when (channelDocument.gameType) {
        TEAM_FORTRESS_2 -> channelDocument.teamFortress2.genericPlayerSay
        else -> null
    }
}
