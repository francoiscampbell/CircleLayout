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
        defStyleAttr,
        defStyleRes
) {
    var angle: Float
    var angleOffset: Float
    var fixedRadius: Int
    var radiusPreset = FITS_LARGEST_CHILD
        set(newRadiusPreset: Int) = when (newRadiusPreset) {
            FITS_LARGEST_CHILD, FITS_SMALLEST_CHILD -> field = newRadiusPreset
            else -> throw IllegalArgumentException("radiusPreset must be either FITS_LARGEST_CHILD or FITS_SMALLEST_CHILD")
        }
    var direction = COUNTER_CLOCKWISE
        set(newDirection: Int) = when {
            newDirection > 0 -> field = 1
            newDirection < 0 -> field = -1
            else -> throw IllegalArgumentException("direction must be either positive or negative")
        }

    val layoutHasCenterView: Boolean
        get() = centerViewId != View.NO_ID

    private var centerViewId: Int
    var centerView: View? = null
        set(newCenterView: View?) = when {
            newCenterView != null && indexOfChild(newCenterView) == -1 -> {
                throw IllegalArgumentException("View with ID ${newCenterView.id} is not a child of this layout")
            }
            else -> {
                field = newCenterView
                centerViewId = newCenterView?.id ?: NO_ID
            }
        }

    private val childrenToLayout = LinkedList<View>()

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CircleLayout, defStyleAttr, 0)
        centerViewId = attributes.getResourceId(R.styleable.CircleLayout_cl_centerView, NO_ID)
        angle = Math.toRadians(attributes.getFloat(R.styleable.CircleLayout_cl_angle, 0f).toDouble()).toFloat()
        angleOffset = Math.toRadians(attributes.getFloat(R.styleable.CircleLayout_cl_angleOffset, 0f).toDouble()).toFloat()
        fixedRadius = attributes.getDimensionPixelSize(R.styleable.CircleLayout_cl_radius, 0)
        radiusPreset = attributes.getInt(R.styleable.CircleLayout_cl_radiusPreset, FITS_LARGEST_CHILD)
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
        forEachChild {
            if (layoutHasCenterView && id == centerViewId || visibility == GONE) {
                return@forEachChild
            }
            childrenToLayout.add(this)
            maxChildRadius = Math.max(maxChildRadius, radius)
            minChildRadius = Math.min(minChildRadius, radius)
        }
        //choose angle increment
        val angleIncrement = if (angle != 0f) angle else getEqualAngle(childrenToLayout.size)

        //choose radius
        val layoutRadius = if (fixedRadius != 0) fixedRadius else getLayoutRadius(outerRadius, maxChildRadius, minChildRadius)

        layoutChildrenAtAngle(centerX, centerY, angleIncrement, angleOffset, layoutRadius, childrenToLayout)
    }

    /**
     * @param outerRadius The outer radius of this layout's display area
     * @param maxChildRadius The radius of the largest child
     * @param minChildRadius The radius of the smallest child
     * @return The radius of the layout path along which the children will be placed
     */
    private fun getLayoutRadius(outerRadius: Int, maxChildRadius: Int, minChildRadius: Int): Int {
        return when (radiusPreset) {
            FITS_LARGEST_CHILD -> outerRadius - maxChildRadius
            FITS_SMALLEST_CHILD -> outerRadius - minChildRadius
            else -> outerRadius - maxChildRadius
        }
    }

    /**
     * Splits a circle into `n` equal slices
     * @param numSlices The number of slices in which to divide the circle
     * @return The angle between two adjacent slices, or 2*pi if `n` is zero
     */
    private fun getEqualAngle(numSlices: Int): Float = 2 * Math.PI.toFloat() / if (numSlices != 0) numSlices else 1

    /**
     * Lays out the child views along a circle
     * @param cx                    The X coordinate of the center of the circle
     * @param cy                    The Y coordinate of the center of the circle
     * @param angleIncrement        The angle increment between two adjacent children
     * @param angleOffset           The starting offset angle from the horizontal axis
     * @param radius                The radius of the circle along which the centers of the children will be placed
     * @param childrenToLayout      The views to layout
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

