package io.github.francoiscampbell.circlelayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by francois on 2016-01-12.
 */
public class CircleLayout extends ViewGroup {
    private enum RadiusOverride {
        FITS_SMALLEST_CHILD(0), FITS_LARGEST_CHILD(1);

        private int value;

        RadiusOverride(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private int centerViewId;
    private float angle;
    private float angleOffset;
    private int fixedRadius;
    private int radiusOverride;

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
        angleOffset = (float) Math.toRadians(attributes.getFloat(R.styleable.CircleLayout_angleOffset, 0));
        fixedRadius = attributes.getDimensionPixelSize(R.styleable.CircleLayout_radius, 0);
        radiusOverride = attributes.getInt(R.styleable.CircleLayout_radiusOverride, RadiusOverride.FITS_LARGEST_CHILD.getValue());
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
            ViewUtils.layoutAtCenter(centerView, centerX, centerY);
        }

        int childCount = getChildCount();
        View[] childrenToLayout = new View[childCount];
        int childIndex = 0;
        int minChildRadius = outerRadius;
        int maxChildRadius = 0;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child != null
                    && (child.getId() != centerViewId || child.getId() == View.NO_ID)
                    && child.getVisibility() != GONE) {
                childrenToLayout[childIndex++] = child;
            }
            int childRadius = ViewUtils.getRadius(child);
            if (childRadius > maxChildRadius) {
                maxChildRadius = childRadius;
            }
            if (childRadius < minChildRadius) {
                minChildRadius = childRadius;
            }
        }

        //choose angle increment
        float angleIncrement = angle;
        if (angleIncrement == 0) {
            angleIncrement = getEqualAngle(childIndex);
        }

        //choose radius
        int layoutRadius = fixedRadius;
        if (layoutRadius == 0) {
            if (radiusOverride == RadiusOverride.FITS_LARGEST_CHILD.value) {
                layoutRadius = outerRadius - maxChildRadius;
            }
            if (radiusOverride == RadiusOverride.FITS_SMALLEST_CHILD.value) {
                layoutRadius = outerRadius - minChildRadius;
            }
        }

        layoutChildrenAtAngle(centerX, centerY, angleIncrement, angleOffset, layoutRadius, childrenToLayout);
    }

    private float getEqualAngle(int numViews) {
        return 2 * (float) Math.PI / numViews;
    }

    private void layoutChildrenAtAngle(int cx, int cy, float angleIncrement, float angleOffset, int radius, View[] children) {
        float currentAngle = angleOffset;
        for (View child : children) {
            if (child == null) {
                continue;
            }

            int childCenterX = polarToX(radius, currentAngle);
            int childCenterY = polarToY(radius, currentAngle);
            ViewUtils.layoutAtCenter(child, cx + childCenterX, cy - childCenterY);

            currentAngle += angleIncrement;
        }
    }

    public int polarToX(float radius, float angle) {
        return (int) (radius * Math.cos(angle));
    }

    public int polarToY(float radius, float angle) {
        return (int) (radius * Math.sin(angle));
    }
}

