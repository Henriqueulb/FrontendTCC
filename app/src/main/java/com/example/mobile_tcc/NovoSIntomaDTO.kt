package com.example.mobile_tcc

import kotlinx.serialization.Serializable

@Serializable
data class DetalheSintomaDTO(
    val nome: String,
    val intensidade: Int
)

@Serializable
data class NovoSintomaDTO(
    val emailUsuario: String,
    val bemEstar: Int,
    val sintomas: List<DetalheSintomaDTO>
)