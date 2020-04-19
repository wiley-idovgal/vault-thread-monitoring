package org.idovgal.vaultthreadmonitoring.pojo

import java.time.LocalDateTime

class AdvancedThreadInfo(id: Long, name: String, state: String, stackTrace: List<String>, val updateTime: LocalDateTime)
    : SimpleThreadInfo(id, name, state, stackTrace) {

    constructor(simpleThreadInfor: SimpleThreadInfo, updateTime: LocalDateTime) : this(
            simpleThreadInfor.id, simpleThreadInfor.name, simpleThreadInfor.state, simpleThreadInfor.stackTrace, updateTime
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as AdvancedThreadInfo

        if (updateTime != other.updateTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + updateTime.hashCode()
        return result
    }


}