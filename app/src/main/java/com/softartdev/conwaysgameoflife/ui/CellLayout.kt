package com.softartdev.conwaysgameoflife.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.GridLayout
import androidx.core.view.children
import com.softartdev.conwaysgameoflife.model.CellState.LIFE_SIZE

class CellLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : GridLayout(context, attrs, defStyleAttr) {

    init {
        columnCount = LIFE_SIZE
        rowCount = LIFE_SIZE
        forEachCell { x, y -> addView(CellView(context, x, y)) }
    }

    fun repaint(generation: Array<BooleanArray>) = children
            .filterIsInstance(CellView::class.java)
            .forEach { it.isLive = generation[it.dx][it.dy] }

    fun setOnCellClickListener(listener: (x: Int, y: Int) -> Unit) {
        val cellListener = OnClickListener { view: View ->
            val cellView = view as CellView
            listener(cellView.dx, cellView.dy)
        }
        children.filterIsInstance(CellView::class.java)
                .forEach { it.setOnClickListener(cellListener) }
    }

    private inline fun forEachCell(action: (x: Int, y: Int) -> Unit) {
        for (x in 0 until LIFE_SIZE) {
            for (y in 0 until LIFE_SIZE) {
                action(x, y)
            }
        }
    }
}