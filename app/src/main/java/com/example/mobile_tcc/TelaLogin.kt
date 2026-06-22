package com.example.mobile_tcc

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobile_tcc.ui.theme.* // Importando as cores do seu tema
import kotlinx.coroutines.launch

@Composable
fun TelaLogin(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var carregando by remember { mutableStateOf(false) }

    fun realizarLogin() {
        if (email.isBlank() || senha.isBlank()) {
            Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            carregando = true
            try {
                val request = LoginRequest(email, senha)
                val response = RetrofitClient.api.login(request)

                if (response.isSuccessful) {
                    val dados = response.body()
                    val emailLimpo = email.trim().lowercase()

                    val sharedPrefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                    val editor = sharedPrefs.edit()
                        .putBoolean("isAcompanhante", dados?.isAcompanhante ?: false)
                        .putString("emailLogado", emailLimpo)

                    if (dados?.isAcompanhante == true) {
                        editor.remove("emailPaciente")
                        editor.apply()
                        navController.navigate("selecionar_paciente/$emailLimpo") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        editor.putString("emailPaciente", emailLimpo)
                        editor.apply()
                        navController.navigate("home/$emailLimpo") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                } else {
                    Toast.makeText(context, "Login falhou. Verifique seus dados.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro de conexão: ${e.message}", Toast.LENGTH_SHORT).show()
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
        containerColor = Background // Fundo claro padrão do app
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bem-vindo ao Medicare!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Primary
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = textFieldShape,
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                label = { Text("Senha") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = textFieldShape,
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { realizarLogin() },
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
                    Text("ENTRAR", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Não tem conta? Cadastre-se",
                color = Primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    navController.navigate("cadastro")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Esqueci minha senha",
                color = Primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    if (email.isNotBlank()) {
                        navController.navigate("trocar_senha/$email")
                    } else {
                        Toast.makeText(context, "Digite seu e-mail acima para trocar a senha", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}