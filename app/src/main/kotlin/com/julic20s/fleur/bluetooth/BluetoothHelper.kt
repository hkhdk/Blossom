package com.julic20s.fleur.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat.getSystemService
import com.julic20s.fleur.data.DeviceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicBoolean

class BluetoothHelper(context: Context) {

    private val _bluetoothMgr = getSystemService(context, BluetoothManager::class.java)!!
    private val _bluetoothAdapter = _bluetoothMgr.adapter
    private val _service = BluetoothService(_bluetoothAdapter, BluetoothStateBroadcastReceiver.connectionStateFlow)

    init {
        BluetoothStateBroadcastReceiver.connectionStateFlow.value =
            if (_bluetoothAdapter.isEnabled) BluetoothConnectionState.Enabled
            else BluetoothConnectionState.Disabled
        BluetoothStateBroadcastReceiver.registerIfNeed(context)
    }

    private object BluetoothStateBroadcastReceiver : BroadcastReceiver() {

        val registered: AtomicBoolean = AtomicBoolean(false)

        fun registerIfNeed(context: Context) {
            if (registered.compareAndSet(false, true)) {
                val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
                context.registerReceiver(this, filter)
            }
        }

        val connectionStateFlow = MutableStateFlow<BluetoothConnectionState>(
            BluetoothConnectionState.Disabled
        )

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != BluetoothAdapter.ACTION_STATE_CHANGED) return
            when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)) {
                BluetoothAdapter.STATE_OFF -> connectionStateFlow.value = BluetoothConnectionState.Disabled
                BluetoothAdapter.STATE_ON -> connectionStateFlow.value = BluetoothConnectionState.Enabled
            }
        }
    }

    val connectionStateFlow: StateFlow<BluetoothConnectionState>
        get() = BluetoothStateBroadcastReceiver.connectionStateFlow

    // 获取蓝牙状态
    val isBluetoothEnabled get() = _bluetoothAdapter.isEnabled

    @get:RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    val bondedDevices get() = _bluetoothAdapter.bondedDevices

    fun connect(device: BluetoothDevice) {
        _service.connect(device)
    }

    fun fake() {
        BluetoothStateBroadcastReceiver.connectionStateFlow.value = BluetoothConnectionState.Connected(
            DeviceState(23.0, 23.0, 23.0, 52.0)
        )
    }

}