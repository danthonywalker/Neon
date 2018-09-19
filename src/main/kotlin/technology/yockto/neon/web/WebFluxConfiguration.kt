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
package technology.yockto.neon.web

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.reactive.config.EnableWebFlux
import reactor.core.publisher.Mono
import technology.yockto.neon.db.document.ChannelDocument
import technology.yockto.neon.db.repository.ChannelRepository
import java.security.Principal
import java.util.UUID

@EnableWebFlux
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Suppress("KDocMissingDocumentation")
class WebFluxConfiguration @Autowired constructor(
    private val channelRepository: ChannelRepository
) : ReactiveUserDetailsService {

    override fun findByUsername(username: String): Mono<UserDetails> {
        return Mono.justOrEmpty(username.toBigIntegerOrNull())
            .flatMap(channelRepository::findById)
            .map(ChannelDocument::password)
            .map(UUID::toString)
            // TODO User must have a role so possibly explore uses for custom roles
            .map { User.withUsername(username).password(it).roles("USER").build() }
    }

    @Bean
    fun getSecurityWebFilerChain(serverHttpSecurity: ServerHttpSecurity): SecurityWebFilterChain {
        // Enables ability to authenticate and authorize incoming REST requests
        return serverHttpSecurity.authorizeExchange()

            .pathMatchers("/api/v1/channels/{id}/**").access { authentication, `object` ->
                authentication.map(Principal::getName) // Checks if the user ID is in path
                    .map { name -> (name == `object`.variables["id"]) }
                    .map(::AuthorizationDecision)
            }

            // Deny all other requests, disable browser login, and enable Authorization Basic through HTTP
            .anyExchange().denyAll().and().formLogin().disable().csrf().disable().httpBasic().and().build()
    }

    @Bean
    @Suppress("DEPRECATION")
    fun getPasswordEncoder(): PasswordEncoder = NoOpPasswordEncoder.getInstance()
}
