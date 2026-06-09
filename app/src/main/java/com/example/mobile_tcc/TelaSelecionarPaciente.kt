package com.example.mobile_tcc

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@Composable
fun TelaSelecionarPaciente(navController: NavController, emailAcompanhante: String) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }
    var pacientes by remember { mutableStateOf<List<PacienteVinculadoDTO>>(emptyList()) }
    var carregando by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Estados para o campo de vincular
    var codigoConvite by remember { mutableStateOf("") }
    var vinculando by remember { mutableStateOf(false) }

    fun carregarPacientes() {
        scope.launch {
            carregando = true
            try {
                val response = RetrofitClient.api.listarPacientesDoAcompanhante(emailAcompanhante)
                if (response.isSuccessful) {
                    pacientes = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao carregar lista", Toast.LENGTH_SHORT).show()
            } finally {
                carregando = false
            }
        }
    }

    // Vincular novo paciente
    fun vincularPaciente() {
        if (codigoConvite.isBlank()) {
            Toast.makeText(context, "Digite o código de convite", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            vinculando = true
            try {
                val request = VincularAcompanhanteDTO(emailAcompanhante, codigoConvite.trim())
                val response = RetrofitClient.api.vincularAcompanhante(request)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Paciente vinculado!", Toast.LENGTH_SHORT).show()
                    codigoConvite = ""
                    carregarPacientes() // Recarrega a lista
                } else {
                    Toast.makeText(context, "Código inválido ou já utilizado", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                vinculando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        carregarPacientes()
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Seus Pacientes", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
        Text("Selecione um paciente para gerenciar os cuidados.", color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Vincular Paciente", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = codigoConvite,
                    onValueChange = { codigoConvite = it },
                    label = { Text("Código de Convite") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { vincularPaciente() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !vinculando
                ) {
                    Text(if (vinculando) "Aguarde..." else "Vincular Conta")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // LISTAGEM DOS PACIENTES
        if (carregando) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (pacientes.isEmpty()) {
            Text("Nenhum paciente vinculado. Peça o código ao paciente e insira acima.", color = Color.Gray)
        } else {
            LazyColumn {
                items(pacientes) { paciente ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable {
                                sharedPrefs.edit().putString("emailPaciente", paciente.emailPaciente).apply()
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