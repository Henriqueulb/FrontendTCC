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
fun TelaAdicionarRotina(navController: NavController, idRotina: Int) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nomeMedicamento by remember { mutableStateOf("") }
    var horario by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }

    var carregando by remember { mutableStateOf(false) }

    // Estados para controle de erro
    var erroNome by remember { mutableStateOf(false) }
    var erroHorario by remember { mutableStateOf(false) }
    var erroDose by remember { mutableStateOf(false) }

    fun salvarCuidado() {
        erroNome = nomeMedicamento.isBlank()
        erroHorario = horario.isBlank() || horario.length < 4
        erroDose = dose.isBlank()

        if (erroNome || erroHorario || erroDose) {
            Toast.makeText(context, "Preencha os campos obrigatórios corretamente.", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            carregando = true
            try {
                val dto = NovoItemRotinaDTO(
                    idRotina = idRotina,
                    titulo = nomeMedicamento,
                    horario = Mascaras.formatarHora(horario),
                    dose = dose,
                    descricao = descricao.takeIf { it.isNotBlank() }
                )

                val response = RetrofitClient.api.criarItemRotina(dto)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Cuidado adicionado com sucesso!", Toast.LENGTH_SHORT).show()
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                    navController.popBackStack()
                } else {
                    Toast.makeText(context, "Erro ao salvar cuidado", Toast.LENGTH_SHORT).show()
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
                title = { Text("Novo Cuidado", color = Color.White) },
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
            OutlinedTextField(
                value = nomeMedicamento,
                onValueChange = {
                    nomeMedicamento = it
                    if (erroNome) erroNome = false
                },
                label = { Text("Nome (Ex: Dipirona, Fisioterapia) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = erroNome,
                supportingText = { if (erroNome) Text("O nome é obrigatório") }
            )

            OutlinedTextField(
                value = horario,
                onValueChange = { valorDigitado ->
                    if (valorDigitado.length <= 4) {
                        horario = valorDigitado.filter { it.isDigit() }
                        if (erroHorario) erroHorario = false
                    }
                },
                label = { Text("Horário (Ex: 08:00) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = erroHorario,
                supportingText = { if (erroHorario) Text("Digite 4 números para o horário") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),

                visualTransformation = Mascaras.HorarioVisualTransformation()
            )

            OutlinedTextField(
                value = dose,
                onValueChange = {
                    dose = it
                    if (erroDose) erroDose = false
                },
                label = { Text("Dose *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = erroDose,
                supportingText = { if (erroDose) Text("Informar a dose é obrigatória") }
            )

            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("Descrição / Observação (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { salvarCuidado() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                enabled = !carregando
            ) {
                if (carregando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("SALVAR CUIDADO", color = Color.White)
                }
            }
        }
    }
}