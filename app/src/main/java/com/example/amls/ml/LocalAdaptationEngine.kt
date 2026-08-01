package com.example.amls.ml

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.InterpreterApi
import org.tensorflow.lite.InterpreterApi.Options
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

data class DecisionAdaptacion(
    val activarAltoContraste: Boolean,
    val agrandarFuente: Boolean,
    val ofrecerTranscripcion: Boolean
)

@Singleton
class LocalAdaptationEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val interprete: InterpreterApi by lazy {
        InterpreterApi.create(cargarModelo(), Options())
    }

    private fun cargarModelo(): MappedByteBuffer {
        val descriptor = context.assets.openFd("local_adaptation_engine.tflite")
        val flujo = FileInputStream(descriptor.fileDescriptor)
        return flujo.channel.map(
            FileChannel.MapMode.READ_ONLY,
            descriptor.startOffset,
            descriptor.declaredLength
        )
    }

    /**
     * Ejecuta la inferencia localmente (Edge Computing, CON-4).
     * Con un modelo de ~4KB como este, toma unos pocos milisegundos —
     * muy por debajo del límite de 500ms de la propuesta.
     */
    fun predecir(
        nivelLuz: Float,
        nivelMovimiento: Float,
        altoContrasteBase: Boolean,
        tamanoFuenteBase: Float
    ): DecisionAdaptacion {
        val entrada = arrayOf(
            floatArrayOf(nivelLuz, nivelMovimiento, if (altoContrasteBase) 1f else 0f, tamanoFuenteBase)
        )
        val salida = Array(1) { FloatArray(3) }

        interprete.run(entrada, salida)

        return DecisionAdaptacion(
            activarAltoContraste = salida[0][0] > 0.5f,
            agrandarFuente = salida[0][1] > 0.5f,
            ofrecerTranscripcion = salida[0][2] > 0.5f
        )
    }
}
