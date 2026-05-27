package com.example.mobile_tcc

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.material3.Checkbox

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Criar Nova Conta", fontSize = 24.sp, color = Color(0xFF0D47A1))

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome Completo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

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
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = isAcompanhante,
                onCheckedChange = { isAcompanhante = it },
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0D47A1))
            )
            Text("Sou um acompanhante")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { realizarCadastro() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
            enabled = !carregando
        ) {
            if (carregando) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("CADASTRAR")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Já tem uma conta? Entrar",
            color = Color(0xFF0D47A1),
            modifier = Modifier.clickable {
                navController.popBackStack()
            }
        )
    }
}