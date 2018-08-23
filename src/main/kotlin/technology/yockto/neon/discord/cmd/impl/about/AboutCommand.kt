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
package technology.yockto.neon.discord.cmd.impl.about

import com.sun.management.OperatingSystemMXBean
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.util.VersionUtil
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import technology.yockto.neon.discord.cmd.NeonCommand
import technology.yockto.neon.util.createMessage
import java.awt.Color
import java.lang.management.ManagementFactory
import java.time.Duration
import kotlin.math.roundToInt

@Component
@Suppress("KDocMissingDocumentation")
class AboutCommand : NeonCommand {

    override val helpDescription: String = "Displays basic information about Neon. For more, use `help about [alias]`!"
    override val helpTitle: String = "About Command"

    override val names: Set<String> = setOf("a", "about")
    override val parent: NeonCommand? = null

    override fun execute(event: MessageCreateEvent, context: List<String>?): Mono<Void> {
        return event.message.channel.flatMap { channel ->
            channel.createMessage(event) {

                val version = VersionUtil.getProperties().getProperty(VersionUtil.APPLICATION_VERSION)
                val osBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
                val runtimeBean = ManagementFactory.getRuntimeMXBean()
                val runtime = Runtime.getRuntime()

                val uptime = Duration.ofMillis(runtimeBean.uptime)
                val days = "${uptime.toDays()}d"
                val hours = "${uptime.toHoursPart()}h"
                val minutes = "${uptime.toMinutesPart()}m"
                val seconds = "${uptime.toSecondsPart()}s"
                val millis = "${uptime.toMillisPart()}ms"

                val processors = "Processors: ${runtime.availableProcessors()}"
                val systemCpu = "System: ${(osBean.systemCpuLoad * 100).roundToInt()}%"
                val jvmCpu = "JVM: ${(osBean.processCpuLoad * 100).roundToInt()}%"

                val totalSystem = (osBean.totalPhysicalMemorySize.toDouble() / 1_000_000)
                val usedSystem = (totalSystem - (osBean.freePhysicalMemorySize.toDouble() / 1_000_000))
                val totalJvm = (runtime.totalMemory().toDouble() / 1_000_000)
                val usedJvm = (totalJvm - (runtime.freeMemory().toDouble() / 1_000_000))

                val percentJvmSystem = "${((usedJvm / totalSystem) * 100).roundToInt()}%"
                val percentSystem = "${((usedSystem / totalSystem) * 100).roundToInt()}%"
                val percentJvm = "${((usedJvm / totalJvm) * 100).roundToInt()}%"

                val neon = "Neon: ${usedJvm.roundToInt()}MB / ${totalSystem.roundToInt()}MB ($percentJvmSystem)"
                val system = "System: ${usedSystem.roundToInt()}MB / ${totalSystem.roundToInt()}MB ($percentSystem)"
                val jvm = "JVM: ${usedJvm.roundToInt()}MB / ${totalJvm.roundToInt()}MB ($percentJvm)"

                it.addField("Author", "danthonywalker#5512", true)
                it.addField("Powered By", "Java (${Runtime.version()})", true)
                it.addField("Library", "Discord4J ($version)", true)
                it.addField("Uptime", "$days $hours $minutes $seconds $millis", true)
                it.addField("CPU Usage", "$processors\n$systemCpu\n$jvmCpu", true)
                it.addField("RAM Usage", "$neon\n$system\n$jvm", true)

                it.setDescription("A Discord Bot Gaming Bridge for the Neon Midori Network")
                it.setColor(Color.CYAN)
                it.setTitle("Summary")
            }
        }.then()
    }
}
