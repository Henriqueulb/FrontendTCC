package com.example.mobile_tcc

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.mobile_tcc.ui.theme.* // Importando as cores do seu tema
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
        containerColor = Background, // Fundo claro padrão do app
        topBar = {
            TopAppBar(
                title = { Text("Nova Rotina", color = Primary, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
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
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Crie uma nova rotina para organizar seus cuidados de saúde.",
                color = OnSurfaceVariant,
                fontSize = 16.sp
            )

            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome da Rotina (ex: Tratamento Pós Op)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = textFieldShape,
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { salvar() },
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
                    Text("SALVAR ROTINA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            // Pequeno respiro na parte inferior
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}