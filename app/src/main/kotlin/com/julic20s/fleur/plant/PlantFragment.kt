package com.julic20s.fleur.plant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.julic20s.fleur.base.BindingFragment
import com.julic20s.fleur.data.PlantInfo
import com.julic20s.fleur.databinding.PlantFragmentBinding
import com.julic20s.fleur.databinding.PlantInfoItemBinding

class PlantFragment : BindingFragment<PlantFragmentBinding>() {

    private val _viewModel by viewModels<PlantViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = PlantFragmentBinding.inflate(inflater, container, false)

    override fun onViewBinding(binding: PlantFragmentBinding, savedInstanceState: Bundle?) {
        val id = requireArguments().getInt("id")
        val plant = _viewModel.plantsRepository.getPlant(id)!!
        binding.img.setImageResource(plant.res)
        binding.text.text = plant.description
        binding.info.adapter = InfoAdapter(plant)
    }

    class InfoViewHolder(private val binding: PlantInfoItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(key: String, value: String) {
            binding.key.text = key
            binding.value.text = value
        }
    }

    inner class InfoAdapter(plant: PlantInfo) : RecyclerView.Adapter<InfoViewHolder>() {
        private val _list = plant.info.toList()
        override fun getItemCount(): Int = _list.size
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoViewHolder {
            return PlantInfoItemBinding.inflate(layoutInflater, parent, false).let(::InfoViewHolder)
        }
        override fun onBindViewHolder(holder: InfoViewHolder, position: Int) {
            holder.bind(_list[position].first, _list[position].second)
        }
    }

}