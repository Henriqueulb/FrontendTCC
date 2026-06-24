package com.example.mobile_tcc

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobile_tcc.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaAcompanhantes(navController: NavController, emailUsuario: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isAcompanhante by remember { mutableStateOf(false) }
    var carregando by remember { mutableStateOf(true) }

    // Estados do Paciente
    var codigoAtual by remember { mutableStateOf("Gerando...") }
    var listaAcompanhantes by remember { mutableStateOf<List<AcompanhanteDTO>>(emptyList()) }

    // Estados do Acompanhante
    var codigoInput by remember { mutableStateOf("") }
    var listaPacientes by remember { mutableStateOf<List<PacienteVinculadoDTO>>(emptyList()) }
    var pacienteSelecionadoId by remember { mutableStateOf<Int?>(null) }

    // === FUNÇÕES DE LÓGICA (RETROFIT) - REVISADAS E CORRIGIDAS ===
    fun carregarDados() {
        scope.launch {
            try {
                // 1. Descobre se o usuario logado e Paciente ou Acompanhante
                val responsePerfil = RetrofitClient.api.getPerfil(emailUsuario)
                if (responsePerfil.isSuccessful) {
                    isAcompanhante = responsePerfil.body()?.isAcompanhante ?: false
                }

                // 2. Carrega dados baseados no tipo de perfil
                if (!isAcompanhante) {
                    // SE FOR PACIENTE: Busca codigo e quem o acompanha
                    val responseCodigo = RetrofitClient.api.gerarCodigoConvite(emailUsuario)
                    if (responseCodigo.isSuccessful) {
                        codigoAtual = responseCodigo.body()?.codigo ?: "Erro ao gerar"
                    }

                    val responseLista = RetrofitClient.api.listarAcompanhantes(emailUsuario)
                    if (responseLista.isSuccessful) {
                        listaAcompanhantes = responseLista.body() ?: emptyList()
                    }
                } else {
                    // SE FOR ACOMPANHANTE: Busca os pacientes que ele acompanha
                    val responsePacientes = RetrofitClient.api.listarPacientesDoAcompanhante(emailUsuario)
                    if (responsePacientes.isSuccessful) {
                        listaPacientes = responsePacientes.body() ?: emptyList()
                        if (listaPacientes.isNotEmpty() && pacienteSelecionadoId == null) {
                            pacienteSelecionadoId = listaPacientes.first().idVinculo
                        }
                    }
                }
            } catch (e: Exception) {
                codigoAtual = "Erro de conexão"
                Toast.makeText(context, "Erro de conexão ao buscar dados", Toast.LENGTH_SHORT).show()
            } finally {
                carregando = false
            }
        }
    }

    fun vincularPaciente() {
        if (codigoInput.isBlank()) {
            Toast.makeText(context, "Digite o código do paciente", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            carregando = true
            try {
                val dto = VincularAcompanhanteDTO(emailUsuario, codigoInput)
                val response = RetrofitClient.api.vincularAcompanhante(dto)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Vinculado ao paciente com sucesso!", Toast.LENGTH_LONG).show()
                    codigoInput = ""
                    carregarDados() // Recarrega a lista
                } else {
                    Toast.makeText(context, "Código inválido ou já utilizado", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro de conexão", Toast.LENGTH_SHORT).show()
            } finally {
                carregando = false
            }
        }
    }

    fun revogarAcessoAcompanhante(idVinculo: Int) {
        scope.launch {
            try {
                val response = RetrofitClient.api.revogarAcompanhante(idVinculo)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Acesso revogado", Toast.LENGTH_SHORT).show()
                    listaAcompanhantes = listaAcompanhantes.filter { it.idVinculo != idVinculo }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao revogar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun pararDeAcompanhar(idVinculo: Int) {
        scope.launch {
            try {
                val response = RetrofitClient.api.revogarAcompanhante(idVinculo)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Você parou de acompanhar este paciente", Toast.LENGTH_SHORT).show()
                    listaPacientes = listaPacientes.filter { it.idVinculo != idVinculo }
                    if (pacienteSelecionadoId == idVinculo) pacienteSelecionadoId = null
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao remover vínculo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // === EFEITO COLATERAL (ATIVA O CARREGAMENTO) ===
    LaunchedEffect(Unit) {
        carregarDados()
    }

    // Estilos padronizados
    val textFieldColors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
    val textFieldShape = RoundedCornerShape(12.dp)

    // === UI PRINCIPAL (O QUE É EXIBIDO) ===
    Scaffold(
        containerColor = Background, // Fundo claro do tema
        topBar = {
            TopAppBar(
                title = { Text("Acompanhantes", color = Primary, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = OnSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            if (carregando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (isAcompanhante) {
                // VISAO DO ACOMPANHANTE (Vincular e Pacientes)
                Text("Vincular a um Novo Paciente", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Primary)
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = codigoInput,
                        onValueChange = { codigoInput = it.uppercase() },
                        label = { Text("Código do Paciente") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = textFieldShape,
                        colors = textFieldColors
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { vincularPaciente() },
                        modifier = Modifier.height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !carregando
                    ) {
                        Text("VINCULAR")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider(color = OutlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Pacientes Acompanhados", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Primary)
                Spacer(modifier = Modifier.height(16.dp))

                if (listaPacientes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                        Text("Você ainda não está acompanhando nenhum paciente.", color = OnSurfaceVariant)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                        items(listaPacientes) { paciente ->
                            val isSelected = paciente.idVinculo == pacienteSelecionadoId

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) PrimaryFixed else Color.White
                                ),
                                border = BorderStroke(1.dp, if (isSelected) Primary else OutlineVariant),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { pacienteSelecionadoId = paciente.idVinculo } // Alterna o paciente ativo
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Quadradinho do Ícone
                                    Box(
                                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(if (isSelected) Color.White else PrimaryFixed),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Person,
                                            contentDescription = null,
                                            tint = if (isSelected) Primary else Primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(paciente.nomePaciente, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnSurface)
                                        Text(paciente.emailPaciente, fontSize = 12.sp, color = OnSurfaceVariant)
                                    }
                                    IconButton(
                                        onClick = { pararDeAcompanhar(paciente.idVinculo) },
                                        modifier = Modifier.background(ErrorContainer, RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remover", tint = ErrorColor)
                                    }
                                }
                            }
                        }
                    }
                }

            } else {
                // VISÃO DO PACIENTE (Código e Acompanhantes)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Seu Código de Convite", fontSize = 14.sp, color = OnSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(codigoAtual, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Primary, letterSpacing = 2.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Compartilhe este código para que alguém acompanhe sua rotina.", fontSize = 12.sp, color = OnSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider(color = OutlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Acompanhantes Ativos", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Primary)
                Spacer(modifier = Modifier.height(16.dp))

                if (listaAcompanhantes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                        Text("Nenhum acompanhante vinculado no momento.", color = OnSurfaceVariant)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                        items(listaAcompanhantes) { acompanhante ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, OutlineVariant),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Quadradinho do Ícone
                                    Box(
                                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(PrimaryFixed),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Outlined.Group, contentDescription = null, tint = Primary)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(acompanhante.nomeAcompanhante, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnSurface)
                                        Text(acompanhante.emailAcompanhante, fontSize = 12.sp, color = OnSurfaceVariant)
                                    }
                                    IconButton(
                                        onClick = { revogarAcessoAcompanhante(acompanhante.idVinculo) },
                                        modifier = Modifier.background(ErrorContainer, RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Revogar Acesso", tint = ErrorColor)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}