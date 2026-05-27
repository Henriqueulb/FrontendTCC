package com.example.mobile_tcc

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Dados", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D47A1))
            )
        }
    ) { paddingValues ->
        if (carregando && nome.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = emailUsuario,
                    onValueChange = {},
                    label = { Text("E-mail (Não editável)") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome Completo") },
                    modifier = Modifier.fillMaxWidth()
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
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { salvarAlteracoes() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                    enabled = !carregando
                ) {
                    if (carregando) CircularProgressIndicator(color = Color.White) else Text("SALVAR ALTERAÇÕES")
                }
            }
        }
    }
}