package com.example.mobile_tcc

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.outlined.PersonAdd
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
import com.example.mobile_tcc.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun TelaSelecionarPaciente(navController: NavController, emailAcompanhante: String) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }
    var pacientes by remember { mutableStateOf<List<PacienteVinculadoDTO>>(emptyList()) }
    var carregando by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    var codigoConvite by remember { mutableStateOf("") }
    var vinculando by remember { mutableStateOf(false) }

    fun realizarLogout() {
        Toast.makeText(context, "Saindo...", Toast.LENGTH_SHORT).show()

        sharedPrefs.edit().clear().apply()

        navController.navigate("login") {
            popUpTo(0) { inclusive = true }
        }
    }

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
                    carregarPacientes()
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

    LaunchedEffect(Unit) { carregarPacientes() }

    Scaffold(containerColor = Background) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
            Text("Seus Pacientes", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Primary)
            Text("Selecione um paciente para gerenciar os cuidados.", color = OnSurfaceVariant)

            Spacer(modifier = Modifier.height(24.dp))

            // Card de Vinculação
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, OutlineVariant),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Vincular Paciente", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = codigoConvite,
                        onValueChange = { codigoConvite = it },
                        label = { Text("Código de Convite") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { vincularPaciente() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        enabled = !vinculando
                    ) {
                        Text(if (vinculando) "Aguarde..." else "Vincular Conta", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Lista de Pacientes
            if (carregando) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (pacientes.isEmpty()) {
                Text("Nenhum paciente vinculado. Peça o código ao paciente e insira acima.", color = OnSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(pacientes) { paciente ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, OutlineVariant),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    sharedPrefs.edit().putString("emailPaciente", paciente.emailPaciente).apply()
                                    navController.navigate("home/${paciente.emailPaciente}")
                                }
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.PersonAdd, contentDescription = null, tint = Primary)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(paciente.nomePaciente, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(paciente.emailPaciente, color = OnSurfaceVariant, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { realizarLogout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp), // Ajuste o padding conforme o padrão da sua tela
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp, // Icone padrao de "Sair"
                    contentDescription = "Ícone de Sair"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sair da conta")
            }
        }
    }
}