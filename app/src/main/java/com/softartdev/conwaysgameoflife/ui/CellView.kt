package com.softartdev.conwaysgameoflife.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.softartdev.conwaysgameoflife.R

class CellView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var isLive: Boolean = false
        set(value) {
            val drawableResId = if (value) R.drawable.cell_live_bg else R.drawable.cell_dead_bg
            setBackgroundResource(drawableResId)
            field = value
        }

    init {
        isLive = false
    }
}