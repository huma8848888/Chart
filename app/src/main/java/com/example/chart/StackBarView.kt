package com.example.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.example.chart.databinding.LayoutBarBinding

data class BarDataBean(val pad_duration: Long, val app_duration: Long, val other_duration: Long)
data class BarLayoutParam(val pad_height : Int, val app_height : Int, val other_height : Int, val left: Int, val top: Int, val right: Int, val bottom: Int)

class StackBarView : FrameLayout {
    private var textSize: Float = 0.0f
    var yAxisLableList: List<String> = emptyList()
    var xAxisLableList: List<String> = emptyList()
    var barDataList: List<BarDataBean> = emptyList()
    var barViewList = arrayListOf<LinearLayout>()
    var barLayoutParamList = arrayListOf<BarLayoutParam>()


    //y轴标签paint
    private var yAxisLablePaint = Paint()

    //网格线
    private var dashLinePaint = Paint()
    private var xAxisLablePaint = Paint()
    private var barWidth = 0.0f

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        textSize = DensityUtil.dp2px(context, 12.0f).toFloat()
        //需要画一个小时字符
        lastYPosition = textSize
        //设置x轴标签的画笔
        xAxisLablePaint.color = context.getColor(R.color.app_000000_60)
        xAxisLablePaint.style = Paint.Style.FILL
        yAxisLablePaint.isAntiAlias = true
        xAxisLablePaint.textSize = textSize
        //设置y轴标签的画笔
        yAxisLablePaint.color = context.getColor(R.color.app_000000_60)
        yAxisLablePaint.style = Paint.Style.FILL
        yAxisLablePaint.isAntiAlias = true
        yAxisLablePaint.textSize = textSize
        yAxisLablePaint.textAlign = Paint.Align.RIGHT
        //设置网格线的画笔
        dashLinePaint.color = context.getColor(R.color.app_000000_05)
        dashLinePaint.style = Paint.Style.STROKE
        dashLinePaint.strokeWidth = DensityUtil.dp2px(context, 1.0f).toFloat()
        //设置虚线效果
        dashLinePaint.pathEffect = DashPathEffect(floatArrayOf(DensityUtil.dp2px(context, 2.0f).toFloat(), DensityUtil.dp2px(context, 2.0f).toFloat()), 0f)
        barWidth = DensityUtil.dp2px(context, 20.0f).toFloat()
    }

    fun setDataList(yAxisLableList : List<String>, xAxisLableList : List<String>, barData : List<BarDataBean>){
        this.yAxisLableList = yAxisLableList
        this.xAxisLableList = xAxisLableList
        this.barDataList = barData
        barViewList.clear()
        removeAllViews()
        xAxisLableList.forEachIndexed { index, lable ->
            val barViewBinding = LayoutBarBinding.inflate(LayoutInflater.from(context), this, false)
            barViewList.add(barViewBinding.root)
            addView(barViewBinding.root)
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        Log.d("test", "ondrawing")
        init()
        //画Y轴标签
        drawYAxisLable(canvas)
        //计算bar区的高度，并且计算时间和高度的换算比率
        initBarData()
        //画X轴
        drawXAxisLableAndBar(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Log.d("test", "onMeasure")
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Log.d("test", "onLayout")
        if (barViewList.isNotEmpty() && barLayoutParamList.isNotEmpty()){
            barViewList.forEachIndexed { index, itemView ->
                val barItemViewLayoutParam = barLayoutParamList[index]
                val otherView = itemView.findViewById<RoundFrameLayout>(R.id.other)
                otherView.layoutParams = LinearLayout.LayoutParams(barWidth.toInt(), barItemViewLayoutParam.other_height)
                val padView = itemView.findViewById<FrameLayout>(R.id.pad)
                padView.layoutParams = LinearLayout.LayoutParams(barWidth.toInt(), barItemViewLayoutParam.pad_height)
                val appView = itemView.findViewById<FrameLayout>(R.id.app)
                appView.layoutParams = LinearLayout.LayoutParams(barWidth.toInt(), barItemViewLayoutParam.app_height)
                itemView.measure(0, 0)
                itemView.layout(barItemViewLayoutParam.left, barItemViewLayoutParam.top, barItemViewLayoutParam.right, barItemViewLayoutParam.bottom)
            }
        }
    }

    //计算bar区的高度，并且计算时间和高度的换算比率
    private fun initBarData() {
        val totalHeightForBar = bottomDashLineY - topDashLineY
        var maxTotalTime = Integer.MIN_VALUE.toLong()
        barDataList.forEach { beanItem ->
            val currTime = beanItem.app_duration + beanItem.pad_duration + beanItem.other_duration
            if (currTime > maxTotalTime) {
                maxTotalTime = currTime
            }
        }
        //高度和时间的换算比例
        heightRation = totalHeightForBar / maxTotalTime.toFloat()
    }

    //上一次Y轴标签的位置
    //起始位置有"小时"字符
    private var lastYPosition = textSize

    //上一次X轴标签的位置
    private var lastXPosition = 0.0f

    //Y轴标签的最大宽度
    private var maxYAxisTextWidth = 0.0f
    private var topDashLineY = 0.0f
    private var bottomDashLineY = 0.0f
    private var heightRation = 0.0f

    //计算Y轴Lable的偏移量
    //因为Y轴是右对齐，需要计算出最大的Y轴标签的宽度，然后计算出偏移量
    private fun calculateXLableBias(){
        lastXPosition = xAxisLablePaint.measureText("小时")
        for (yLable in yAxisLableList){
            val textWidth = yAxisLablePaint.measureText(yLable)
            if (textWidth > lastXPosition){
                lastXPosition = textWidth
            }
        }
    }
    //画Y轴标签
    private fun drawYAxisLable(canvas: Canvas?) {
        val yAxisBottom = DensityUtil.dp2px(context, 14.0f)
        calculateXLableBias()
        val yAxisVerticalMargin: Float = if (yAxisLableList.size <= 1) {
            (height - yAxisBottom).toFloat()
        } else {
            (height - yAxisBottom - textSize - yAxisLableList.size * textSize) / (yAxisLableList.size)
        }
        canvas?.drawText("小时", lastXPosition, lastYPosition, yAxisLablePaint)
        //更新Y轴标签的最大宽度
        refreshMaxYAxisTextWidth("小时")
        //准备绘制y轴的数字lable，计算Y轴坐标
        lastYPosition += yAxisVerticalMargin + textSize
        yAxisLableList.forEachIndexed { index, yLable ->
            // 在文本的原点坐标处绘制一个小圆点（仅用于标识）
            // 在文本的原点坐标处绘制一个小圆点（仅用于标识）
            val dotPaint = Paint()
            dotPaint.color = Color.RED
            canvas!!.drawCircle(lastXPosition, lastYPosition, 5f, dotPaint)
            canvas.drawText(yLable, lastXPosition, lastYPosition, yAxisLablePaint)
            //更新Y轴标签的最大宽度
            refreshMaxYAxisTextWidth(yLable)
            //画一条对应的横向虚线
            drawDashLine(canvas, lastXPosition + DensityUtil.dp2px(context, 6.0f), lastYPosition - (textSize / 2))
            //更新上下虚线的位置
            if (index == 0) {
                topDashLineY = lastYPosition - textSize / 2
            } else if (index == yAxisLableList.lastIndex) {
                bottomDashLineY = lastYPosition - textSize / 2
            }
            lastYPosition += yAxisVerticalMargin + textSize
        }
    }

    private fun refreshMaxYAxisTextWidth(text: String) {
        if (yAxisLablePaint.measureText(text) > maxYAxisTextWidth) {
            maxYAxisTextWidth = yAxisLablePaint.measureText(text)
        }
    }

    private fun drawDashLine(canvas: Canvas?, startX: Float, startY: Float) {
        canvas?.drawLine(startX, startY, width.toFloat(), startY, dashLinePaint)
    }

    private fun drawXAxisLableAndBar(canvas: Canvas?) {
        //以今字作为横轴每个字符的宽度基准
        val xAxisTextWidth = xAxisLablePaint.measureText("今")
        val xAxisMargin = (width - maxYAxisTextWidth - DensityUtil.dp2px(context, 22.0f) - xAxisTextWidth * xAxisLableList.size - DensityUtil.dp2px(context, 19.0f)) / (xAxisLableList.size - 1)
        var startXPosition = lastXPosition + DensityUtil.dp2px(context, 22.0f)
        val startYAxisPosition = lastYPosition - DensityUtil.dp2px(context, 10f)
        xAxisLableList.forEachIndexed { index, xLableText ->
            canvas?.drawText(xLableText, startXPosition, startYAxisPosition, xAxisLablePaint)
            drawBarItemView(barDataList[index], startXPosition, startYAxisPosition, xAxisTextWidth, canvas)
            startXPosition += xAxisMargin + xAxisTextWidth
        }
    }

    private fun drawBarItemView(item: BarDataBean, textX: Float, textY: Float, textWidth: Float, canvas: Canvas?) {
        //文字距离bar底部的高度
        val textTop2BarBottomHeight = DensityUtil.dp2px(context, 6f)
        var linearLayoutItem = LayoutBarBinding.inflate(LayoutInflater.from(context), this, false)
        val otherHeight = item.other_duration * heightRation
        val appHeight = item.app_duration * heightRation
        val padHeight = item.pad_duration * heightRation
        linearLayoutItem.other.layoutParams = LinearLayout.LayoutParams(barWidth.toInt(), otherHeight.toInt())
        linearLayoutItem.app.layoutParams = LinearLayout.LayoutParams(barWidth.toInt(), appHeight.toInt())
        linearLayoutItem.pad.layoutParams = LinearLayout.LayoutParams(barWidth.toInt(), padHeight.toInt())
        linearLayoutItem.root.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        barWidth = linearLayoutItem.root.measuredWidth.toFloat()
        val barXBias = Math.abs((textWidth - barWidth) / 2f)
        val left = (textX - barXBias).toInt()
        val top = (textY - textSize - textTop2BarBottomHeight - linearLayoutItem.root.measuredHeight).toInt()
        val right = (textX + textSize + barXBias).toInt()
        val bottom = (textY - textSize - textTop2BarBottomHeight).toInt()
        barLayoutParamList.add(BarLayoutParam(padHeight.toInt(), appHeight.toInt(), otherHeight.toInt(), left, top, right, bottom))
//        linearLayoutItem.root.layout(left, top, right, bottom)
//        barViewList.plus(linearLayoutItem)
//        addView(linearLayoutItem.root)
    }
}