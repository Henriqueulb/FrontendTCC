package com.example.mobile_tcc

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaRotina(navController: NavController, emailUsuario: String, idRotina: Int) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var listaTarefas by remember { mutableStateOf<List<ItemRotinaDTO>>(emptyList()) }
    var carregando by remember { mutableStateOf(true) }

    // sinalizacao de "refresh" vindo da tela de adicao
    val precisaAtualizar = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Boolean>("refresh")
        ?.observeAsState()

    fun carregarDados() {
        scope.launch {
            try {
                carregando = true
                val response = RetrofitClient.api.listarItensDaRotina(idRotina)
                if (response.isSuccessful) {
                    listaTarefas = response.body() ?: emptyList()
                } else {
                    Toast.makeText(context, "Erro ao carregar itens", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro de conexão", Toast.LENGTH_SHORT).show()
            } finally {
                carregando = false
            }
        }
    }

    fun excluirTarefa(idItem: Int) {
        scope.launch {
            try {
                val response = RetrofitClient.api.deletarRotina(idItem)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Item removido", Toast.LENGTH_SHORT).show()
                    carregarDados()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao excluir", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit, precisaAtualizar?.value) {
        carregarDados()
        // controle para nao entrar em loop
        navController.currentBackStackEntry?.savedStateHandle?.set("refresh", false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cuidados da Rotina", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D47A1))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Navega para adicionar item passando o ID da rotina pai
                    navController.navigate("adicionar_item_rotina/$idRotina")
                },
                containerColor = Color(0xFF0D47A1),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Cuidado")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (carregando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF0D47A1))
                }
            } else if (listaTarefas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum cuidado cadastrado nesta rotina.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(listaTarefas) { tarefa ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tarefa.titulo,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color(0xFF0D47A1)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Horário: ${tarefa.horario}", fontSize = 14.sp)
                                    if (!tarefa.dose.isNullOrBlank()) {
                                        Text(text = "Dose: ${tarefa.dose}", fontSize = 14.sp)
                                    }
                                }

                                IconButton(onClick = { excluirTarefa(tarefa.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Excluir",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}