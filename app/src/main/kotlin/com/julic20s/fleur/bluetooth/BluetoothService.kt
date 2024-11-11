package com.julic20s.fleur.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.alibaba.fastjson2.JSON
import com.julic20s.fleur.data.DeviceState
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
class BluetoothService(
    private val _adapter: BluetoothAdapter,
    private val _flow: MutableStateFlow<BluetoothConnectionState>
) {
    private var _secureAcceptThread: AcceptThread? = null
    private var _insecureAcceptThread: AcceptThread? = null
    private var _connectThread: ConnectThread? = null
    private var _connectedThread: ConnectedThread? = null

    /**
     * Return the current connection state.
     */
    @get:Synchronized
    var state: Int
        private set

    init {
        state = STATE_NONE
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    @Synchronized
    fun start() {
        Log.d(TAG, "start")

        // Cancel any thread attempting to make a connection
        if (_connectThread != null) {
            _connectThread!!.cancel()
            _connectThread = null
        }

        // Cancel any thread currently running a connection
        if (_connectedThread != null) {
            _connectedThread!!.cancel()
            _connectedThread = null
        }

        // Start the thread to listen on a BluetoothServerSocket
        if (_insecureAcceptThread == null) {
            _insecureAcceptThread = AcceptThread()
            _insecureAcceptThread!!.start()
        }
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    @Synchronized
    fun connect(device: BluetoothDevice) {
        Log.d(TAG, "connect to: $device")

        // Cancel any thread attempting to make a connection
        if (state == STATE_CONNECTING) {
            if (_connectThread != null) {
                _connectThread!!.cancel()
                _connectThread = null
            }
        }

        // Cancel any thread currently running a connection
        if (_connectedThread != null) {
            _connectedThread!!.cancel()
            _connectedThread = null
        }

        // Start the thread to connect with the given device
        _connectThread = ConnectThread(device)
        _connectThread!!.start()
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     */
    @SuppressLint("MissingPermission")
    @Synchronized
    private fun connected(socket: BluetoothSocket?) {
        Log.d(TAG, "connected")

        // Cancel the thread that completed the connection
        if (_connectThread != null) {
            _connectThread!!.cancel()
            _connectThread = null
        }

        // Cancel any thread currently running a connection
        if (_connectedThread != null) {
            _connectedThread!!.cancel()
            _connectedThread = null
        }

        // Cancel the accept thread because we only want to connect to one device
        if (_secureAcceptThread != null) {
            _secureAcceptThread!!.cancel()
            _secureAcceptThread = null
        }
        if (_insecureAcceptThread != null) {
            _insecureAcceptThread!!.cancel()
            _insecureAcceptThread = null
        }

        // Start the thread to manage the connection and perform transmissions
        _connectedThread = ConnectedThread(socket)
        _connectedThread!!.start()
    }

    /**
     * Stop all threads
     */
    @Synchronized
    fun stop() {
        Log.d(TAG, "stop")
        _connectThread?.cancel()
        _connectThread = null
        _connectedThread?.cancel()
        _connectedThread = null
        _secureAcceptThread?.cancel()
        _secureAcceptThread = null
        _insecureAcceptThread?.cancel()
        _insecureAcceptThread = null
        state = STATE_NONE
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread.write
     */
    fun write(out: ByteArray) {
        // Create temporary object
        var r: ConnectedThread? = null
        // Synchronize a copy of the ConnectedThread
        synchronized(this) {
            if (state != STATE_CONNECTED) return
            r = _connectedThread
        }
        // Perform the write unsynchronized
        r!!.write(out)
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private fun connectionFailed() {
        _flow.value = BluetoothConnectionState.Enabled
        state = STATE_NONE

        // Start the service over to restart listening mode
        start()
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private fun connectionLost() {
        _flow.value = BluetoothConnectionState.Enabled
        state = STATE_NONE

        // Start the service over to restart listening mode
        start()
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private inner class AcceptThread @SuppressLint("MissingPermission") constructor() : Thread() {
        // The local server socket
        private val _serverSocket: BluetoothServerSocket?

        init {
            var tmp: BluetoothServerSocket? = null

            // Create a new listening server socket
            try {
                tmp = _adapter.listenUsingInsecureRfcommWithServiceRecord(
                    NAME_INSECURE, UUID_SERIAL
                )
            } catch (e: IOException) {
                Log.e(TAG, "Socket listen() failed", e)
            }
            _serverSocket = tmp
            this@BluetoothService.state = STATE_LISTEN
        }

        override fun run() {
            Log.d(TAG, "Socket Type: BEGIN mAcceptThread$this")
            var socket: BluetoothSocket?

            // Listen to the server socket if we're not connected
            while (this@BluetoothService.state != STATE_CONNECTED) {
                socket = try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    _serverSocket!!.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Socket accept() failed", e)
                    break
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized(this@BluetoothService) {
                        when (this@BluetoothService.state) {
                            STATE_LISTEN, STATE_CONNECTING ->
                                connected(socket)
                            STATE_NONE, STATE_CONNECTED ->
                                try {
                                    socket.close()
                                } catch (e: IOException) {
                                    Log.e(TAG, "Could not close unwanted socket", e)
                                }
                        }
                        return@synchronized
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread")
        }

        fun cancel() {
            Log.d(TAG, "Socket cancel $this")
            try {
                _serverSocket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "Socket close() of server failed", e)
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private inner class ConnectThread @SuppressLint("MissingPermission") constructor(device: BluetoothDevice) :
        Thread() {
        private val _socket: BluetoothSocket?

        init {
            var tmp: BluetoothSocket? = null
            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(UUID_SERIAL)
            } catch (e: IOException) {
                Log.e(TAG, "Socket create() failed", e)
            }
            _socket = tmp
            this@BluetoothService.state = STATE_CONNECTING
        }

        @SuppressLint("MissingPermission")
        override fun run() {
            Log.i(TAG, "BEGIN mConnectThread")

            // Always cancel discovery because it will slow down a connection
            _adapter.cancelDiscovery()

            _flow.value = BluetoothConnectionState.Connecting

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                _socket!!.connect()
            } catch (e: IOException) {
                // Close the socket
                try {
                    _socket!!.close()
                } catch (e2: IOException) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2)
                }
                connectionFailed()
                return
            }

            // Reset the ConnectThread because we're done
            synchronized(this@BluetoothService) { _connectThread = null }

            // Start the connected thread
            connected(_socket)
        }

        fun cancel() {
            try {
                _socket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed", e)
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private inner class ConnectedThread(socket: BluetoothSocket?) : Thread() {
        private val socket: BluetoothSocket?
        private val _inStream: InputStream?
        private val _outStream: OutputStream?

        init {
            Log.d(TAG, "create ConnectedThread")
            this.socket = socket
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket!!.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
                Log.e(TAG, "temp sockets not created", e)
            }
            _inStream = tmpIn
            _outStream = tmpOut
            this@BluetoothService.state = STATE_CONNECTED
        }

        override fun run() {
            Log.i(TAG, "BEGIN mConnectedThread")
                val buffer = ByteArray(128)

            // Keep listening to the InputStream while connected
            while (this@BluetoothService.state == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    _inStream!!.read(buffer)
                    if (buffer[0] == '{'.code.toByte()) {
                        _flow.value = BluetoothConnectionState.Connected(
                            JSON.parseObject(buffer, DeviceState::class.java)
                        ).apply { name = "NIUBI" }
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "disconnected", e)
                    connectionLost()
                    break
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        fun write(buffer: ByteArray) {
            try {
                _outStream!!.write(buffer)
            } catch (e: IOException) {
                Log.e(TAG, "Exception during write", e)
            }
        }

        fun cancel() {
            try {
                socket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed", e)
            }
        }
    }

    companion object {
        // Debugging
        private const val TAG = "BluetoothChatService"
        private const val NAME_INSECURE = "BluetoothChatInsecure"

        @JvmStatic
        private val UUID_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        // Constants that indicate the current connection state
        const val STATE_NONE = 0 // we're doing nothing
        const val STATE_LISTEN = 1 // now listening for incoming connections
        const val STATE_CONNECTING = 2 // now initiating an outgoing connection
        const val STATE_CONNECTED = 3 // now connected to a remote device
    }
}