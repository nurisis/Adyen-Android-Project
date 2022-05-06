package com.adyen.android.assignment.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adyen.android.assignment.api.model.Category
import com.google.android.material.chip.Chip

class VenueCategoryListAdapter() : ListAdapter<Category, VenueCategoryListAdapter.CategoryHolder>(
    CategoryDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        return CategoryHolder(
            Chip(parent.context)
        )
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryHolder constructor(
        private val rootView: Chip,
    ) : RecyclerView.ViewHolder(rootView) {

        init {
            rootView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            rootView.isClickable = false
        }

        fun bind(item: Category) {
            rootView.text = item.name
        }
    }
}

private class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
    override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem.id== newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Category,
        newItem: Category
    ): Boolean {
        return oldItem == newItem
    }
}
