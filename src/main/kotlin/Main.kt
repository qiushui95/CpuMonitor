import com.sun.management.OperatingSystemMXBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.io.File
import java.lang.management.ManagementFactory
import java.util.*

private val logDir = File("/var/log/cpu_monitor")

private val logName = now().toString("yyyy_MM-dd_HH_mm_ss_SSS")

private val logFile = File(logDir, "$logName.log")

private var idleTimes = 0

private fun now() = DateTime.now(DateTimeZone.forTimeZone(TimeZone.getTimeZone("Asia/Shanghai")))

fun main() = runBlocking<Unit>(Dispatchers.IO) {
    if (!logDir.exists()) logDir.mkdirs()

    if (logFile.exists()) logFile.delete()

    logFile.createNewFile()

    val systemMXBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean

    while (true) {

        val systemCpuLoad = (systemMXBean.systemCpuLoad * 100).toInt()

        val logBuilder = StringBuilder()

        logBuilder.append(now().toString("yyyy-MM-dd HH:mm:ss"))
            .append("-->")
            .append(systemCpuLoad)
            .append("%")
            .append("\n")

        logFile.appendText(logBuilder.toString())

        if (systemCpuLoad <= 10) {
            idleTimes++
        } else {
            idleTimes = 0
        }

        if (idleTimes > 60 * 2) {
            shutdown()
            break
        }

        delay(1000)
    }
}

private fun shutdown() {
    val logBuilder = StringBuilder()

    logBuilder.append(now().toString("yyyy-MM-dd HH:mm:ss"))
        .append("-->")
        .append("shutdown")
        .append("\n")

    logFile.appendText(logBuilder.toString())

    Runtime.getRuntime().exec("sudo shutdown").waitFor()
}