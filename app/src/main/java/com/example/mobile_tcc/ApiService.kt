package com.example.mobile_tcc

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

// DTOs Gerais
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("senha") val senha: String
)

data class CadastroRequest(
    @SerializedName("nome") val nome: String,
    @SerializedName("email") val email: String,
    @SerializedName("senha") val senha: String,
    @SerializedName("telefone") val telefone: String,
    @SerializedName("isAcompanhante") val isAcompanhante: Boolean = false,
    @SerializedName("codigoConvite") val codigoConvite: String? = null
)

data class RespostaApi(
    @SerializedName("mensagem") val mensagem: String,
    @SerializedName("sucesso") val sucesso: Boolean,
    @SerializedName("nomeUsuario") val nomeUsuario: String? = null
)

// DTOs Rotina
data class RotinaResumoDTO(
    @SerializedName("idRotina") val idRotina: Int,
    @SerializedName("nomeRotina") val nomeRotina: String,
    @SerializedName("dataCriacao") val dataCriacao: String,
    @SerializedName("status") val status: String
)

data class CriarRotinaDTO(
    @SerializedName("emailUsuario") val emailUsuario: String,
    @SerializedName("nomeRotina") val nomeRotina: String
)

data class NovoItemRotinaDTO(
    @SerializedName("idRotina") val idRotina: Int,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("horario") val horario: String,
    @SerializedName("dose") val dose: String?,
    @SerializedName("descricao") val descricao: String?
)

data class ItemRotinaDTO(
    @SerializedName("id") val id: Int,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("horario") val horario: String,
    @SerializedName("dose") val dose: String?,
    @SerializedName("feita") var feita: Boolean
)

data class HomeResumoDTO(
    @SerializedName("progresso") val progresso: Float,
    @SerializedName("tarefas") val tarefas: List<ItemRotinaDTO>,
    @SerializedName("nomeUsuario") val nomeUsuario: String
)

data class StatusRotinaDTO(
    @SerializedName("idItem") val idItem: Int,
    @SerializedName("feito") val feito: Boolean,
    @SerializedName("data") val data: String
)

// DTOs Sintomas e Perfil
data class NovoSintomaDTO(
    @SerializedName("emailUsuario") val emailUsuario: String,
    @SerializedName("bemEstar") val bemEstar: Int,
    @SerializedName("sintomas") val sintomas: Int
)

data class PerfilUsuarioDTO(
    @SerializedName("nome") val nome: String,
    @SerializedName("email") val email: String,
    @SerializedName("telefone") val telefone: String
)

data class AtualizarPerfilDTO(
    @SerializedName("emailBusca") val emailBusca: String,
    @SerializedName("novoNome") val novoNome: String,
    @SerializedName("novoTelefone") val novoTelefone: String
)

data class TrocarSenhaDTO(
    @SerializedName("email") val email: String,
    @SerializedName("novaSenha") val novaSenha: String
)

data class FichaMedicaDTO(
    @SerializedName("emailUsuario") val emailUsuario: String,
    @SerializedName("alergias") val alergias: String,
    @SerializedName("medicacoes") val medicacoes: String,
    @SerializedName("comorbidades") val comorbidades: String
)

data class NotificacaoConfigDTO(
    @SerializedName("emailUsuario") val emailUsuario: String,
    @SerializedName("ativo") val ativo: Boolean,
    @SerializedName("som") val som: Boolean
)

data class AcompanhanteDTO(
    @SerializedName("idVinculo") val idVinculo: Int,
    @SerializedName("nomeAcompanhante") val nomeAcompanhante: String,
    @SerializedName("emailAcompanhante") val emailAcompanhante: String,
    @SerializedName("status") val status: String
)

data class CodigoConviteDTO(
    @SerializedName("codigo") val codigo: String
)

// Interface
interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<RespostaApi>

    @POST("cadastro")
    suspend fun cadastro(@Body request: CadastroRequest): Response<RespostaApi>

    @GET("rotinas")
    suspend fun listarRotinas(@Query("email") email: String): Response<List<RotinaResumoDTO>>

    @POST("rotinas")
    suspend fun criarNovaRotina(@Body request: CriarRotinaDTO): Response<RespostaApi>

    @PUT("rotinas/{id}/concluir")
    suspend fun concluirRotina(@Path("id") idRotina: Int): Response<RespostaApi>

    @GET("rotinas/{id}/itens")
    suspend fun listarItensDaRotina(@Path("id") idRotina: Int): Response<List<ItemRotinaDTO>>

    @POST("rotinas/itens")
    suspend fun criarItemRotina(@Body request: NovoItemRotinaDTO): Response<RespostaApi>

    @GET("home")
    suspend fun getHome(@Query("email") email: String): Response<HomeResumoDTO>

    @POST("rotina/status")
    suspend fun atualizarStatus(@Body status: StatusRotinaDTO): Response<RespostaApi>

    @DELETE("rotina/{id}")
    suspend fun deletarRotina(@Path("id") id: Int): Response<RespostaApi>

    @POST("sintomas")
    suspend fun registrarSintoma(@Body request: NovoSintomaDTO): Response<RespostaApi>

    @GET("perfil")
    suspend fun getPerfil(@Query("email") email: String): Response<PerfilUsuarioDTO>

    @PUT("perfil")
    suspend fun atualizarPerfil(@Body dados: AtualizarPerfilDTO): Response<RespostaApi>

    @PUT("usuario/senha")
    suspend fun trocarSenha(@Body dados: TrocarSenhaDTO): Response<RespostaApi>

    @DELETE("usuario")
    suspend fun deletarConta(@Query("email") email: String): Response<RespostaApi>

    @GET("ficha")
    suspend fun getFichaMedica(@Query("email") email: String): Response<FichaMedicaDTO>

    @POST("ficha")
    suspend fun salvarFichaMedica(@Body dados: FichaMedicaDTO): Response<RespostaApi>

    @GET("notificacao")
    suspend fun getConfigNotificacao(@Query("email") email: String): Response<NotificacaoConfigDTO>

    @POST("notificacao")
    suspend fun salvarConfigNotificacao(@Body config: NotificacaoConfigDTO): Response<RespostaApi>

    @GET("acompanhantes/codigo")
    suspend fun gerarCodigoConvite(@Query("email") email: String): Response<CodigoConviteDTO>

    @GET("acompanhantes")
    suspend fun listarAcompanhantes(@Query("email") email: String): Response<List<AcompanhanteDTO>>

    @DELETE("acompanhantes/{id}")
    suspend fun revogarAcompanhante(@Path("id") idVinculo: Int): Response<RespostaApi>
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}