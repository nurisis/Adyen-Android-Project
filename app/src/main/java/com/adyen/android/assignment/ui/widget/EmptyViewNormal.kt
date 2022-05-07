package com.adyen.android.assignment.ui.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.adyen.android.assignment.R
import com.adyen.android.assignment.databinding.EmptyViewNormalBinding

class EmptyViewNormal @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null
) : ConstraintLayout(context, attributeSet) {

    private val binding: EmptyViewNormalBinding =
        EmptyViewNormalBinding.inflate(LayoutInflater.from(context), this)

    var image: Drawable? = null
        set(value) {
            field = value
            value?.let { binding.imageView.setImageDrawable(it) }
        }

    var imageResId: Int? = null
        set(value) {
            field = value
            binding.imageView.setImageResource(value ?: R.drawable.ic_smile_24)
        }

    var title: String = ""
        set(value) {
            field = value
            binding.titleTextView.text = value
        }

    var message: String = ""
        set(value) {
            field = value
            binding.messageTextView.text = value
        }

    var buttonText: String? = null
        set(value) {
            field = value
            binding.ctaButton.text = value
            binding.ctaButton.visibility = if (buttonText == null) View.GONE else View.VISIBLE
        }

    var buttonClickListener: OnClickListener? = null
        set(value) {
            field = value
            binding.ctaButton.setOnClickListener(value)
        }

    init {
        buttonText = null
    }
}
