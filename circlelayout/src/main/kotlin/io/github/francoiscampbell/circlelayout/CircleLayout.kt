package io.github.francoiscampbell.circlelayout

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import java.util.*

/**
 * A layout that lays out its children in a circle
 * @param context      A view context. Cannot be an application context.
 * @param attrs        The set of attributes to customize the layout
 * @param defStyleAttr The default style to use
 */
class CircleLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0)
: ViewGroup(
        context,
        attrs,
        defStyleAttr
) {
    /**
     * (Optional) A fixed angle between views.
     */
    var angle: Float = 0f
        set (value) {
            field = value % 360f
            requestLayout()
        }

    /**
     *  The initial angle of the layout pass. A value of 0 will start laying out from the horizontal axis. Defaults to 0.
     */
    var angleOffset: Float = 0f
        set (value) {
            field = value % 360f
            requestLayout()
        }

    /**
     * The radius of the circle. Use a dimension, <code>FITS_SMALLEST_CHILD</code>, or <code>FITS_LARGEST_CHILD</code>. Defaults to <code>FITS_LARGEST_CHILD</code>.
     */
    var radius = FITS_LARGEST_CHILD
        set(value) {
            field = value
            requestLayout()
        }

    /**
     * The layout direction. Takes the sign (+/-) of the value only. Defaults to <code>COUNTER_CLOCKWISE</code>.
     */
    var direction = COUNTER_CLOCKWISE
        set(value) {
            field = Math.signum(value.toFloat()).toInt()
            requestLayout()
        }

    /**
     * Whether this layout currently has a visible view in the center
     */
    val hasCenterView: Boolean
        get() = centerView != null && centerView?.visibility != GONE

    private var centerViewId: Int

    /**
     * The view shown in the center of the circle
     */
    var centerView: View? = null
        set(newCenterView) {
            if (newCenterView != null && indexOfChild(newCenterView) == -1) {
                throw IllegalArgumentException("View with ID ${newCenterView.id} is not a child of this layout")
            }
            field = newCenterView
            centerViewId = newCenterView?.id ?: NO_ID
            requestLayout()
        }

    // Pre-allocate to avoid object allocation in onLayout
    private val childrenToLayout = LinkedList<View>()

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CircleLayout, defStyleAttr, defStyleRes)
        centerViewId = attributes.getResourceId(R.styleable.CircleLayout_cl_centerView, NO_ID)
        angle = attributes.getFloat(R.styleable.CircleLayout_cl_angle, 0f)
        angleOffset = attributes.getFloat(R.styleable.CircleLayout_cl_angleOffset, 0f)
        radius = attributes.getInt(R.styleable.CircleLayout_cl_radius, FITS_LARGEST_CHILD)
        direction = attributes.getInt(R.styleable.CircleLayout_cl_direction, COUNTER_CLOCKWISE)
        attributes.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        centerView = findViewById(centerViewId)
    }

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    public override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val displayAreaLeft = left + paddingLeft
        val displayAreaTop = top + paddingTop
        val displayAreaRight = right - paddingRight
        val displayAreaBottom = bottom - paddingBottom

        val displayAreaWidth = displayAreaRight - displayAreaLeft
        val displayAreaHeight = displayAreaBottom - displayAreaTop
        val centerX = paddingLeft + displayAreaWidth / 2
        val centerY = paddingRight + displayAreaHeight / 2
        val outerRadius = Math.min(displayAreaWidth, displayAreaHeight) / 2

        centerView?.layoutFromCenter(centerX, centerY)

        var minChildRadius = outerRadius
        var maxChildRadius = 0
        childrenToLayout.clear()
        for (i in 0..childCount - 1) {
            val child = getChildAt(i)
            if ((hasCenterView && child.id == centerViewId) || child.visibility == GONE) {
                continue
            }
            childrenToLayout.add(child)
            maxChildRadius = Math.max(maxChildRadius, child.radius)
            minChildRadius = Math.min(minChildRadius, child.radius)
        }
        //choose angle increment
        val angleIncrement = if (angle != 0f) angle else getEqualAngle(childrenToLayout.size)

        //choose radius
        val layoutRadius = getLayoutRadius(outerRadius, maxChildRadius, minChildRadius)

        layoutChildrenAtAngle(centerX, centerY, angleIncrement, angleOffset, layoutRadius, childrenToLayout)
    }

    /**
     * @param outerRadius The outer radius of this layout's display area
     * @param maxChildRadius The radius of the largest child
     * @param minChildRadius The radius of the smallest child
     * @return The radius of the layout path along which the children will be placed
     */
    fun getLayoutRadius(outerRadius: Int, maxChildRadius: Int, minChildRadius: Int): Int {
        return when (radius) {
            FITS_LARGEST_CHILD -> outerRadius - maxChildRadius
            FITS_SMALLEST_CHILD -> outerRadius - minChildRadius
            else -> Math.abs(radius)
        }
    }

    /**
     * Splits a circle into <code>n</code> equal slices
     * @param numSlices The number of slices in which to divide the circle
     * @return The angle between two adjacent slices in degrees, or 360 if <code>n</code> is zero
     */
    fun getEqualAngle(numSlices: Int): Float = 360f / if (numSlices != 0) numSlices else 1

    /**
     * Lays out the child views along a circle
     * @param cx                    The X coordinate of the center of the circle
     * @param cy                    The Y coordinate of the center of the circle
     * @param angleIncrement        The angle increment between two adjacent children, in degrees
     * @param angleOffset           The starting offset angle from the horizontal axis, in degrees
     * @param radius                The radius of the circle along which the centers of the children will be placed
     * @param childrenToLayout      The views to layout
     */
    private fun layoutChildrenAtAngle(cx: Int, cy: Int, angleIncrement: Float, angleOffset: Float, radius: Int, childrenToLayout: List<View>) {
        val angleIncrementRad = Math.toRadians(angleIncrement.toDouble())
        var currentAngleRad = Math.toRadians(angleOffset.toDouble())
        for (i in 0..childrenToLayout.size - 1) {
            val child = childrenToLayout[i]
            val childCenterX = polarToX(radius.toDouble(), currentAngleRad)
            val childCenterY = polarToY(radius.toDouble(), currentAngleRad)
            child.layoutFromCenter((cx + childCenterX).toInt(), (cy - childCenterY).toInt())

            currentAngleRad += angleIncrementRad * direction
        }
    }

    /**
     * Gets the X coordinate from a set of polar coordinates
     * @param radius The polar radius
     * @param angle  The polar angle, in radians
     * @return The equivalent X coordinate
     */
    fun polarToX(radius: Double, angle: Double) = radius * Math.cos(angle)

    /**
     * Gets the Y coordinate from a set of polar coordinates
     * @param radius The polar radius
     * @param angle  The polar angle, in radians
     * @return The equivalent Y coordinate
     */
    fun polarToY(radius: Double, angle: Double) = radius * Math.sin(angle)


    companion object {
        /**
         * Will adjust the radius to make the smallest child fit in the layout and larger children will bleed outside the radius.
         */
        const val FITS_SMALLEST_CHILD = -1
        /**
         * Will adjust the radius to make the largest child fit in the layout.
         */
        const val FITS_LARGEST_CHILD = -2

        /**
         * For use with <code>setDirection</code>
         */
        const val COUNTER_CLOCKWISE = 1
        /**
         * For use with <code>setDirection</code>
         */
        const val CLOCKWISE = -1
    }
}

