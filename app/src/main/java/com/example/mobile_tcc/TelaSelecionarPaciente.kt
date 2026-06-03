package com.example.mobile_tcc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun TelaSelecionarPaciente(navController: NavController, emailAcompanhante: String) {
    var pacientes by remember { mutableStateOf<List<PacienteVinculadoDTO>>(emptyList()) }
    var carregando by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = RetrofitClient.api.listarPacientesDoAcompanhante(emailAcompanhante)
                if (response.isSuccessful) {
                    pacientes = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // Tratar erro
            } finally {
                carregando = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Seus Pacientes", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
        Text("Selecione um paciente para gerenciar os cuidados.", color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        if (carregando) {
            CircularProgressIndicator()
        } else if (pacientes.isEmpty()) {
            Text("Nenhum paciente ativo encontrado.")
        } else {
            LazyColumn {
                items(pacientes) { paciente ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable {
                                navController.navigate("home/${paciente.emailPaciente}")
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(paciente.nomePaciente, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(paciente.emailPaciente, color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}