package io.github.francoiscampbell.circlelayout

import android.view.View
import android.view.ViewGroup

/**
 * Created by francois on 2016-01-12.
 */
/**
 * @param view The view from which to get the radius
 * *
 * @return The effective radius of the view, if it were round
 */
val View.radius: Int
    get() = Math.max(measuredWidth, measuredHeight) / 2

/**
 * Lays out a view so that its center will be on `cx` and `cy`

 * @param view The view to layout
 * *
 * @param cx   The X coordinate of the location in the parent in which to place the view
 * *
 * @param cy   The Y coordinate of the location in the parent in which to place the view
 */
fun View.layoutFromCenter(cx: Int, cy: Int) {
    val left = cx - measuredWidth / 2
    val top = cy - measuredHeight / 2
    val right = left + measuredWidth
    val bottom = top + measuredHeight
    layout(left, top, right, bottom)
}

operator fun ViewGroup.get(i: Int): View = getChildAt(i)
