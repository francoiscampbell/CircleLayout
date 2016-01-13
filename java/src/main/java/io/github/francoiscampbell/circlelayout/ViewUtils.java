package io.github.francoiscampbell.circlelayout;

import android.view.View;

/**
 * Created by francois on 2016-01-12.
 */
public class ViewUtils {
    static int getRadius(View view) {
        return Math.max(view.getMeasuredWidth(), view.getMeasuredHeight()) / 2;
    }

    static void layoutAtCenter(View view, int cx, int cy) {
        int left = cx - view.getMeasuredWidth() / 2;
        int top = cy - view.getMeasuredHeight() / 2;
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
        view.layout(left, top, right, bottom);
    }
}
