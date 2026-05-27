package com.example.mobile_tcc

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

object Mascaras {

    class TelefoneVisualTransformation : VisualTransformation {
        override fun filter(text: AnnotatedString): TransformedText {
            // Pega apenas os numeros digitados
            val digits = text.text.filter { it.isDigit() }
            var formatado = ""

            for (i in digits.indices) {
                if (i == 0) formatado += "("
                formatado += digits[i]
                if (i == 1) formatado += ") "
                if (i == 6) formatado += "-"
            }

            // funcao OffsetMapping para o cursor saber ele deve pular quando encontra (, ) ou -
            val offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    if (offset <= 0) return offset
                    if (offset <= 2) return offset + 1
                    if (offset <= 6) return offset + 3
                    if (offset <= 11) return offset + 4
                    return 15
                }

                override fun transformedToOriginal(offset: Int): Int {
                    if (offset <= 1) return 0
                    if (offset <= 3) return offset - 1
                    if (offset <= 5) return 2
                    if (offset <= 10) return offset - 3
                    if (offset <= 11) return 6
                    if (offset <= 15) return offset - 4
                    return 11
                }
            }

            return TransformedText(AnnotatedString(formatado), offsetMapping)
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