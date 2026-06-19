package com.example.mobile_tcc

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MedicalServices
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
import com.example.mobile_tcc.ui.theme.* // Importando as cores do seu tema
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDadosMedicos(navController: NavController, emailUsuario: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados
    var alergias by remember { mutableStateOf("") }
    var medicacoes by remember { mutableStateOf("") }
    var comorbidades by remember { mutableStateOf("") }
    var carregando by remember { mutableStateOf(true) }

    // Carregar dados iniciais
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = RetrofitClient.api.getFichaMedica(emailUsuario)
                if (response.isSuccessful) {
                    val dados = response.body()
                    if (dados != null) {
                        alergias = dados.alergias
                        medicacoes = dados.medicacoes
                        comorbidades = dados.comorbidades
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Sem conexão", Toast.LENGTH_SHORT).show()
            } finally {
                carregando = false
            }
        }
    }

    fun salvarDados() {
        scope.launch {
            carregando = true
            try {
                val dto = FichaMedicaDTO(
                    emailUsuario = emailUsuario,
                    alergias = alergias,
                    medicacoes = medicacoes,
                    comorbidades = comorbidades
                )

                val response = RetrofitClient.api.salvarFichaMedica(dto)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Ficha atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                } else {
                    Toast.makeText(context, "Erro ao salvar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro de conexão", Toast.LENGTH_SHORT).show()
            } finally {
                carregando = false
            }
        }
    }

    // Estilo padronizado para os campos de texto
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Primary,
        unfocusedBorderColor = OutlineVariant,
        focusedLabelColor = Primary,
        unfocusedLabelColor = OnSurfaceVariant,
        cursorColor = Primary
    )
    val textFieldShape = RoundedCornerShape(12.dp)

    Scaffold(
        containerColor = Background, // Fundo padronizado
        topBar = {
            TopAppBar(
                title = { Text("Dados Médicos", color = Primary, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = OnSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { paddingValues ->
        if (carregando && alergias.isEmpty() && comorbidades.isEmpty() && medicacoes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()), // Permite rolar a tela se o teclado abrir
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Preencha as informações clínicas importantes para casos de emergência ou consulta.",
                    color = OnSurfaceVariant,
                    fontSize = 16.sp
                )

                // Alergias
                OutlinedTextField(
                    value = alergias,
                    onValueChange = { alergias = it },
                    label = { Text("Alergias") },
                    placeholder = { Text("Ex: Dipirona, Iodo, Frutos do mar...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 5,
                    shape = textFieldShape,
                    colors = textFieldColors
                )

                // Comorbidades
                OutlinedTextField(
                    value = comorbidades,
                    onValueChange = { comorbidades = it },
                    label = { Text("Comorbidades / Doenças Pré-existentes") },
                    placeholder = { Text("Ex: Diabetes Tipo 2, Hipertensão, Asma...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 5,
                    shape = textFieldShape,
                    colors = textFieldColors
                )

                // Medicações Contínuas
                OutlinedTextField(
                    value = medicacoes,
                    onValueChange = { medicacoes = it },
                    label = { Text("Medicações de Uso Contínuo") },
                    placeholder = { Text("Ex: Losartana 50mg (Manhã)...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 5,
                    shape = textFieldShape,
                    colors = textFieldColors
                )

                Spacer(modifier = Modifier.weight(1f, fill = false))
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { salvarDados() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp), // Altura aprimorada
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        disabledContainerColor = Primary.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !carregando
                ) {
                    if (carregando) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SALVAR DADOS MÉDICOS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                // Respiro no fundo da tela
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}