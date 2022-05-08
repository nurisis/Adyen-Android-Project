package com.adyen.android.assignment.ui.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adyen.android.assignment.R
import com.adyen.android.assignment.api.model.VenueResult
import com.adyen.android.assignment.databinding.ItemVenueBinding
import com.adyen.android.assignment.widget.HorizontalSpacingItemDecoration

class VenueListAdapter : ListAdapter<VenueResult, VenueListAdapter.VenueHolder>(
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
            binding.categoryRecyclerView.addItemDecoration(
                HorizontalSpacingItemDecoration(
                    space = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        10f,
                        binding.root.context.resources.displayMetrics
                    ).toInt()
                )
            )
        }

        fun bind(item: VenueResult) {
            binding.name.text = item.name
            binding.address.text = item.location.formatted_address
            binding.distance.text =
                binding.root.context.getString(R.string.main_item_distance, item.distance)

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
