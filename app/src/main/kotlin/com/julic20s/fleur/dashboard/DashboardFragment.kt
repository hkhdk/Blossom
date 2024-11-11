package com.julic20s.fleur.dashboard

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.julic20s.fleur.R
import com.julic20s.fleur.base.BindingFragment
import com.julic20s.fleur.bluetooth.BluetoothConnectionState
import com.julic20s.fleur.bluetooth.BluetoothHelper
import com.julic20s.fleur.databinding.DashboardFragmentBinding

class DashboardFragment : BindingFragment<DashboardFragmentBinding>() {

    private companion object {
        @JvmStatic
        val bluetoothPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                "android.permission.BLUETOOTH_SCAN",
                "android.permission.BLUETOOTH_CONNECT",
            )
        } else {
            arrayOf(
                "android.permission.BLUETOOTH",
                "android.permission.BLUETOOTH_ADMIN",
            )
        }
    }

    private val _viewModel: DashboardViewModel by viewModels()

    private val _bluetoothHelper by lazy { BluetoothHelper(requireContext()) }

    private val _bluetoothEnableLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    @SuppressLint("MissingPermission", "NotifyDataSetChanged")
    private val _bluetoothPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            for (successful in it.values) {
                if (!successful) {
                    Toast.makeText(context, R.string.bluetooth_permission_failed, Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                    return@registerForActivityResult
                }
            }
        }

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = DashboardFragmentBinding.inflate(inflater, container, false)

    private inline fun doIfHasPermissions(action: () -> Unit) {
        if (_viewModel.connectionState.value is BluetoothConnectionState.Connecting) return
        val allGranted = bluetoothPermissions.all {
            val state = ContextCompat.checkSelfPermission(requireContext(), it)
            state == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) action()
        else _bluetoothPermissionLauncher.launch(bluetoothPermissions)
    }


    @SuppressLint("UseCompatTextViewDrawableApis")
    override fun onViewBinding(binding: DashboardFragmentBinding, savedInstanceState: Bundle?) {
        with(binding) {
            search.setOnClickListener {
                val extra = FragmentNavigatorExtras(search to search.transitionName)
                findNavController().navigate(R.id.action_dashboardFragment_to_wikiFragment, null, null, extra)
            }
            bluetoothState.setOnClickListener {
                if (_bluetoothHelper.isBluetoothEnabled) {
                    findNavController().navigate(
                        R.id.action_dashboardFragment_to_devicesFragment,
                        null, null,
                        FragmentNavigatorExtras(bluetoothState to bluetoothState.transitionName)
                    )
                } else {
                    doIfHasPermissions {
                        _bluetoothEnableLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    }
                }
            }
            _viewModel.connectionState.observe(viewLifecycleOwner) {
                when (it) {
                    is BluetoothConnectionState.Disabled -> {
                        with(bluetoothState) {
                            setText(R.string.bluetooth_disabled)
                            compoundDrawableTintList = ColorStateList.valueOf(context.getColor(R.color.disabled))
                        }
                        stateCards.visibility = View.INVISIBLE
                        connectionTip.visibility = View.VISIBLE
                    }
                    is BluetoothConnectionState.Enabled -> {
                        with(bluetoothState) {
                            setText(R.string.device_disconnected)
                            compoundDrawableTintList = ColorStateList.valueOf(context.getColor(R.color.cation))
                        }
                        stateCards.visibility = View.INVISIBLE
                        connectionTip.visibility = View.VISIBLE
                    }
                    is BluetoothConnectionState.Connected -> {
                        with(bluetoothState) {
                            compoundDrawableTintList = ColorStateList.valueOf(context.getColor(R.color.pass))
                            text = it.state.name
                        }
                        stateCards.visibility = View.VISIBLE
                        connectionTip.visibility = View.INVISIBLE
                        tempText.text = String.format("%.0f", it.state.temperature)// "27"
                        humidityText.text = String.format("%.0f", it.state.humidity)//"63"
                        soilText.text = String.format("%.0f", it.state.soil)
                        val hack = 75.0// min(100.0, it.state.battery * 19)
                        batteryText.text = String.format("%.0f%%", hack)
                        battery.progress = (hack  / 100).toFloat()
                    }
                    is BluetoothConnectionState.Connecting -> {
                        with(bluetoothState) {
                            setText(R.string.device_connecting)
                            compoundDrawableTintList = ColorStateList.valueOf(context.getColor(R.color.night))
                        }
                        stateCards.visibility = View.INVISIBLE
                        connectionTip.visibility = View.VISIBLE
                    }
                }
            }

            battery.clipToOutline = true
            soil.clipToOutline = true

            battery.post {
                with(battery) {
                    val anim = ValueAnimator.ofFloat(1f, 0f)

                    val phase = width
                    val length = width / 2f
                    // 总之非常暴力，下次一定写 xml 里
                    val minStrength = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, context.resources.displayMetrics)
                    val maxStrength = minStrength / 6 * 16
                    anim.addUpdateListener {
                        val v = it.animatedValue as Float
                        if (v > .5f) {
                            val vv = (v - .5f) * 2
                            reshape(
                                phase = (phase * v).toInt(),
                                strength = minStrength * vv + maxStrength * (1 - vv),
                                length = length,
                            )
                        } else {
                            val vv = v * 2
                            reshape(
                                phase = (phase * v).toInt(),
                                strength = maxStrength * vv + minStrength * (1 - vv),
                                length = length,
                            )
                        }
                    }
                    anim.duration = 5000
                    anim.interpolator = LinearInterpolator()
                    anim.repeatCount = ValueAnimator.INFINITE
                    anim.start()
                }
            }

            val t = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if (t < 7 || 18 <= t) {
                timeIcon.setImageResource(R.drawable.night)
            } else {
                timeIcon.setImageResource(R.drawable.sun)
            }
        }
    }

}