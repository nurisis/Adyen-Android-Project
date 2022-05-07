package com.adyen.android.assignment.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adyen.android.assignment.api.model.VenueResult
import com.adyen.android.assignment.databinding.ItemVenueBinding

class VenuesListAdapter : ListAdapter<VenueResult, VenuesListAdapter.VenueHolder>(
    VenueDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VenueHolder {
        return VenueHolder(
            ItemVenueBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    override fun onBindViewHolder(holder: VenueHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VenueHolder constructor(
        private val binding: ItemVenueBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        private val categoryListAdapter: VenueCategoryListAdapter = VenueCategoryListAdapter()

        init {
            binding.categoryRecyclerView.adapter = categoryListAdapter
        }

        fun bind(item: VenueResult) {
            binding.name.text = item.name
            binding.address.text = item.location.formatted_address
            binding.distance.text = "${item.distance}m away from you \uD83D\uDE0A"

            categoryListAdapter.submitList(item.categories)
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
