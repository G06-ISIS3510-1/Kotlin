package com.wheels.app.core.analytics.domain.repository

import com.wheels.app.core.analytics.domain.model.RoleChangeEvent

interface RoleChangeEventRepository {
    suspend fun trackRoleChange(event: RoleChangeEvent)
}
