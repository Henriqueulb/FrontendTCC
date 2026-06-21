package com.example.mobile_tcc

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobile_tcc.ui.theme.* // Importando as cores do seu tema
import kotlinx.coroutines.launch

@Composable
fun TelaCadastro(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var carregando by remember { mutableStateOf(false) }
    var isAcompanhante by remember { mutableStateOf(false) }

    fun realizarCadastro() {
        if (nome.isBlank() || email.isBlank() || senha.isBlank()) {
            Toast.makeText(context, "Preencha os campos obrigatórios", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            carregando = true
            try {
                val request = CadastroRequest(nome, email, senha, telefone, isAcompanhante)
                val response = RetrofitClient.api.cadastro(request)

                if (response.isSuccessful) {
                    Toast.makeText(context, "Conta criada! Faça login.", Toast.LENGTH_LONG).show()
                    navController.popBackStack()
                } else {
                    Toast.makeText(context, "Erro: ${response.body()?.mensagem ?: "Falha ao cadastrar"}", Toast.LENGTH_SHORT).show()
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
        containerColor = Background // Fundo claro padrão do app
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Criar Nova Conta",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome Completo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = textFieldShape,
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = textFieldShape,
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = telefone,
                onValueChange = { novoValor ->
                    val numeros = novoValor.filter { it.isDigit() }
                    if (numeros.length <= 11) {
                        telefone = numeros
                    }
                },
                label = { Text("Telefone (Opcional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                visualTransformation = Mascaras.TelefoneVisualTransformation(),
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

            Spacer(modifier = Modifier.height(16.dp))

            // Checkbox estilizado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = isAcompanhante,
                    onCheckedChange = { isAcompanhante = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Primary,
                        uncheckedColor = OutlineVariant,
                        checkmarkColor = Color.White
                    )
                )
                Text("Sou um acompanhante", color = OnSurface, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { realizarCadastro() },
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
                    Text("CADASTRAR", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Já tem uma conta? Entrar",
                color = Primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    navController.popBackStack()
                }
            )
        }
    }
}