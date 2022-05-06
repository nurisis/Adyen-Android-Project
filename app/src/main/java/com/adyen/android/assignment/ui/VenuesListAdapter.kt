package com.adyen.android.assignment.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adyen.android.assignment.api.model.VenueResult
import com.adyen.android.assignment.databinding.ItemVenueBinding

class VenuesListAdapter(
    private val onItemClick: (venue: VenueResult) -> Unit,
) : ListAdapter<VenueResult, VenuesListAdapter.VenueHolder>(
    VenueDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VenueHolder {
        return VenueHolder(
            ItemVenueBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onItemClick
        )
    }

    override fun onBindViewHolder(holder: VenueHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VenueHolder constructor(
        private val binding: ItemVenueBinding,
        private val onItemClick: (venue: VenueResult) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        private val categoryListAdapter: VenueCategoryListAdapter = VenueCategoryListAdapter()

        init {
            binding.categoryRecyclerView.adapter = categoryListAdapter
        }

        fun bind(item: VenueResult) {
            binding.name.text = item.name
            binding.distance.text = "${item.distance}m"

            categoryListAdapter.submitList(item.categories)

//            Glide.with(binding.root.context)
//                .load(item.)
//                .centerCrop()
//                .into(binding.venuImageView)

            binding.root.setOnClickListener { onItemClick.invoke(item) }
        }
    }
}

private class VenueDiffCallback : DiffUtil.ItemCallback<VenueResult>() {
    override fun areItemsTheSame(oldItem: VenueResult, newItem: VenueResult): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: VenueResult,
        newItem: VenueResult
    ): Boolean {
        return oldItem == newItem
    }
}
