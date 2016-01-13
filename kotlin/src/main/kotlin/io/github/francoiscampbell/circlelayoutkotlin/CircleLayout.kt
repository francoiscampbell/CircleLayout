package io.github.francoiscampbell.circlelayoutkotlin

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

/**
 * Created by francois on 2016-01-12.
 */
class CircleLayout(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ViewGroup(context, attrs, defStyleAttr) {
    private var centerViewId: Int
    private var angle: Float

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CircleLayout, defStyleAttr, 0)
        centerViewId = attributes.getResourceId(R.styleable.CircleLayout_centerView, View.NO_ID)
        angle = Math.toRadians(attributes.getFloat(R.styleable.CircleLayout_angle, 0f).toDouble()).toFloat()
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
        centerView?.layoutAtCenter(centerX, centerY)

        val childCount = childCount
        val childrenToLayout = arrayOfNulls<View>(childCount)
        var childIndex = 0
        for (i in 0..childCount - 1) {
            val child = getChildAt(i) ?: continue
            if ((child.id != centerViewId || child.id == View.NO_ID) && child.visibility != View.GONE) {
                childrenToLayout[childIndex++] = child
            }
        }

        var angleIncrement = angle
        if (angleIncrement == 0f) {
            angleIncrement = getEqualAngle(childIndex)
        }
        layoutChildrenAtFixedAngle(centerX, centerY, outerRadius, angleIncrement, childrenToLayout)
    }

    private fun getEqualAngle(numViews: Int) = 2 * Math.PI.toFloat() / numViews

    private fun layoutChildrenAtFixedAngle(cx: Int, cy: Int, outerRadius: Int, angle: Float, children: Array<View?>) {
        var currentAngle = 0.0f
        for (child in children) {
            child ?: continue
            val innerWidth = outerRadius - child.measuredWidth / 2
            val innerHeight = outerRadius - child.measuredHeight / 2
            val semiMajorAxis = Math.min(innerWidth, innerHeight)
            val semiMinorAxis = Math.max(innerWidth, innerHeight)
            val radius = getOvalRadiusAtAngle(semiMajorAxis, semiMinorAxis, currentAngle)

            val childCenter = toPoint(radius, currentAngle)
            child.layoutAtCenter(cx + childCenter.x, cy - childCenter.y)

            currentAngle += angle
        }
    }

    private fun View.layoutAtCenter(cx: Int, cy: Int) {
        val left = cx - measuredWidth / 2
        val top = cy - measuredHeight / 2
        val right = left + measuredWidth
        val bottom = top + measuredHeight
        layout(left, top, right, bottom)
    }

    fun toPoint(radius: Float, angle: Float): Point {
        val newX = (radius * Math.cos(angle.toDouble())).toInt()
        val newY = (radius * Math.sin(angle.toDouble())).toInt()
        return Point(newX, newY)
    }

    fun getOvalRadiusAtAngle(semiMajorAxis: Int, semiMinorAxis: Int, angle: Float): Float {
        val sin = Math.sin(angle.toDouble())
        val cos = Math.cos(angle.toDouble())
        val cosSquared = cos * cos;
        val sinSquared = sin * sin;

        val numerator = semiMajorAxis * semiMinorAxis
        val semiMinorSquared = semiMinorAxis * semiMinorAxis
        val semiMajorSquared = semiMajorAxis * semiMajorAxis

        return numerator / Math.sqrt(semiMinorSquared * cosSquared + semiMajorSquared * sinSquared).toFloat()
    }
}

