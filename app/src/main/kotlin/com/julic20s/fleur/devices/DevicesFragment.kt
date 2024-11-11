package com.julic20s.fleur.devices

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.julic20s.fleur.R
import com.julic20s.fleur.base.BindingFragment
import com.julic20s.fleur.bluetooth.BluetoothConnectionState
import com.julic20s.fleur.bluetooth.BluetoothHelper
import com.julic20s.fleur.databinding.DevicesListFragmentBinding
import com.julic20s.fleur.databinding.DevicesListItemBinding

class DevicesFragment : BindingFragment<DevicesListFragmentBinding>() {

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

    private val _adapter = ListAdapter()
    private val _bluetoothHelper by lazy { BluetoothHelper(requireContext()) }
    private val _viewModel by viewModels<DevicesViewModel>()

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
            _adapter.devices = _bluetoothHelper.bondedDevices.toList()
            _adapter.notifyDataSetChanged()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.move)
    }

    private inline fun doIfHasPermissions(action: () -> Unit) {
        val allGranted = bluetoothPermissions.all {
            val state = ContextCompat.checkSelfPermission(requireContext(), it)
            state == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) action()
        else _bluetoothPermissionLauncher.launch(bluetoothPermissions)
    }

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = DevicesListFragmentBinding.inflate(inflater, container, false)

    @SuppressLint("MissingPermission")
    override fun onViewBinding(binding: DevicesListFragmentBinding, savedInstanceState: Bundle?) {
        binding.list.adapter = _adapter
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.bondTip.setOnClickListener {
            startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
        }
        _viewModel.connectionState.observe(viewLifecycleOwner) {
            if (it is BluetoothConnectionState.Connected) {
                Toast.makeText(requireContext(), R.string.connect_successful, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onStart() {
        super.onStart()
        doIfHasPermissions {
            _adapter.devices = _bluetoothHelper.bondedDevices.toList()
            _adapter.notifyDataSetChanged()
        }
    }

    inner class ListViewHolder(private val binding: DevicesListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("MissingPermission")
        fun bind(device: BluetoothDevice) {
            binding.name.text = device.name
            binding.mac.text = device.address
            binding.root.setOnClickListener {
                if (_viewModel.connectionState.value != BluetoothConnectionState.Connecting) {
                    _bluetoothHelper.connect(device)
                    findNavController().popBackStack()
                }
            }
        }
    }

    private inner class ListAdapter : RecyclerView.Adapter<ListViewHolder>() {

        var devices: List<BluetoothDevice> = emptyList()

        override fun getItemCount() = devices.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = DevicesListItemBinding
            .inflate(layoutInflater, parent, false)
            .let(::ListViewHolder)

        override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
            holder.bind(devices[position])
        }

    }

}