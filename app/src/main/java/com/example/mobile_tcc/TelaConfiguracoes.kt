package com.example.mobile_tcc

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobile_tcc.ui.theme.* import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaConfiguracoes(navController: NavController, emailUsuario: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var mostrarDialogExclusao by remember { mutableStateOf(false) }
    var carregando by remember { mutableStateOf(false) }
    var senhaConfirmacao by remember { mutableStateOf("") } // Novo estado para a senha

    fun desativarConta() {
        if (senhaConfirmacao.isBlank()) {
            Toast.makeText(context, "Digite sua senha para continuar.", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            carregando = true
            try {
                // Passa o DTO com email e senha para a nova rota POST
                val dto = DesativarContaDTO(emailUsuario, senhaConfirmacao)
                val response = RetrofitClient.api.desativarConta(dto)

                if (response.isSuccessful && response.body()?.sucesso == true) {
                    Toast.makeText(context, "Conta desativada.", Toast.LENGTH_LONG).show()

                    val sharedPrefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                    sharedPrefs.edit().clear().apply()
                    // Volta para o login e limpa tudo
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                } else {
                    Toast.makeText(context, response.body()?.mensagem ?: "Erro ao desativar conta. Verifique sua senha.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro de conexão.", Toast.LENGTH_SHORT).show()
            } finally {
                carregando = false
                mostrarDialogExclusao = false
                senhaConfirmacao = "" // Limpa a senha por segurança
            }
        }
    }

    // UI PRINCIPAL
    Scaffold(
        containerColor = Background, // Fundo claro padrão
        topBar = {
            TopAppBar(
                title = { Text("Configurações", color = Primary, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = OnSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Bloco de Configurações Agrupado em um Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, OutlineVariant),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Trocar Senha
                    ItemConfiguracao(
                        icone = Icons.Outlined.Lock,
                        titulo = "Trocar Senha",
                        onClick = { navController.navigate("trocar_senha/$emailUsuario") }
                    )

                    HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f))

                    // Notificações
                    ItemConfiguracao(
                        icone = Icons.Outlined.Notifications,
                        titulo = "Notificações",
                        onClick = { navController.navigate("notificacoes/$emailUsuario") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Desativar Conta
            Button(
                onClick = { mostrarDialogExclusao = true },
                colors = ButtonDefaults.buttonColors(containerColor = ErrorContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp), // Altura aprimorada
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                if (carregando) {
                    CircularProgressIndicator(color = ErrorColor, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.DeleteForever, null, tint = ErrorColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Desativar Minha Conta", color = ErrorColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    // DIALOG DE CONFIRMAÇÃO COM SENHA
    if (mostrarDialogExclusao) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogExclusao = false
                senhaConfirmacao = "" // Limpa o campo se fechar o dialog
            },
            containerColor = Color.White,
            title = {
                Text("Desativar Conta?", fontWeight = FontWeight.Bold, color = OnSurface)
            },
            text = {
                Column {
                    Text(
                        text = "Essa ação irá desativar sua conta para sempre. Para confirmar, digite sua senha abaixo:",
                        color = OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = senhaConfirmacao,
                        onValueChange = { senhaConfirmacao = it },
                        label = { Text("Sua Senha") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { desativarConta() },
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorColor)
                ) {
                    Text("CONFIRMAR", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogExclusao = false
                        senhaConfirmacao = ""
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = OnSurfaceVariant)
                ) {
                    Text("Cancelar", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

// COMPONENTE DE ITEM DE MENU ADAPTADO
@Composable
fun ItemConfiguracao(icone: ImageVector, titulo: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(PrimaryFixed, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icone, null, tint = Primary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = titulo,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = OnSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = OutlineVariant)
    }
}