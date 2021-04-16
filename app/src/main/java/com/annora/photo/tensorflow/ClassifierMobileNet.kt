package com.annora.photo.tensorflow

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import com.annora.photo.common.BaseApp
import com.annora.photo.common.TEST_TAG
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import kotlin.jvm.Throws

class ClassifierMobileNet {
    companion object {
        const val IMG_SIZE = 224

        private const val TAG = "Classifier"

        private const val MODEL_NAME = "converted_model.tflite"
        private const val LABEL_NAME = "model_labels.txt"

        private const val NUM_PRE_CHANNEL = 4

        private const val DIM_BATCH_SIZE = 1
        private const val DIM_PIXEL_SIZE = 3

        private val tfliteOptions = Interpreter.Options()
    }

    private var tflite: Interpreter? = null
    private var imgData: ByteBuffer
    private val intValues = IntArray(IMG_SIZE * IMG_SIZE)
    private val labelProbArray: Array<FloatArray> by lazy {
        Array(1) { FloatArray(modelLabelList.size) }
    }

    private val modelFile by lazy(LazyThreadSafetyMode.NONE) { loadModelFile(BaseApp.context) }
    private val modelLabelList by lazy(LazyThreadSafetyMode.NONE) { loadLabelList(BaseApp.context) }

    init {
        tflite = Interpreter(modelFile, tfliteOptions)
        imgData = ByteBuffer.allocateDirect(
            DIM_BATCH_SIZE
                    * IMG_SIZE
                    * IMG_SIZE
                    * DIM_PIXEL_SIZE
                    * NUM_PRE_CHANNEL
        )
        imgData.order(ByteOrder.nativeOrder())
    }

    @Throws(IOException::class)
    private fun loadLabelList(context: Context): List<String> {
        val labelList: MutableList<String> =
            ArrayList()
        val reader = BufferedReader(
            InputStreamReader(
                context.assets.open(LABEL_NAME)
            )
        )
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            line?.let { labelList.add(it) }
        }
        reader.close()
        return labelList
    }

    @Throws(IOException::class)
    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(MODEL_NAME)
        val inputStream =
            FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            startOffset,
            declaredLength
        )
    }

    fun classifyFrame(bitmap: Bitmap?): Array<String>? {
        if (tflite == null) {
            Log.e(TAG, "Image classifier has not been initialized; Skipped.")
            return null
        }
        convertBitmapToByteBuffer(bitmap!!)
        // Here's where the magic happens!!!
        val startTime = SystemClock.uptimeMillis()
        tflite?.run(imgData, labelProbArray)
        val endTime = SystemClock.uptimeMillis()
        Log.d(TAG, "Timecost to run model inference: " + (endTime - startTime))

        return printLabel()
    }

    private fun printLabel(): Array<String>? {
        var max = 0f
        var maxIndex = -1
        for (i in modelLabelList.indices) {
            val per = labelProbArray[0][i]
            Log.d(TEST_TAG, "label : ${modelLabelList[i]} | ${labelProbArray[0][i]}")
            if (max < per) {
                max = per
                maxIndex = i
            }
        }
        return if (maxIndex == -1) null else arrayOf(
            modelLabelList[maxIndex],
            labelProbArray[0][maxIndex].toString()
        )
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap) {
        imgData.rewind()
        bitmap.getPixels(
            intValues,
            0,
            bitmap.width,
            0,
            0,
            bitmap.width,
            bitmap.height
        )
        // Convert the image to floating point.
        var pixel = 0
        for (i in 0 until IMG_SIZE) {
            for (j in 0 until IMG_SIZE) {
                val pixelValue: Int = intValues[pixel++]
                imgData.putFloat(((pixelValue shr 16 and 0xFF) / 255f - 0.5f) * 2.0f)
                imgData.putFloat(((pixelValue shr 8 and 0xFF) / 255f - 0.5f) * 2.0f)
                imgData.putFloat(((pixelValue and 0xFF) / 255f - 0.5f) * 2.0f)
            }
        }
    }

    fun close() {
        tflite?.close()
        tflite = null
    }

}