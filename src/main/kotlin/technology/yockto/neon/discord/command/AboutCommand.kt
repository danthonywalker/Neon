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
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.MessageCreateSpec
import discord4j.core.util.VersionUtil
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import technology.yockto.neon.util.createEmbed
import technology.yockto.neon.util.round
import java.awt.Color
import java.lang.management.ManagementFactory
import java.time.Duration
import java.time.Instant

@Component
@Suppress("KDocMissingDocumentation")
class AboutCommand : NeonCommand {

    override val aliases: Set<String> = setOf("about")
    private val startTime = Instant.now()

    override fun execute(event: MessageCreateEvent): Mono<Void> {
        return event.message.channel // Sends a reply to same exact channel
            .filter { getArguments(event.message.content.get()).isEmpty() }
            .flatMap { channel ->
                event.createEmbed {

                    val version = VersionUtil.getProperties().getProperty(VersionUtil.APPLICATION_VERSION)
                    val os = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
                    val uptime = Duration.between(startTime, Instant.now())
                    val runtime = Runtime.getRuntime()

                    val days = "${uptime.toDays()}d"
                    val hours = "${uptime.toHoursPart()}h"
                    val minutes = "${uptime.toMinutesPart()}m"
                    val seconds = "${uptime.toSecondsPart()}s"
                    val millis = "${uptime.toMillisPart()}ms"

                    val processors = "Processors: ${os.availableProcessors}"
                    val systemCpu = "System: ${(os.systemCpuLoad * 100).round(2)}%"
                    val jvmCpu = "JVM: ${(os.processCpuLoad * 100).round(2)}%"

                    val totalSystem = (os.totalPhysicalMemorySize / 1_000_000)
                    val usedSystem = (totalSystem - (os.freePhysicalMemorySize / 1_000_000))
                    val totalJvm = (runtime.totalMemory() / 1_000_000)
                    val usedJvm = (totalJvm - (runtime.freeMemory() / 1_000_000))

                    val percentJvmSystem = "${((usedJvm.toDouble() / totalSystem.toDouble()) * 100).round(2)}%"
                    val percentSystem = "${((usedSystem.toDouble() / totalSystem.toDouble()) * 100).round(2)}%"
                    val percentJvm = "${((usedJvm.toDouble() / totalJvm.toDouble()) * 100).round(2)}%"

                    val neonRam = "Neon: ${usedJvm}MB / ${totalSystem}MB ($percentJvmSystem)"
                    val systemRam = "System: ${usedSystem}MB / ${totalSystem}MB ($percentSystem)"
                    val jvmRam = "JVM: ${usedJvm}MB / ${totalJvm}MB ($percentJvm)"

                    it.addField("Author", "danthonywalker#5512", true)
                    it.addField("Powered By", "Discord4J ($version)", true)
                    it.addField("Operating System", "${os.name}-${os.arch}-${os.version}", true)
                    it.addField("Uptime", "$days $hours $minutes $seconds $millis", true)
                    it.addField("CPU Usage", "$processors\n$systemCpu\n$jvmCpu", true)
                    it.addField("RAM Usage", "$neonRam\n$systemRam\n$jvmRam", true)

                    it.addField("Privacy Policy", "By inviting Neon to your Guild, or by interacting with " +
                        "Features provided by Neon, then you agree to the collection and use of Information " +
                        "in relation with this policy. The Personal Information that Neon collects is used for " +
                        "providing and improving the Service. Neon will not use or share your Information with " +
                        "anyone unless required by Law.\nNeon will never collect message texts, but may collect " +
                        "statistical Information such as message count and/or guild count. Users may request to " +
                        "view and/or delete their Information at any time (use **/help** for details).", false)

                    it.setDescription("A Discord Bot Gaming Bridge for the Neon Midori Network")
                    it.setColor(Color.CYAN)
                    it.setTitle("Summary")

                }.flatMap { channel.createMessage(MessageCreateSpec().setEmbed(it)) }
            }.then()
    }
}
