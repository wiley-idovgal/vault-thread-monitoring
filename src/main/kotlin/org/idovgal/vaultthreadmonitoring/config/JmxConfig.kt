package org.idovgal.vaultthreadmonitoring.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.management.MBeanServerConnection
import javax.management.remote.JMXConnector
import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXServiceURL
import java.lang.management.ManagementFactory.THREAD_MXBEAN_NAME
import java.lang.management.ManagementFactory.newPlatformMXBeanProxy
import java.lang.management.ThreadMXBean


@Configuration
class JmxConfig {

    @Bean
    fun jmxUrl(@Value("\${application.vault.jmx}") vaultJmx: String): JMXServiceURL {
        val jmxServiceURL = JMXServiceURL(vaultJmx)
        return jmxServiceURL;
    }

    @Bean(destroyMethod = "close")
    fun jmxConector(url: JMXServiceURL): JMXConnector {
        return JMXConnectorFactory.connect(url)
    }

    @Bean
    fun mbeanServerConnection(jmxConnector: JMXConnector): MBeanServerConnection {
        return jmxConnector.getMBeanServerConnection()
    }

    @Bean
    fun vaultThreadMxBean(connection: MBeanServerConnection): ThreadMXBean {
        return newPlatformMXBeanProxy(connection, THREAD_MXBEAN_NAME, ThreadMXBean::class.java)
    }


}