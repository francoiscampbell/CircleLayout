package io.github.francoiscampbell.circlelayout

import android.view.View

/**
 * Created by francois on 2016-01-12.
 */
object ViewUtils {
    /**
     * @param view The view from which to get the radius
     * *
     * @return The effective radius of the view, if it were round
     */
    internal fun getRadius(view: View): Int {
        return Math.max(view.measuredWidth, view.measuredHeight) / 2
    }

    /**
     * Lays out a view so that its center will be on `cx` and `cy`

     * @param view The view to layout
     * *
     * @param cx   The X coordinate of the location in the parent in which to place the view
     * *
     * @param cy   The Y coordinate of the location in the parent in which to place the view
     */
    internal fun layoutFromCenter(view: View, cx: Int, cy: Int) {
        val left = cx - view.measuredWidth / 2
        val top = cy - view.measuredHeight / 2
        val right = left + view.measuredWidth
        val bottom = top + view.measuredHeight
        view.layout(left, top, right, bottom)
    }
}
