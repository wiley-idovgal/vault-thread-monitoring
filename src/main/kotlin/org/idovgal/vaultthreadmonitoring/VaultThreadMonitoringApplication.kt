package org.idovgal.vaultthreadmonitoring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(
        exclude = arrayOf(JmxAutoConfiguration::class)
)
@EnableScheduling
class VaultThreadMonitoringApplication

fun main(args: Array<String>) {
    runApplication<VaultThreadMonitoringApplication>(*args)
}
