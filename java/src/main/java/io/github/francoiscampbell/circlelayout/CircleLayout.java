package io.github.francoiscampbell.circlelayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * A layout that lays out its children in a circle
 */
public class CircleLayout extends ViewGroup {

    /**
     * The type of override for the radius of the circle
     */
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

    /**
     * Initializes this layout
     * @param context A view context. Cannot be an application context.
     */
    public CircleLayout(Context context) {
        this(context, null);
    }

    /**
     * Initializes this layout
     * @param context A view context. Cannot be an application context.
     * @param attrs The set of attributes to customize the layout
     */
    public CircleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Initializes this layout
     * @param context A view context. Cannot be an application context.
     * @param attrs The set of attributes to customize the layout
     * @param defStyleAttr The default style to use
     */
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
            ViewUtils.layoutFromCenter(centerView, centerX, centerY);
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

    /**
     * Splits a circle into {@code n} equal slices
     * @param n The number of slices in which to divide the circle
     * @return The angle between two adjacent slices, or 2*pi if {@code n} is zero
     */
    private float getEqualAngle(int n) {
        if (n == 0) {
            n = 1;
        }
        return 2 * (float) Math.PI / n;
    }

    /**
     * Lays out the visible child views along the circle
     * @param cx The X coordinate of the center of the circle
     * @param cy The Y coordinate of the center of the circle
     * @param angleIncrement The angle increment between two adjacent children
     * @param angleOffset The starting offset angle from the horizontal axis
     * @param radius The radius of the circle along which the centers of the children will be placed
     * @param children The views to layout. Any null views are ignored
     */
    private void layoutChildrenAtAngle(int cx, int cy, float angleIncrement, float angleOffset, int radius, View[] children) {
        float currentAngle = angleOffset;
        for (View child : children) {
            if (child == null) {
                continue;
            }

            int childCenterX = polarToX(radius, currentAngle);
            int childCenterY = polarToY(radius, currentAngle);
            ViewUtils.layoutFromCenter(child, cx + childCenterX, cy - childCenterY);

            currentAngle += angleIncrement;
        }
    }

    /**
     * Gets the X coordinate from a set of polar coordinates
     * @param radius The polar radius
     * @param angle The polar angle
     * @return The equivalent X coordinate
     */
    public int polarToX(float radius, float angle) {
        return (int) (radius * Math.cos(angle));
    }

    /**
     * Gets the Y coordinate from a set of polar coordinates
     * @param radius The polar radius
     * @param angle The polar angle
     * @return The equivalent Y coordinate
     */
    public int polarToY(float radius, float angle) {
        return (int) (radius * Math.sin(angle));
    }
}

