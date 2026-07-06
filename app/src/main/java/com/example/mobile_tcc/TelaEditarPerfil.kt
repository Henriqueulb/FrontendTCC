package com.example.mobile_tcc

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobile_tcc.ui.theme.* // Importando as cores do seu tema
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaEditarPerfil(navController: NavController, emailUsuario: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados
    var nome by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var carregando by remember { mutableStateOf(true) }

    // Carregar dados iniciais
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = RetrofitClient.api.getPerfil(emailUsuario)
                if (response.isSuccessful) {
                    val dados = response.body()
                    if (dados != null) {
                        nome = dados.nome
                        telefone = dados.telefone.filter { it.isDigit() }
                    }
                } else {
                    Toast.makeText(context, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Sem conexão", Toast.LENGTH_SHORT).show()
            } finally {
                carregando = false
            }
        }
    }

    fun salvarAlteracoes() {
        if (nome.isBlank()) {
            Toast.makeText(context, "Nome não pode ser vazio", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            carregando = true
            try {
                val dto = AtualizarPerfilDTO(
                    emailBusca = emailUsuario,
                    novoNome = nome,
                    novoTelefone = telefone
                )

                val response = RetrofitClient.api.atualizarPerfil(dto)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Perfil atualizado!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                } else {
                    Toast.makeText(context, "Erro ao atualizar", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Erro de conexão", Toast.LENGTH_SHORT).show()
            } finally {
                carregando = false
            }
        }
    }

    // Estilo para os campos de texto
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Primary,
        unfocusedBorderColor = OutlineVariant,
        focusedLabelColor = Primary,
        unfocusedLabelColor = OnSurfaceVariant,
        cursorColor = Primary,
        disabledBorderColor = OutlineVariant.copy(alpha = 0.5f),
        disabledLabelColor = OnSurfaceVariant.copy(alpha = 0.7f),
        disabledTextColor = OnSurfaceVariant.copy(alpha = 0.7f)
    )
    val textFieldShape = RoundedCornerShape(12.dp)

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Editar Dados", color = Primary, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = OnSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { paddingValues ->
        if (carregando && nome.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = emailUsuario,
                    onValueChange = {},
                    label = { Text("E-mail (Não editável)") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = textFieldShape,
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome Completo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = textFieldShape,
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = telefone,
                    onValueChange = { novoValor ->
                        val numeros = novoValor.filter { it.isDigit() }
                        if (numeros.length <= 11) {
                            telefone = numeros
                        }
                    },
                    label = { Text("Telefone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    visualTransformation = Mascaras.TelefoneVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = textFieldShape,
                    colors = textFieldColors
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { salvarAlteracoes() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp), // Altura aprimorada para o toque
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        disabledContainerColor = Primary.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !carregando
                ) {
                    if (carregando) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("SALVAR ALTERAÇÕES", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}