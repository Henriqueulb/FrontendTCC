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

    // Estilo padronizado para todos os TextFields desta tela
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Primary,
        unfocusedBorderColor = OutlineVariant,
        focusedLabelColor = Primary,
        unfocusedLabelColor = OnSurfaceVariant,
        cursorColor = Primary,
        errorBorderColor = ErrorColor,
        errorLabelColor = ErrorColor,
        errorCursorColor = ErrorColor
    )
    val textFieldShape = RoundedCornerShape(12.dp)

    Scaffold(
        containerColor = Background, // Fundo claro do HTML
        topBar = {
            TopAppBar(
                title = { Text("Novo Cuidado", color = Primary, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
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
                supportingText = { if (erroNome) Text("O nome é obrigatório") },
                shape = textFieldShape,
                colors = textFieldColors
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
                visualTransformation = Mascaras.HorarioVisualTransformation(),
                shape = textFieldShape,
                colors = textFieldColors
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
                supportingText = { if (erroDose) Text("Informar a dose é obrigatória") },
                shape = textFieldShape,
                colors = textFieldColors
            )

            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("Descrição / Observação (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = textFieldShape,
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { salvarCuidado() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp), // Altura melhorada para clique
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
                    Text("SALVAR CUIDADO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            // Pequeno respiro na parte inferior
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}