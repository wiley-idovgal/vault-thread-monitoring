package org.idovgal.vaultthreadmonitoring.service

import org.idovgal.vaultthreadmonitoring.exception.DisconnectedException
import org.idovgal.vaultthreadmonitoring.pojo.AdvancedThreadInfo
import org.idovgal.vaultthreadmonitoring.pojo.Alert
import org.idovgal.vaultthreadmonitoring.pojo.SimpleThreadInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import javax.annotation.PostConstruct


@Component
class ThreadInfoMonitor(val threadInfoService: ThreadInfoService,
                        val messagingTemplate: SimpMessagingTemplate,
                        @Value("\${application.stuck-warning}") val stuckWarning: Duration
) {

    @Volatile
    private lateinit var lastThreadInfo: Map<Long, SimpleThreadInfo>

    private val threadUpdateTime: MutableMap<Long, LocalDateTime> = ConcurrentHashMap();
    private val threadAlertState: MutableMap<Long, Boolean> = ConcurrentHashMap();
    private val counter: AtomicLong = AtomicLong()
    private val connected: AtomicBoolean = AtomicBoolean(false)

    @PostConstruct
    fun init() {
        this.lastThreadInfo = threadInfoService.getThreadInfo()
                .map { it.id to it }
                .toMap()
        lastThreadInfo.forEach {
            threadUpdateTime[it.key] = LocalDateTime.now()
            threadAlertState[it.key] = false
        }
    }

    private fun gotStuck(id: Long) {
        this.messagingTemplate.convertAndSend("/topic/alert", Alert(counter.incrementAndGet(),
                LocalDateTime.now(), id, "STUCK"));
    }

    private fun resumed(id: Long) {
        this.messagingTemplate.convertAndSend("/topic/alert", Alert(counter.incrementAndGet(),
                LocalDateTime.now(), id, "RESUMED"));
    }

    private fun sendCurrentState() {
        this.messagingTemplate.convertAndSend("/topic/current-state", getMonitorInfo());
    }

    private fun setConnected(state: Boolean) {
        val oldState = this.connected.getAndSet(state)
        if (oldState && !state)
            this.messagingTemplate.convertAndSend("/topic/alert", Alert(
                    counter.incrementAndGet(),
                    LocalDateTime.now(),
                    null,
                    "DISCONNECTED"));
        if (!oldState && state)
            this.messagingTemplate.convertAndSend("/topic/alert", Alert(
                    counter.incrementAndGet(),
                    LocalDateTime.now(),
                    null,
                    "CONNECTED"
            ));
    }

    @Scheduled(fixedRateString = "\${application.refresh-duration}")
    fun tick() {
        try {
            val threadInfo = threadInfoService.getThreadInfo()
            (threadUpdateTime.keys - threadInfo.map { it.id }.toSet()).forEach {
                threadUpdateTime.remove(it)
                threadAlertState.remove(it)
            }
            threadInfo.forEach {
                if (it.id !in threadUpdateTime) {
                    threadUpdateTime[it.id] = LocalDateTime.now()
                    threadAlertState[it.id] = false
                } else
                    if (lastThreadInfo[it.id]!! != it) {
                        threadUpdateTime[it.id] = LocalDateTime.now()
                        if (threadAlertState[it.id]!!)
                            resumed(it.id)
                        threadAlertState[it.id] = false
                    }
            }
            this.lastThreadInfo = threadInfo.map { it.id to it }
                    .toMap()

            threadUpdateTime.filter {
                var duration = Duration.between(it.value, LocalDateTime.now())
                return@filter duration > stuckWarning && !threadAlertState[it.key]!!
            }
                    .map { it.key }
                    .forEach {
                        threadAlertState[it] = true
                        gotStuck(it)
                    }
            setConnected(true)
            sendCurrentState()
        } catch (e: DisconnectedException) {
            setConnected(false)
        }
    }

    fun getMonitorInfo(): List<AdvancedThreadInfo> {
        return lastThreadInfo.map {
            AdvancedThreadInfo(it.value, threadUpdateTime[it.key]!!)
        }
    }

}