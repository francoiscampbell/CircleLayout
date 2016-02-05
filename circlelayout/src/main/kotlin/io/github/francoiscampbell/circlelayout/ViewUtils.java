package io.github.francoiscampbell.circlelayout;

import android.view.View;

/**
 * Created by francois on 2016-01-12.
 */
public class ViewUtils {
    /**
     * @param view The view from which to get the radius
     * @return The effective radius of the view, if it were round
     */
    static int getRadius(View view) {
        return Math.max(view.getMeasuredWidth(), view.getMeasuredHeight()) / 2;
    }

    /**
     * Lays out a view so that its center will be on {@code cx} and {@code cy}
     *
     * @param view The view to layout
     * @param cx   The X coordinate of the location in the parent in which to place the view
     * @param cy   The Y coordinate of the location in the parent in which to place the view
     */
    static void layoutFromCenter(View view, int cx, int cy) {
        int left = cx - view.getMeasuredWidth() / 2;
        int top = cy - view.getMeasuredHeight() / 2;
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
        view.layout(left, top, right, bottom);
    }
}
