package com.example.amls.ml

data class FragmentoSubtitulo(
    val texto: String,
    val inicioMs: Long,
    val finMs: Long
)

/**
 * Convierte el contenido crudo de un archivo .srt en una lista de
 * fragmentos con sus tiempos, para sincronizar el resaltado de la
 * transcripción con la posición actual del video.
 */
fun parsearSrt(contenido: String): List<FragmentoSubtitulo> {
    val bloques = contenido.trim().split(Regex("\n\\s*\n"))
    val fragmentos = mutableListOf<FragmentoSubtitulo>()

    val patronTiempo = Regex("""(\d{2}):(\d{2}):(\d{2}),(\d{3})\s*-->\s*(\d{2}):(\d{2}):(\d{2}),(\d{3})""")

    for (bloque in bloques) {
        val lineas = bloque.trim().split("\n")
        if (lineas.size < 3) continue

        val coincidencia = patronTiempo.find(lineas[1]) ?: continue
        val (h1, m1, s1, ms1, h2, m2, s2, ms2) = coincidencia.destructured

        val inicioMs = h1.toLong() * 3600000 + m1.toLong() * 60000 + s1.toLong() * 1000 + ms1.toLong()
        val finMs = h2.toLong() * 3600000 + m2.toLong() * 60000 + s2.toLong() * 1000 + ms2.toLong()
        val texto = lineas.drop(2).joinToString(" ")

        fragmentos.add(FragmentoSubtitulo(texto, inicioMs, finMs))
    }

    return fragmentos
}
