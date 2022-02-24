package com.softartdev.conwaysgameoflife.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.widget.GridLayout
import com.softartdev.conwaysgameoflife.model.CellState.LIFE_SIZE

class CellLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : GridLayout(context, attrs, defStyleAttr) {

    private val cellViews: Array<Array<CellView>> = Array(size = LIFE_SIZE, init = { x ->
        Array(size = LIFE_SIZE, init = { y ->
            CellView(context, x, y)
        })
    })

    init {
        columnCount = LIFE_SIZE
        rowCount = LIFE_SIZE
        forEachCell { x, y -> addView(cellViews[x][y]) }
    }

    fun repaint(generation: Array<BooleanArray>) = forEachCell { x, y ->
        cellViews[x][y].isLive = generation[x][y]
    }

    fun setOnCellClickListener(listener: (x: Int, y: Int) -> Unit) {
        val cellListener = OnClickListener { view: View ->
            val cellView = view as CellView
            listener(cellView.dx, cellView.dy)
        }
        forEachCell { x, y ->
            cellViews[x][y].setOnClickListener(cellListener)
        }
    }

    private inline fun forEachCell(action: (x: Int, y: Int) -> Unit) {
        for (x in 0 until LIFE_SIZE) {
            for (y in 0 until LIFE_SIZE) {
                action(x, y)
            }
        }
    }
}