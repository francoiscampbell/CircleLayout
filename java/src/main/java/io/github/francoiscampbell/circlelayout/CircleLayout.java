package io.github.francoiscampbell.circlelayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by francois on 2016-01-12.
 */
public class CircleLayout extends ViewGroup {
    private int centerViewId;
    private float angle;

    public CircleLayout(Context context) {
        this(context, null);
    }

    public CircleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CircleLayout, defStyleAttr, 0);
        centerViewId = attributes.getResourceId(R.styleable.CircleLayout_centerView, View.NO_ID);
        angle = (float) Math.toRadians(attributes.getFloat(R.styleable.CircleLayout_angle, 0));
        attributes.recycle();
    }


    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void onLayout(boolean changed, int l, int t, int r, int b) {
        int displayAreaLeft = getLeft() + getPaddingLeft();
        int displayAreaTop = getTop() + getPaddingTop();
        int displayAreaRight = getRight() - getPaddingRight();
        int displayAreaBottom = getBottom() - getPaddingBottom();

        int displayAreaWidth = displayAreaRight - displayAreaLeft;
        int displayAreaHeight = displayAreaBottom - displayAreaTop;
        int centerX = getPaddingLeft() + displayAreaWidth / 2;
        int centerY = getPaddingRight() + displayAreaHeight / 2;
        int outerRadius = Math.min(displayAreaWidth, displayAreaHeight) / 2;

        View centerView = findViewById(centerViewId);
        if (centerView != null) {
            layoutAtCenter(centerView, centerX, centerY);
        }

        int childCount = getChildCount();
        View[] childrenToLayout = new View[childCount];
        int childIndex = 0;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child != null && (child.getId() != centerViewId || child.getId() == View.NO_ID) && child.getVisibility() != GONE) {
                childrenToLayout[childIndex++] = child;
            }
        }

        float angleIncrement = angle;
        if (angleIncrement == 0) {
            angleIncrement = getEqualAngle(childIndex);
        }
        layoutChildrenAtFixedAngle(centerX, centerY, outerRadius, angleIncrement, childrenToLayout);
    }

    private float getEqualAngle(int numViews) {
        return 2 * (float) Math.PI / numViews;
    }

    private void layoutChildrenAtFixedAngle(int cx, int cy, int outerRadius, float angle, View[] children) {
        float currentAngle = 0.0f;
        for (View child : children) {
            if (child == null) {
                continue;
            }
            int innerWidth = outerRadius - child.getMeasuredWidth() / 2;
            int innerHeight = outerRadius - child.getMeasuredHeight() / 2;
            int semiMajorAxis = Math.min(innerWidth, innerHeight);
            int semiMinorAxis = Math.max(innerWidth, innerHeight);
            float radius = getOvalRadiusAtAngle(semiMajorAxis, semiMinorAxis, currentAngle);

            Point childCenter = toPoint(radius, currentAngle);
            layoutAtCenter(child, cx + childCenter.x, cy - childCenter.y);

            currentAngle += angle;
        }
    }

    private static void layoutAtCenter(View view, int cx, int cy) {
        int left = cx - view.getMeasuredWidth() / 2;
        int top = cy - view.getMeasuredHeight() / 2;
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
        view.layout(left, top, right, bottom);
    }

    public Point toPoint(float radius, float angle) {
        int newX = (int) (radius * Math.cos(angle));
        int newY = (int) (radius * Math.sin(angle));
        return new Point(newX, newY);
    }

    public float getOvalRadiusAtAngle(int semiMajorAxis, int semiMinorAxis, float angle) {
        float sin = (float) Math.sin(angle);
        float cos = (float) Math.cos(angle);
        return semiMajorAxis * semiMinorAxis / (float) Math.sqrt(semiMinorAxis * semiMinorAxis * cos * cos + semiMajorAxis * semiMajorAxis * sin * sin);
    }
}

