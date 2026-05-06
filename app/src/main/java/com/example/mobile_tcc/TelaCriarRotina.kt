package com.example.mobile_tcc

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCriarRotina(navController: NavController, emailUsuario: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nome by remember { mutableStateOf("") }
    var carregando by remember { mutableStateOf(false) }

    fun salvar() {
        if (nome.isBlank()) {
            Toast.makeText(context, "Por favor, insira um nome para a rotina.", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            carregando = true
            try {
                // Passando apenas email e nome
                val request = CriarRotinaDTO(
                    emailUsuario = emailUsuario,
                    nomeRotina = nome
                )
                val res = RetrofitClient.api.criarNovaRotina(request)

                if (res.isSuccessful) {
                    Toast.makeText(context, "Rotina criada com sucesso!", Toast.LENGTH_SHORT).show()
                    // Sinaliza para a tela de lista de rotinas que ela precisa recarregar os dados
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                    navController.popBackStack()
                } else {
                    Toast.makeText(context, "Erro ao criar rotina.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro de conexão com o servidor.", Toast.LENGTH_SHORT).show()
            } finally {
                carregando = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nova Rotina", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D47A1))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Crie uma nova rotina para organizar seus cuidados de saúde.",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome da Rotina (ex: Tratamento Pós Op)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { salvar() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                enabled = !carregando
            ) {
                if (carregando) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("SALVAR ROTINA", color = Color.White)
                }
            }
        }
    }
}