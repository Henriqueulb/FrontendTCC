package com.example.mobile_tcc

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.mobile_tcc.NovoSintomaDTO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaRegistroSintomas(navController: NavController, emailUsuario: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sharedPrefs = remember { context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }
    val emailLogado = remember { sharedPrefs.getString("emailLogado", "") ?: "" }

    var bemEstar by remember { mutableStateOf(5f) }
    var nivelSintomas by remember { mutableStateOf(5f) }
    var listaSintomas by remember { mutableStateOf(listOf<DetalheSintomaDTO>()) }
    var nomeNovoSintoma by remember { mutableStateOf("") }
    var intensidadeNova by remember { mutableStateOf(5f) }
    var enviando by remember { mutableStateOf(false) }

    fun salvarSintomas() {
        scope.launch {
            enviando = true
            try {
                val request = NovoSintomaDTO(
                    emailUsuario = emailUsuario,
                    bemEstar = bemEstar.toInt(),
                    sintomas = listaSintomas
                )
                val emailRegistro = emailLogado.ifEmpty { emailUsuario }
                val response = RetrofitClient.api.registrarSintoma(emailRegistro, request)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Registro salvo com sucesso!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                } else {
                    Toast.makeText(context, "Erro ao salvar registro", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro de conexão", Toast.LENGTH_SHORT).show()
            } finally {
                enviando = false
            }
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Registrar Sintomas", color = Primary, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = OnSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Como você está se sentindo hoje?",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface,
                lineHeight = 28.sp
            )
            Text(
                "Registre seu bem-estar e intensidade de sintomas para acompanhamento médico.",
                color = OnSurfaceVariant,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Card Bem-Estar
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, OutlineVariant),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Nível de Bem-Estar", fontWeight = FontWeight.Bold, color = Primary)
                        Text("${bemEstar.toInt()}/10", fontWeight = FontWeight.Bold, color = Primary)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Slider(
                        value = bemEstar,
                        onValueChange = { bemEstar = it },
                        valueRange = 0f..10f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = Primary,
                            activeTrackColor = Primary,
                            inactiveTrackColor = OutlineVariant.copy(alpha = 0.3f)
                        )
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Péssimo", fontSize = 12.sp, color = OnSurfaceVariant)
                        Text("Excelente", fontSize = 12.sp, color = OnSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Adicionar Sintomas
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, OutlineVariant),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Adicionar Sintoma", fontWeight = FontWeight.Bold, color = Primary)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = nomeNovoSintoma,
                        onValueChange = { nomeNovoSintoma = it },
                        label = { Text("Nome do sintoma") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = OutlineVariant,
                            focusedLabelColor = Primary
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Intensidade: ${intensidadeNova.toInt()}/10", fontSize = 14.sp, color = OnSurfaceVariant)
                    Slider(
                        value = intensidadeNova,
                        onValueChange = { intensidadeNova = it },
                        valueRange = 1f..10f,
                        steps = 9
                    )

                    Button(
                        onClick = {
                            if (nomeNovoSintoma.isNotEmpty()) {
                                listaSintomas = listaSintomas + DetalheSintomaDTO(nomeNovoSintoma, intensidadeNova.toInt())
                                nomeNovoSintoma = ""
                                intensidadeNova = 5f
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Adicionar à Lista")
                    }

                    if (listaSintomas.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))

                        listaSintomas.forEach { sintoma ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(sintoma.nome, fontWeight = FontWeight.Bold)
                                        Text("Intensidade: ${sintoma.intensidade}/10", fontSize = 12.sp)
                                    }
                                    IconButton(onClick = { listaSintomas = listaSintomas - sintoma }) {
                                        Text("X", color = Color.Red, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { salvarSintomas() },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = !enviando
            ) {
                if (enviando) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("SALVAR REGISTRO", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
