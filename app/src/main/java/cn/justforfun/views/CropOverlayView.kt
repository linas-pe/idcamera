package cn.justforfun.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.Nullable
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class CropOverlayView(context: Context, @Nullable attrs: AttributeSet?) : View(context, attrs) {
    private var defaultMargin = 100
    private val vertexSize = 30F
    private val gridSize = 3

    private var _bitmap: Bitmap? = null
    private var topLeft = Point(0, 0)
    private var topRight = Point(0, 0)
    private var bottomLeft = Point(0, 0)
    private var bottomRight = Point(0, 0)

    private var touchDownX = 0F
    private var touchDownY = 0F
    private var cropPosition = CropPosition.TOP_LEFT

    private var currentWidth = 0
    private var currentHeight = 0

    private var minX = 0
    private var maxX = 0
    private var minY = 0
    private var maxY = 0

    private val WIDTH_BLOCK = 40
    private val HEIGHT_BLOCK = 40

    constructor(context: Context) : this(context, null)

    fun setBitmap(bitmap: Bitmap) {
        _bitmap = bitmap
        resetPoints()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (width != currentWidth || height != currentHeight) {
            currentWidth = width
            currentHeight = height
            resetPoints()
        }
        Log.e("stk", "canvasSize=" + width + "x" + height)

        drawBackground(canvas)
        drawVertex(canvas)
        drawEdge(canvas)
    }

    private fun resetPoints() {
        if (_bitmap == null) {
            return
        }
        Log.e("stk", "resetPoints, bitmap=$_bitmap");

        // 1. calculate bitmap size in new canvas
        val scaleX = _bitmap!!.width * 1F / width
        val scaleY = _bitmap!!.height * 1F / height
        val maxScale = scaleX.coerceAtLeast(scaleY)

        // 2. determine minX, maxX if maxScale = scalY | minY , maxY if maxScale = scaleX
        var minX = 0
        var maxX = width
        var minY = 0
        var maxY = height

        if (maxScale == scaleY) {
            val bitmapInCanvasWidth = (_bitmap!!.width / maxScale).toInt()
            minX = (width - bitmapInCanvasWidth) / 2
            maxX = width - minX
        } else {
            val bitmapInCanvasWidth = (_bitmap!!.height / maxScale).toInt()
            minY = (height - bitmapInCanvasWidth) / 2
            maxY = height - minY
        }

        this.minX = minX
        this.minY = minY
        this.maxX = maxX
        this.maxY = maxY

        defaultMargin = if (maxX - minX < defaultMargin || maxY - minY < defaultMargin)
            0 else 30

        Log.e("stk", "maxX - minX=" + (maxX - minX))
        Log.e("stk", "maxY - minY=" + (maxY - minY))

        topLeft = Point(minX + defaultMargin, minY + defaultMargin)
        topRight = Point(maxX - defaultMargin, minY + defaultMargin)
        bottomLeft = Point(minX + defaultMargin, maxY - defaultMargin)
        bottomRight = Point(maxX - defaultMargin, maxY - defaultMargin)
    }

    private fun drawBackground(canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.parseColor("#66000000")
        paint.style = Paint.Style.FILL

        val path = Path()
        path.moveTo(topLeft.x.toFloat(), topLeft.y.toFloat())
        path.lineTo(topRight.x.toFloat(), topRight.y.toFloat())
        path.lineTo(bottomRight.x.toFloat(), bottomRight.y.toFloat())
        path.lineTo(bottomLeft.x.toFloat(), bottomLeft.y.toFloat())
        path.close()

        canvas.save()
        canvas.clipPath(path, Region.Op.DIFFERENCE)
        canvas.drawColor(Color.parseColor("#66000000"))
        canvas.restore()
    }

    private fun drawVertex(canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL

        canvas.drawCircle(topLeft.x.toFloat(), topLeft.y.toFloat(), vertexSize, paint)
        canvas.drawCircle(topRight.x.toFloat(), topRight.y.toFloat(), vertexSize, paint)
        canvas.drawCircle(bottomLeft.x.toFloat(), bottomLeft.y.toFloat(), vertexSize, paint)
        canvas.drawCircle(bottomRight.x.toFloat(), bottomRight.y.toFloat(), vertexSize, paint)
    }

    private fun drawEdge(canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.WHITE
        paint.strokeWidth = 3F
        paint.isAntiAlias = true

        canvas.drawLine(topLeft.x.toFloat(), topLeft.y.toFloat(),
            topRight.x.toFloat(), topRight.y.toFloat(), paint)
        canvas.drawLine(topLeft.x.toFloat(), topLeft.y.toFloat(),
            bottomLeft.x.toFloat(), bottomLeft.y.toFloat(), paint)
        canvas.drawLine(bottomRight.x.toFloat(), bottomRight.y.toFloat(),
            topRight.x.toFloat(), topRight.y.toFloat(), paint)
        canvas.drawLine(bottomRight.x.toFloat(), bottomRight.y.toFloat(),
            bottomLeft.x.toFloat(), bottomLeft.y.toFloat(), paint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> parent.requestDisallowInterceptTouchEvent(false)
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(false)
                onActionDown(event)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                parent.requestDisallowInterceptTouchEvent(true)
                onActionMove(event)
                return true
            }
        }
        return false
    }

    private fun onActionDown(event: MotionEvent) {
        touchDownX = event.x
        touchDownY = event.y
        val touchPoint = Point(event.x.toInt(), event.y.toInt())
        var minDistance = distance(touchPoint, topLeft)
        cropPosition = CropPosition.TOP_LEFT
        if (minDistance > distance(touchPoint, topRight)) {
            minDistance = distance(touchPoint, topRight)
            cropPosition = CropPosition.TOP_RIGHT
        }
        if (minDistance > distance(touchPoint, bottomLeft)) {
            minDistance = distance(touchPoint, bottomLeft)
            cropPosition = CropPosition.BOTTOM_LEFT
        }
        if (minDistance > distance(touchPoint, bottomRight)) {
            cropPosition = CropPosition.BOTTOM_RIGHT
        }
    }

    private fun distance(src: Point, dst: Point): Int {
        return sqrt((src.x - dst.x).toDouble().pow(2.0) + (src.y - dst.y).toDouble().pow(2.0)).toInt()
    }

    private fun onActionMove(event: MotionEvent) {
        val deltaX = (event.x - touchDownX).toInt()
        val deltaY = (event.y - touchDownY).toInt()

        when(cropPosition) {
            CropPosition.TOP_LEFT -> {
                adjustTopLeft(deltaX, deltaY)
                invalidate()
            }
            CropPosition.TOP_RIGHT -> {
                adjustTopRight(deltaX, deltaY)
                invalidate()
            }
            CropPosition.BOTTOM_LEFT -> {
                adjustBottomLeft(deltaX, deltaY)
                invalidate()
            }
            CropPosition.BOTTOM_RIGHT -> {
                adjustBottomRight(deltaX, deltaY)
                invalidate()
            }
        }
        touchDownX = event.x
        touchDownY = event.y
    }

    private fun adjustTopLeft(deltaX: Int, deltaY: Int) {
        var newX = topLeft.x + deltaX

        if (newX < minX) newX = minX
        if (newX > maxX) newX = maxX

        var newY = topLeft.y + deltaY
        if (newY < minY) newY = minY
        if (newY > maxY) newY = maxY

        topLeft.set(newX, newY)
    }

    private fun adjustTopRight(deltaX: Int, deltaY: Int) {
        var newX = topRight.x + deltaX

        if (newX < minX) newX = minX
        if (newX > maxX) newX = maxX

        var newY = topRight.y + deltaY
        if (newY < minY) newY = minY
        if (newY > maxY) newY = maxY

        topRight.set(newX, newY)
    }

    private fun adjustBottomLeft(deltaX: Int, deltaY: Int) {
        var newX = bottomLeft.x + deltaX

        if (newX < minX) newX = minX
        if (newX > maxX) newX = maxX

        var newY = bottomLeft.y + deltaY
        if (newY < minY) newY = minY
        if (newY > maxY) newY = maxY

        bottomLeft.set(newX, newY)
    }

    private fun adjustBottomRight(deltaX: Int, deltaY: Int) {
        var newX = bottomRight.x + deltaX

        if (newX < minX) newX = minX
        if (newX > maxX) newX = maxX

        var newY = bottomRight.y + deltaY
        if (newY < minY) newY = minY
        if (newY > maxY) newY = maxY

        bottomRight.set(newX, newY)
    }

    fun crop(cropListener: CropListener, needStretch: Boolean) {
        if (_bitmap == null) {
            return
        }
        val scaleX = _bitmap!!.width.toFloat() / width
        val scaleY = _bitmap!!.height.toFloat() / height
        val maxScale = scaleX.coerceAtLeast(scaleY)

        val bitmapTopLeft = Point(((topLeft.x - minX) * maxScale).toInt(),
            ((topLeft.y - minY) * maxScale).toInt())
        val bitmapTopRight = Point(((topRight.x - minX) * maxScale).toInt(),
            ((topRight.y - minY) * maxScale).toInt())
        val bitmapBottomLeft = Point(((bottomLeft.x - minX) * maxScale).toInt(),
            ((bottomLeft.y - minY) * maxScale).toInt())
        val bitmapBottomRight = Point(((bottomRight.x - minX) * maxScale).toInt(),
            ((bottomRight.y - minY) * maxScale).toInt())

        val output = Bitmap.createBitmap(_bitmap!!.width + 1, _bitmap!!.height + 1,
            Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()
        // 1. draw path
        val path = Path()
        path.moveTo(bitmapTopLeft.x.toFloat(), bitmapTopLeft.y.toFloat())
        path.lineTo(bitmapTopRight.x.toFloat(), bitmapTopRight.y.toFloat())
        path.lineTo(bitmapBottomRight.x.toFloat(), bitmapBottomRight.y.toFloat())
        path.lineTo(bitmapBottomLeft.x.toFloat(), bitmapBottomLeft.y.toFloat())
        path.close()
        canvas.drawPath(path, paint)

        // 2. draw original bitmap
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(_bitmap!!, 0F, 0F, paint)

        // 3. cut
        val cropRect = Rect(
            bitmapTopLeft.x.coerceAtMost(bitmapBottomLeft.x),
            bitmapTopLeft.y.coerceAtMost(bitmapTopRight.y),
            bitmapBottomRight.x.coerceAtLeast(bitmapTopRight.x),
            bitmapBottomRight.y.coerceAtLeast(bitmapBottomLeft.y)
        )
        if (cropRect.width() <= 0 || cropRect.height() <= 0) {
            cropListener.onFinish(null)
            return
        }
        val cut = Bitmap.createBitmap(output, cropRect.left, cropRect.top,
            cropRect.width(), cropRect.height())
        if (needStretch) {
            // 4. re-calculate coordinate in cropRect
            val cutTopLeft = Point()
            val cutTopRight = Point()
            val cutBottomLeft = Point()
            val cutBottomRight = Point()

            cutTopLeft.x = if (bitmapTopLeft.x > bitmapBottomLeft.x)
                bitmapTopLeft.x - bitmapBottomLeft.x else 0
            cutTopLeft.y = if (bitmapTopLeft.y > bitmapTopRight.y)
                bitmapTopLeft.y - bitmapTopRight.y else 0

            cutTopRight.x = if (bitmapTopRight.x > bitmapBottomRight.x) cropRect.width()
                else cropRect.width() - abs(bitmapBottomRight.x - bitmapTopRight.x)
            cutTopRight.y = if (bitmapTopLeft.y > bitmapTopRight.y)
                0 else abs(bitmapTopLeft.y - bitmapTopRight.y)

            cutBottomLeft.x = if (bitmapTopLeft.x > bitmapBottomLeft.x)
                0 else abs(bitmapTopLeft.x - bitmapBottomLeft.x)
            cutBottomLeft.y = if (bitmapBottomLeft.y > bitmapBottomRight.y) cropRect.height()
                else cropRect.height() - abs(bitmapBottomRight.y - bitmapBottomLeft.y)

            cutBottomRight.x = if (bitmapTopRight.x > bitmapBottomRight.x)
                cropRect.width() - abs(bitmapBottomRight.x - bitmapTopRight.x)
                else cropRect.width()
            cutBottomRight.y = if (bitmapBottomLeft.y > bitmapBottomRight.y)
                cropRect.height() - abs(bitmapBottomRight.y - bitmapBottomLeft.y)
                else cropRect.height()

            val width = cut.width.toFloat()
            val height = cut.height.toFloat()

            val src = floatArrayOf(
                cutTopLeft.x.toFloat(),
                cutTopLeft.y.toFloat(),
                cutTopRight.x.toFloat(),
                cutTopRight.y.toFloat(),
                cutBottomRight.x.toFloat(),
                cutBottomRight.y.toFloat(),
                cutBottomLeft.x.toFloat(),
                cutBottomLeft.y.toFloat()
            )
            val dst = floatArrayOf(0f, 0f, width, 0f, width, height, 0f, height)

            val matrix = Matrix()
            matrix.setPolyToPoly(src, 0, dst, 0, 4)
            val stretch = Bitmap.createBitmap(cut.width, cut.height, Bitmap.Config.ARGB_8888)

            val stretchCanvas = Canvas(stretch)
            stretchCanvas.concat(matrix)
            stretchCanvas.drawBitmapMesh(cut, WIDTH_BLOCK, HEIGHT_BLOCK,
                generateVertices(cut.width, cut.height),
                0, null, 0, null)
            cropListener.onFinish(stretch)
        } else {
            cropListener.onFinish(cut)
        }
    }

    private fun generateVertices(widthBitmap: Int, heightBitmap: Int) : FloatArray {
        val vertices = FloatArray((WIDTH_BLOCK + 1) * (HEIGHT_BLOCK + 1) * 2)
        val widthBlock = widthBitmap.toFloat() / WIDTH_BLOCK
        val heightBlock = heightBitmap.toFloat() / HEIGHT_BLOCK

        for (i in 0..HEIGHT_BLOCK) {
            for (j in 0..WIDTH_BLOCK) {
                vertices[i * ((HEIGHT_BLOCK + 1) * 2) + (j * 2)] = j * widthBlock
                vertices[i * ((HEIGHT_BLOCK + 1) * 2) + (j * 2) + 1] = i * heightBlock
            }
        }
        return vertices
    }
}