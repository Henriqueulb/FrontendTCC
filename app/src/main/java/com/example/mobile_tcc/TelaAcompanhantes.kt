package com.example.mobile_tcc

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
                        // Opcional: Auto-selecionar o primeiro paciente da lista
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

    LaunchedEffect(Unit) { carregarDados() }

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
                    carregarDados() // Recarrega a lista para mostrar o novo paciente
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

    // Funcao para o acompanhante parar de seguir o paciente
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Acompanhantes", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D47A1))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (carregando && codigoAtual == "Gerando...") {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (isAcompanhante) {
                // VISAO DO ACOMPANHANTE
                Text("Vincular a um Novo Paciente", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = codigoInput,
                        onValueChange = { codigoInput = it.uppercase() },
                        label = { Text("Código do Paciente") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { vincularPaciente() },
                        modifier = Modifier.height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                        enabled = !carregando
                    ) {
                        Text("VINCULAR")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                Text("Pacientes Acompanhados", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Selecione um paciente para gerenciar a rotina dele:", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))

                if (listaPacientes.isEmpty()) {
                    Text("Você ainda não está acompanhando nenhum paciente.", color = Color.Gray)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listaPacientes) { paciente ->
                            val isSelected = paciente.idVinculo == pacienteSelecionadoId

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
                                ),
                                border = if (isSelected) BorderStroke(2.dp, Color(0xFF0D47A1)) else null,
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { pacienteSelecionadoId = paciente.idVinculo } // Alterna o paciente ativo
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(paciente.nomePaciente, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            if (isSelected) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(Icons.Default.CheckCircle, contentDescription = "Ativo", tint = Color(0xFF0D47A1), modifier = Modifier.size(18.dp))
                                            }
                                        }
                                        Text(paciente.emailPaciente, fontSize = 14.sp, color = Color.Gray)
                                    }
                                    IconButton(onClick = { pararDeAcompanhar(paciente.idVinculo) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remover", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }

            } else {
                // VISÃO DO PACIENTE
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Seu Código de Convite", fontSize = 16.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(codigoAtual, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1), letterSpacing = 2.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Compartilhe este código para que alguém acompanhe sua rotina.", fontSize = 12.sp, color = Color.DarkGray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                Text("Acompanhantes Ativos", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
                Spacer(modifier = Modifier.height(8.dp))

                if (listaAcompanhantes.isEmpty()) {
                    Text("Nenhum acompanhante vinculado no momento.", color = Color.Gray)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listaAcompanhantes) { acompanhante ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(acompanhante.nomeAcompanhante, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(acompanhante.emailAcompanhante, fontSize = 14.sp, color = Color.Gray)
                                    }
                                    IconButton(onClick = { revogarAcessoAcompanhante(acompanhante.idVinculo) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Revogar Acesso", tint = Color.Red)
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