package com.julic20s.fleur.wiki

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.julic20s.fleur.R
import com.julic20s.fleur.base.BindingFragment
import com.julic20s.fleur.data.PlantInfo
import com.julic20s.fleur.databinding.WikiFragmentBinding
import com.julic20s.fleur.databinding.WikiItemBinding

class WikiFragment : BindingFragment<WikiFragmentBinding>() {

    private val _viewModel by viewModels<WikiViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.move)
    }

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = WikiFragmentBinding.inflate(inflater, container, false)

    override fun onViewBinding(binding: WikiFragmentBinding, savedInstanceState: Bundle?) {
        binding.list.adapter = Adapter()
    }

    inner class ViewHolder(private val binding: WikiItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.img.clipToOutline = true
        }

        fun bind(plant: PlantInfo) {
            val bundle = bundleOf(
                "id" to plant.id
            )

            val onClick = View.OnClickListener {
                val extra = FragmentNavigatorExtras(
                    binding.root to binding.root.transitionName,
                    binding.img to binding.img.transitionName
                )
                findNavController().navigate(
                    R.id.action_wikiFragment_to_plantFragment,
                    bundle,
                    null,
                    extra,
                )
            }

            binding.root.setOnClickListener(onClick)
            binding.img.setOnClickListener(onClick)
            binding.img.setImageResource(plant.res)
            binding.name.text = plant.name
            binding.desc.text = plant.type
        }
    }

    inner class Adapter : RecyclerView.Adapter<ViewHolder>() {

        override fun getItemCount() = _viewModel.plants.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return WikiItemBinding.inflate(layoutInflater, parent, false).let(::ViewHolder)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(_viewModel.plants[position])
        }
    }
}