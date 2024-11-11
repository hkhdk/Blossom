package com.julic20s.fleur.bluetooth

import com.julic20s.fleur.data.DeviceState
import kotlinx.coroutines.flow.StateFlow

sealed class BluetoothConnectionState {

    object Disabled : BluetoothConnectionState()

    object Enabled : BluetoothConnectionState()

    object Connecting : BluetoothConnectionState()

    class Connected(val state: DeviceState) : BluetoothConnectionState()

}