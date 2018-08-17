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
package technology.yockto.neon.db

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@EnableReactiveMongoRepositories
@Suppress("KDocMissingDocumentation")
class DatabaseConfiguration @Autowired constructor(
    @Value("\${NEON_MONGODB_USERNAME}") private val username: String,
    @Value("\${NEON_MONGODB_PASSWORD}") private val password: String
) : AbstractReactiveMongoConfiguration() {

    override fun reactiveMongoClient(): MongoClient = MongoClients.create("mongodb://$username:$password@mongo")
    override fun getDatabaseName(): String = "neon"
}
