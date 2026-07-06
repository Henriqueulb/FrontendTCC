package com.example.mobile_tcc

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobile_tcc.ui.theme.*
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
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Cuidados da Rotina", color = Primary, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = OnSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Navega para adicionar item passando o ID da rotina pai
                    navController.navigate("adicionar_item_rotina/$idRotina")
                },
                containerColor = Primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Cuidado")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            if (carregando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (listaTarefas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.MedicalServices, contentDescription = null, tint = OutlineVariant, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Nenhum cuidado cadastrado.", color = OnSurfaceVariant, fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(listaTarefas) { tarefa ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, OutlineVariant),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // LADO ESQUERDO: Icone e Detalhes
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(PrimaryFixed),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.MedicalServices,
                                            contentDescription = null,
                                            tint = Primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // Textos
                                    Column {
                                        Text(
                                            text = tarefa.titulo,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = OnSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = tarefa.horario, fontSize = 12.sp, color = OnSurfaceVariant)
                                        }
                                        if (!tarefa.dose.isNullOrBlank()) {
                                            Text(
                                                text = "Dose: ${tarefa.dose}",
                                                fontSize = 12.sp,
                                                color = OnSurfaceVariant,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }

                                // LADO DIREITO: Botao de Excluir
                                IconButton(
                                    onClick = { excluirTarefa(tarefa.id) },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(ErrorContainer.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Excluir",
                                        tint = ErrorColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}