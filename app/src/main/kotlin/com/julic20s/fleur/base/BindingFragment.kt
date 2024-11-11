package com.julic20s.fleur.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BindingFragment<B : ViewBinding> : Fragment() {

    private lateinit var _binding: B

    val binding : B get() = _binding

    abstract fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : B

    open fun onViewBinding(binding: B, savedInstanceState: Bundle?) {
    }

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = onCreateBinding(inflater, container, savedInstanceState)
        return _binding.root
    }

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewBinding(_binding, savedInstanceState)
    }

}