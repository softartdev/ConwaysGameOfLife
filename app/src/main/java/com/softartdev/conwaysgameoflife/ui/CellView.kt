package com.softartdev.conwaysgameoflife.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.GridLayout
import com.softartdev.conwaysgameoflife.R

@SuppressLint("ViewConstructor")
class CellView(
    context: Context,
    val dx: Int,
    val dy: Int
) : View(context) {

    var isLive: Boolean = false
        set(value) {
            val drawableResId = if (value) R.drawable.cell_live_bg else R.drawable.cell_dead_bg
            setBackgroundResource(drawableResId)
            field = value
        }

    init {
        val cellSize = resources.getDimensionPixelSize(R.dimen.cell_size)
        layoutParams = GridLayout.LayoutParams(GridLayout.spec(dy), GridLayout.spec(dx)).apply {
            width = cellSize
            height = cellSize
        }
        isLive = false
    }
}