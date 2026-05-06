package com.example.mobile_tcc

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

object Mascaras {

    fun formatarTelefone(texto: String): String {
        val numeros = texto.filter { it.isDigit() }
        val limitado = if (numeros.length > 11) numeros.substring(0, 11) else numeros

        return when {
            limitado.length > 10 -> {
                "(${limitado.substring(0, 2)}) ${limitado.substring(2, 7)}-${limitado.substring(7)}"
            }
            limitado.length > 6 -> {
                "(${limitado.substring(0, 2)}) ${limitado.substring(2, 6)}-${limitado.substring(6)}"
            }
            limitado.length > 2 -> {
                "(${limitado.substring(0, 2)}) ${limitado.substring(2)}"
            }
            else -> limitado
        }
    }

    fun formatarHora(texto: String): String {
        val numeros = texto.filter { it.isDigit() }
        val limitado = if (numeros.length > 4) numeros.substring(0, 4) else numeros

        return when {
            limitado.length >= 3 -> {
                "${limitado.substring(0, 2)}:${limitado.substring(2)}"
            }
            else -> limitado
        }
    }

    class HorarioVisualTransformation : VisualTransformation {
        override fun filter(text: AnnotatedString): TransformedText {
            // Pega apenas os números digitados
            val digits = text.text.filter { it.isDigit() }
            var formatado = ""

            for (i in digits.indices) {
                formatado += digits[i]
                if (i == 1) formatado += ":"
            }

            // validacao
            val offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    if (offset <= 1) return offset
                    if (offset <= 4) return offset + 1
                    return 5
                }
                override fun transformedToOriginal(offset: Int): Int {
                    if (offset <= 2) return offset
                    if (offset <= 5) return offset - 1
                    return 4
                }
            }
            return TransformedText(AnnotatedString(formatado), offsetMapping)
        }
    }
}