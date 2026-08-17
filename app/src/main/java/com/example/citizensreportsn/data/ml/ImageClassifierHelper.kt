package com.example.citizensreportsn.data.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer

class ImageClassifierHelper(context: Context) {

    private var interpreter: Interpreter? = null
    private var labels = listOf<String>()
    private var inputSize = 224
    private var inputDataType = DataType.UINT8
    private var outputDataType = DataType.FLOAT32
    private var numClasses = 0

    init {
        try {
            val modelBuffer: MappedByteBuffer = FileUtil.loadMappedFile(context, "model.tflite")
            interpreter = Interpreter(modelBuffer)

            labels = try {
                FileUtil.loadLabels(context, "labels.txt").map { it.replace(Regex("^\\d+\\s+"), "").trim() }
            } catch (e: Exception) {
                listOf("Routes", "Éclairage", "Déchets", "Transports", "Sécurité")
            }

            val inputTensor = interpreter?.getInputTensor(0)
            if (inputTensor != null) {
                inputSize = inputTensor.shape()[1]
                inputDataType = inputTensor.dataType()
            }

            val outputTensor = interpreter?.getOutputTensor(0)
            if (outputTensor != null) {
                outputDataType = outputTensor.dataType()
                val shape = outputTensor.shape()
                numClasses = shape[shape.size - 1]
            }
            
            Log.d("IA_DEBUG", "IA prête. Taille: $inputSize, Classes: $numClasses")
        } catch (e: Exception) {
            Log.e("IA_DEBUG", "Erreur init: ${e.message}")
        }
    }

    fun classify(bitmap: Bitmap): String {
        val interp = interpreter ?: return "IA non prête"

        return try {
            //  Redimensionner
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
            
            //  Entrée
            val bytesPerInput = if (inputDataType == DataType.UINT8) 1 else 4
            val inputBuffer = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * bytesPerInput)
            inputBuffer.order(ByteOrder.nativeOrder())
            
            val intValues = IntArray(inputSize * inputSize)
            scaledBitmap.getPixels(intValues, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

            for (pixelValue in intValues) {
                val r = (pixelValue shr 16 and 0xFF)
                val g = (pixelValue shr 8 and 0xFF)
                val b = (pixelValue and 0xFF)

                if (inputDataType == DataType.UINT8) {
                    inputBuffer.put(r.toByte())
                    inputBuffer.put(g.toByte())
                    inputBuffer.put(b.toByte())
                } else {
                    inputBuffer.putFloat(r / 255.0f)
                    inputBuffer.putFloat(g / 255.0f)
                    inputBuffer.putFloat(b / 255.0f)
                }
            }

            // Sortie
            val bytesPerOutput = if (outputDataType == DataType.UINT8) 1 else 4
            val outputBuffer = ByteBuffer.allocateDirect(numClasses * bytesPerOutput)
            outputBuffer.order(ByteOrder.nativeOrder())

            // EXÉCUTION
            interp.run(inputBuffer, outputBuffer)

            // Analyse
            outputBuffer.rewind()
            val probabilities = FloatArray(numClasses)
            
            if (outputDataType == DataType.UINT8) {
                for (i in 0 until numClasses) {
                    probabilities[i] = (outputBuffer.get().toInt() and 0xFF) / 255.0f
                }
            } else {
                outputBuffer.asFloatBuffer().get(probabilities)
            }

            var maxIdx = -1
            var maxVal = -1f
            for (i in probabilities.indices) {
                if (probabilities[i] > maxVal) {
                    maxVal = probabilities[i]
                    maxIdx = i
                }
            }

            if (maxIdx != -1 && maxVal > 0.05f) {
                val score = (maxVal * 100).toInt()
                val name = labels.getOrElse(maxIdx) { "ID #$maxIdx" }
                "$name ($score%)"
            } else {
                "Incertain"
            }
        } catch (e: Exception) {
            Log.e("IA_DEBUG", "Erreur classification: ${e.message}")
            "Erreur: ${e.message?.take(20)}"
        }
    }
}