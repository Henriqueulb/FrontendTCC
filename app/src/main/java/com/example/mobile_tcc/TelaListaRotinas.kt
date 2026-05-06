package com.example.mobile_tcc

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
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
fun TelaListaRotinas(navController: NavController, emailUsuario: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var rotinas by remember { mutableStateOf<List<RotinaResumoDTO>>(emptyList()) }
    var carregando by remember { mutableStateOf(true) }
    var menuExpandido by remember { mutableStateOf(false) }

    // Observa se precisa atualizar a lista (vindo de outras telas)
    val precisaAtualizar = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Boolean>("refresh")
        ?.observeAsState()

    fun carregarRotinas() {
        scope.launch {
            try {
                carregando = true
                val response = RetrofitClient.api.listarRotinas(emailUsuario)
                if (response.isSuccessful) {
                    rotinas = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao carregar rotinas", Toast.LENGTH_SHORT).show()
            } finally {
                carregando = false
            }
        }
    }

    LaunchedEffect(Unit, precisaAtualizar?.value) {
        carregarRotinas()
        navController.currentBackStackEntry?.savedStateHandle?.set("refresh", false)
    }

    fun concluirRotina(idRotina: Int) {
        scope.launch {
            try {
                val response = RetrofitClient.api.concluirRotina(idRotina)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Rotina concluída com sucesso!", Toast.LENGTH_SHORT).show()
                    carregarRotinas()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao atualizar status", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minhas Rotinas", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home/$emailUsuario") { popUpTo(0) } }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D47A1))
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { menuExpandido = true },
                    containerColor = Color(0xFF0D47A1),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Opções de Rotina")
                }

                DropdownMenu(
                    expanded = menuExpandido,
                    onDismissRequest = { menuExpandido = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Cadastrar nova rotina") },
                        onClick = {
                            menuExpandido = false
                            navController.navigate("criar_rotina/$emailUsuario")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Reutilizar rotina anterior") },
                        onClick = {
                            menuExpandido = false
                            // tela de historico
                            Toast.makeText(context, "Funcionalidade em breve", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    ) { padding ->
        if (carregando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF0D47A1))
            }
        } else if (rotinas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Você ainda não possui rotinas cadastradas.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rotinas) { rotina ->
                    val estaConcluida = rotina.status == "CONCLUIDA"

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (estaConcluida) Color(0xFFE8F5E9) else Color(0xFFF0F4F8)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("rotina_detalhes/$emailUsuario/${rotina.idRotina}")
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rotina.nomeRotina,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = if (estaConcluida) Color.Gray else Color.Black
                                )
                                Text(
                                    text = "Criada em: ${rotina.dataCriacao}",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }

                            if (!estaConcluida) {
                                IconButton(onClick = { concluirRotina(rotina.idRotina) }) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Concluir Rotina",
                                        tint = Color(0xFF2E7D32)
                                    )
                                }
                            } else {
                                Text(
                                    text = "CONCLUÍDA",
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}