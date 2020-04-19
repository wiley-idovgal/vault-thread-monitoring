package org.idovgal.vaultthreadmonitoring.service

import org.idovgal.vaultthreadmonitoring.exception.DisconnectedException
import org.idovgal.vaultthreadmonitoring.pojo.SimpleThreadInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.lang.management.ThreadMXBean
import java.lang.reflect.UndeclaredThrowableException
import java.rmi.ConnectIOException

@Component
class ThreadInfoService(val threadMXBean: ThreadMXBean,
                        @Value("\${application.monitoring-threads}") val monitoringThreads: String) {



    fun getThreadInfo(): List<SimpleThreadInfo> {
        try {
            var matchedThreads = threadMXBean.getThreadInfo(threadMXBean.allThreadIds)
                    .filterNotNull()
                    .filter { it.threadName.matches(Regex(monitoringThreads)) }
                    .map { it.threadId }
                    .toLongArray()

            return threadMXBean.getThreadInfo(matchedThreads, Int.MAX_VALUE)
                    .map {
                        SimpleThreadInfo(
                                it.threadId,
                                it.threadName,
                                it.threadState.name,
                                it.stackTrace.map { "${it.className}#${it.methodName} at ${it.lineNumber}" }
                        )
                    }
                    .toList()
        } catch (e: UndeclaredThrowableException) {
            if(e.undeclaredThrowable is ConnectIOException)
                throw DisconnectedException();
            throw e;
        }
    }


}