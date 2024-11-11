package com.julic20s.fleur.data

import com.alibaba.fastjson2.annotation.JSONField

data class DeviceState(
    @JSONField(name = "humidity")
    val humidity: Double,
    @JSONField(name = "temperature")
    val temperature: Double,
    @JSONField(name = "soil")
    val soil: Double,
    @JSONField(name = "power")
    val battery: Double,
) {
    val name: String = "UNKNOWN"
}