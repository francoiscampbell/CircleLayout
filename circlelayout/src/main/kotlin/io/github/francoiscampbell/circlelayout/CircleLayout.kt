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
class CircleLayout(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ViewGroup(context, attrs, defStyleAttr) {

    private val centerViewId: Int
    private val angle: Float
    private val angleOffset: Float
    private val fixedRadius: Int
    private val radiusOverride: Int
    private val direction: Int

    private val childrenToLayout = LinkedList<View>()

    val layoutHasCenterView: Boolean
        get() = centerViewId != View.NO_ID

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.cl_CircleLayout, defStyleAttr, 0)
        centerViewId = attributes.getResourceId(R.styleable.cl_CircleLayout_cl_centerView, View.NO_ID)
        angle = Math.toRadians(attributes.getFloat(R.styleable.cl_CircleLayout_cl_angle, 0f).toDouble()).toFloat()
        angleOffset = Math.toRadians(attributes.getFloat(R.styleable.cl_CircleLayout_cl_angleOffset, 0f).toDouble()).toFloat()
        fixedRadius = attributes.getDimensionPixelSize(R.styleable.cl_CircleLayout_cl_radius, 0)
        radiusOverride = attributes.getInt(R.styleable.cl_CircleLayout_cl_radiusPreset, FITS_LARGEST_CHILD)
        direction = attributes.getInt(R.styleable.cl_CircleLayout_cl_direction, COUNTER_CLOCKWISE)
        attributes.recycle()
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

        val centerView = findViewById(centerViewId)
        centerView?.layoutFromCenter(centerX, centerY)


        var minChildRadius = outerRadius
        var maxChildRadius = 0
        childrenToLayout.clear()
        forEachChild {
            if (layoutHasCenterView && id == centerViewId || visibility == GONE) {
                return@forEachChild
            }
            childrenToLayout.add(this)
            when {
                radius > maxChildRadius -> {
                    maxChildRadius = radius
                }
                radius < minChildRadius -> {
                    minChildRadius = radius
                }
            }
        }
        //choose angle increment
        val angleIncrement = if (angle != 0f) angle else getEqualAngle(childrenToLayout.size)

        //choose radius
        val layoutRadius = if (fixedRadius != 0) fixedRadius else getLayoutRadius(outerRadius, maxChildRadius, minChildRadius)

        layoutChildrenAtAngle(centerX, centerY, angleIncrement, angleOffset, layoutRadius, childrenToLayout)
    }

    private fun getLayoutRadius(outerRadius: Int, maxChildRadius: Int, minChildRadius: Int): Int {
        when (radiusOverride) {
            FITS_LARGEST_CHILD -> return outerRadius - maxChildRadius
            FITS_SMALLEST_CHILD -> return outerRadius - minChildRadius
            else -> return outerRadius - maxChildRadius
        }
    }

    /**
     * Splits a circle into `n` equal slices
     * @param numSlices The number of slices in which to divide the circle
     * @return The angle between two adjacent slices, or 2*pi if `n` is zero
     */
    private fun getEqualAngle(numSlices: Int): Float = 2 * Math.PI.toFloat() / if (numSlices != 0) numSlices else 1

    /**
     * Lays out the visible child views along the circle
     * @param cy                    The Y coordinate of the center of the circle
     * @param angleIncrement        The angle increment between two adjacent children
     * @param angleOffset           The starting offset angle from the horizontal axis
     * @param radius                The radius of the circle along which the centers of the children will be placed
     * @param childrenToLayout      The views to layout. Any null views are ignored
     */
    private fun layoutChildrenAtAngle(cx: Int, cy: Int, angleIncrement: Float, angleOffset: Float, radius: Int, childrenToLayout: List<View>) {
        var currentAngle = angleOffset
        childrenToLayout.forEach {
            val childCenterX = polarToX(radius.toFloat(), currentAngle)
            val childCenterY = polarToY(radius.toFloat(), currentAngle)
            it.layoutFromCenter(cx + childCenterX, cy - childCenterY)

            currentAngle += angleIncrement * direction
        }
    }

    /**
     * Gets the X coordinate from a set of polar coordinates
     * @param radius The polar radius
     * @param angle  The polar angle
     * @return The equivalent X coordinate
     */
    fun polarToX(radius: Float, angle: Float): Int = (radius * Math.cos(angle.toDouble())).toInt()

    /**
     * Gets the Y coordinate from a set of polar coordinates
     * @param radius The polar radius
     * @param angle  The polar angle
     * @return The equivalent Y coordinate
     */
    fun polarToY(radius: Float, angle: Float): Int = (radius * Math.sin(angle.toDouble())).toInt()


    companion object {
        /**
         * The type of override for the radius of the circle
         */
        private val FITS_SMALLEST_CHILD = 0
        private val FITS_LARGEST_CHILD = 1

        /**
         * The direction of rotation, 1 for counter-clockwise, -1 for clockwise
         */
        private val COUNTER_CLOCKWISE = 1
        private val CLOCKWISE = -1
    }
}

