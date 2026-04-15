package com.example.mobile_tcc

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
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

    var codigoAtual by remember { mutableStateOf("Gerando...") }
    var listaAcompanhantes by remember { mutableStateOf<List<AcompanhanteDTO>>(emptyList()) }
    var carregando by remember { mutableStateOf(true) }

    fun carregarDados() {
        scope.launch {
            try {
                // Chama a API para gerar ou pegar o código ativo
                val responseCodigo = RetrofitClient.api.gerarCodigoConvite(emailUsuario)
                if (responseCodigo.isSuccessful) {
                    codigoAtual = responseCodigo.body()?.codigo ?: "Erro ao gerar"
                }

                // Carrega a lista de acompanhantes
                val responseLista = RetrofitClient.api.listarAcompanhantes(emailUsuario)
                if (responseLista.isSuccessful) {
                    listaAcompanhantes = responseLista.body() ?: emptyList()
                }
            } catch (e: Exception) {
                codigoAtual = "Erro de conexão"
                Toast.makeText(context, "Erro de conexão", Toast.LENGTH_SHORT).show()
            } finally {
                carregando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        carregarDados()
    }

    fun revogarAcesso(idVinculo: Int) {
        scope.launch {
            try {
                val response = RetrofitClient.api.revogarAcompanhante(idVinculo)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Acesso revogado", Toast.LENGTH_SHORT).show()
                    // Atualiza a lista localmente
                    listaAcompanhantes = listaAcompanhantes.filter { it.idVinculo != idVinculo }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao revogar", Toast.LENGTH_SHORT).show()
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
            // SEÇÃO: Código de Convite
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
                    if (carregando) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text(codigoAtual, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1), letterSpacing = 2.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Compartilhe este código para que alguém acompanhe sua rotina e sintomas.", fontSize = 12.sp, color = Color.DarkGray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // SEÇÃO: Lista de Acompanhantes
            Text("Acompanhantes Ativos", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
            Spacer(modifier = Modifier.height(8.dp))

            if (carregando) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (listaAcompanhantes.isEmpty()) {
                Text("Nenhum acompanhante vinculado à sua conta no momento.", color = Color.Gray)
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
                                IconButton(onClick = { revogarAcesso(acompanhante.idVinculo) }) {
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