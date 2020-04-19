package org.idovgal.vaultthreadmonitoring.pojo

open class SimpleThreadInfo(
        val id: Long,
        val name: String,
        val state: String,
        val stackTrace: List<String>
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SimpleThreadInfo

        if (id != other.id) return false
        if (name != other.name) return false
        if (state != other.state) return false
        if (stackTrace != other.stackTrace) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + stackTrace.hashCode()
        return result
    }
}