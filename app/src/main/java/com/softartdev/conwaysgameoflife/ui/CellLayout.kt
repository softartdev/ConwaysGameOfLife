package com.softartdev.conwaysgameoflife.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.GridLayout
import com.softartdev.conwaysgameoflife.R
import com.softartdev.conwaysgameoflife.model.CellState.LIFE_SIZE

class CellLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : GridLayout(context, attrs, defStyleAttr) {

    private val cells: Array<Array<CellView>> = Array(LIFE_SIZE) { x ->
        Array(LIFE_SIZE) cell@{ y ->
            val cellView = CellView(context).apply {
                layoutParams = LayoutParams(spec(y), spec(x)).apply {
                    width = this@CellLayout.resources.getDimensionPixelSize(R.dimen.cell_size)
                    height = this@CellLayout.resources.getDimensionPixelSize(R.dimen.cell_size)
                }
                tag = "x${x}y${y}"
            }
            addView(cellView)
            return@cell cellView
        }
    }

    init {
        columnCount = LIFE_SIZE
        rowCount = LIFE_SIZE
    }

    fun repaint(generation: Array<BooleanArray>) = forEachCell { x, y ->
        cells[x][y].isLive = generation[x][y]
    }

    fun setOnCellListener(cellListener: CellListener) = forEachCell { x, y ->
        cells[x][y].setOnClickListener { cellListener.onCell(x, y) }
    }

    private inline fun forEachCell(action: (x: Int, y: Int) -> Unit) {
        for (x in 0 until LIFE_SIZE) {
            for (y in 0 until LIFE_SIZE) {
                action(x, y)
            }
        }
    }

    interface CellListener {
        fun onCell(x: Int, y: Int)
    }
}