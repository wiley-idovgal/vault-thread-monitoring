package org.idovgal.vaultthreadmonitoring.pojo

import java.time.LocalDateTime

data class Alert(
        val id: Long,
        val date: LocalDateTime,
        val threadId: Long?,
        val type: String
)