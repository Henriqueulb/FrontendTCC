package com.example.mobile_tcc

import kotlinx.serialization.Serializable

@Serializable
data class RelatorioCompletoDTO(
    val mediaBemEstar: Double,
    val taxaAdesaoGlobal: Double,
    val evolucaoTemporal: List<PontoEvolucaoDTO>,
    val sintomasFrequentes: List<SintomaFrequenteDTO>,
    // Novos campos para o PDF completo
    var nomePaciente: String? = null,
    var fichaMedica: FichaMedicaDTO? = null,
    var acompanhantes: List<AcompanhanteDTO>? = null,
    var itensRotina: List<ItemRotinaDTO>? = null
)

@Serializable
data class PontoEvolucaoDTO(
    val data: String,
    val dosesTomadas: Int,
    val dosesEsquecidas: Int
)

@Serializable
data class SintomaFrequenteDTO(
    val nome: String,
    val contagem: Int
)
