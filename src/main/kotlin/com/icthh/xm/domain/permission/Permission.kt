package com.icthh.xm.domain.permission

import com.fasterxml.jackson.annotation.JsonIgnore
import com.icthh.xm.domain.permission.ReactionStrategy.EXCEPTION

data class Permission(
    @field:JsonIgnore
    var msName: String = "",
    @field:JsonIgnore
    var roleKey: String = "",
    var privilegeKey: String = ""
): Comparable<Permission>  {

    var disabled: Boolean? = true
    var deleted: Boolean? = false
    var envCondition: String? = null
    var resourceCondition: String? = null
    var reactionStrategy: ReactionStrategy? = EXCEPTION

    override fun compareTo(other: Permission) = privilegeKey.compareTo(other.privilegeKey)
}
