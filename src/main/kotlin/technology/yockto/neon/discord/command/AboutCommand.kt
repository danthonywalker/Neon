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
package technology.yockto.neon.discord.command

import com.sun.management.OperatingSystemMXBean
import discord4j.core.`object`.util.Image.Format.PNG
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.util.VersionUtil
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.awt.Color
import java.lang.management.ManagementFactory
import java.time.Duration
import java.time.Instant

@Component
@Suppress("KDocMissingDocumentation")
class AboutCommand : NeonCommand {

    override fun execute(event: MessageCreateEvent): Mono<Void> {
        return Mono.zip(event.message.channel, event.client.self)
            .flatMap { tuple ->

                tuple.t1.createMessage { messageCreateSpec ->
                    messageCreateSpec.setEmbed {

                        val os = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
                        val avatarUrl = tuple.t2.getAvatarUrl(PNG).orElse(tuple.t2.defaultAvatarUrl)
                        val authorName = "${tuple.t2.username}#${tuple.t2.discriminator}"
                        val runtime = Runtime.getRuntime()

                        val uptime = Duration.ofNanos(os.processCpuTime)
                        val days = "${uptime.toDays()}d"
                        val hours = "${uptime.toHoursPart()}h"
                        val minutes = "${uptime.toMinutesPart()}m"
                        val seconds = "${uptime.toSecondsPart()}s"
                        val millis = "${uptime.toMillisPart()}ms"
                        val nanos = "${uptime.toNanosPart()}ns"

                        val processors = "Processors: ${os.availableProcessors}"
                        val jvmCpuUsage = "JVM: ${os.processCpuLoad * 100}%"
                        val systemCpuUsage = "System: ${os.systemCpuLoad * 100}%"

                        val totalSystem = os.totalPhysicalMemorySize
                        val freeSystem = os.freePhysicalMemorySize
                        val totalJvm = runtime.totalMemory()
                        val freeJvm = runtime.freeMemory()

                        val usedSystem = ((totalSystem - freeSystem) / totalSystem) * 100
                        val usedJvm = ((totalJvm - freeJvm) / totalJvm) * 100

                        val systemRam = "System: ${totalSystem * 1_000_000}MB ($usedSystem% Used)"
                        val jvmRam = "JVM: ${totalJvm * 1_000_000}MB ($usedJvm% Used)"

                        it.addField("Author", "danthonywalker#5512", true)
                        it.addField("Powered By", "Discord4J (${VersionUtil.GIT_COMMIT_ID})", true)
                        it.addField("Operating System", "${os.name}-${os.arch}-${os.version}", true)
                        it.addField("Uptime", "$days $hours $minutes $seconds $millis $nanos", true)
                        it.addField("CPU Usage", "$processors\n$systemCpuUsage\n$jvmCpuUsage", true)
                        it.addField("RAM Usage", "$systemRam\n$jvmRam", true)

                        it.addField("Privacy Policy", "By inviting Neon to your Guild, or by interacting with " +
                            "Features provided by Neon, then you agree to the collection and use of Information " +
                            "in relation with this policy. The Personal Information that Neon collects is used for " +
                            "providing and improving the Service. Neon will not use or share your Information with " +
                            "anyone unless required by Law.\nNeon will never collect message texts, but may collect " +
                            "statistical Information such as message count and/or guild count. Users may request to " +
                            "view and/or delete their Information at any time (see Help command for details).", false)

                        it.setDescription("A Discord Bot Gaming Bridge for the Neon Midori Network")
                        it.setAuthor(authorName, "https://neon.yockto.technology/", avatarUrl)
                        it.setTimestamp(Instant.now())
                        it.setThumbnail(avatarUrl)
                        it.setColor(Color.CYAN)
                        it.setTitle("Summary")
                    }
                }
            }.then()
    }

    override val aliases: Set<String> = setOf("about")
}
