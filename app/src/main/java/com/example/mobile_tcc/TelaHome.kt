package com.example.mobile_tcc

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaHome(navController: NavController, emailUsuario: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Verifica se o usuário atual está logado como acompanhante
    val sharedPrefs = remember { context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }
    val isAcompanhante = remember { sharedPrefs.getBoolean("isAcompanhante", false) }

    var progresso by remember { mutableStateOf(0.0f) }
    var tarefasPendentes by remember { mutableStateOf<List<ItemRotinaDTO>>(emptyList()) }
    var nomeExibicao by remember { mutableStateOf("Carregando...") }
    var carregando by remember { mutableStateOf(true) }

    fun carregarHome() {
        scope.launch {
            try {
                val response = RetrofitClient.api.getHome(emailUsuario)
                if (response.isSuccessful) {
                    val dados = response.body()
                    if (dados != null) {
                        progresso = dados.progresso
                        tarefasPendentes = dados.tarefas.filter { !it.feita }
                        nomeExibicao = dados.nomeUsuario
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro de conexão", Toast.LENGTH_SHORT).show()
                nomeExibicao = "Usuário"
            } finally {
                carregando = false
            }
        }
    }

    fun atualizarStatusTarefa(idItem: Int, foiFeito: Boolean) {
        val dataHoje = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        scope.launch {
            try {
                val statusDto = StatusRotinaDTO(
                    idItem = idItem,
                    feito = foiFeito,
                    data = dataHoje
                )
                val response = RetrofitClient.api.atualizarStatus(statusDto)

                if (response.isSuccessful) {
                    carregarHome()
                } else {
                    Toast.makeText(context, "Erro ao atualizar status", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro de conexão", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        carregarHome()
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = true,
                    onClick = { /* Já estamos na Home */ },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF0D47A1))
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Rotina") },
                    label = { Text("Rotina") },
                    selected = false,
                    onClick = { navController.navigate("rotina/$emailUsuario") },
                    colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Face, contentDescription = "Sintomas") },
                    label = { Text("Sintomas") },
                    selected = false,
                    onClick = { navController.navigate("registro_sintomas/$emailUsuario") },
                    colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = false,
                    onClick = { navController.navigate("perfil/$emailUsuario") },
                    colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Banner informativo superior para o Perfil de Acompanhante
            if (isAcompanhante) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCC80)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Modo Acompanhante",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFE65100)
                                )
                                Text(
                                    text = "Visualizando: $nomeExibicao",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = Color.Black
                                )
                            }
                            TextButton(
                                onClick = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            ) {
                                Text("Trocar", color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Cabecalho
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Foto",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "Olá,", fontSize = 16.sp, color = Color.Gray)
                        Text(text = nomeExibicao, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
                    }
                }
            }

            // Progresso
            item {
                Text("Seu Progresso Hoje", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Aderência", fontWeight = FontWeight.SemiBold, color = Color(0xFF0D47A1))
                            Text("${(progresso * 100).toInt()}%", fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { progresso },
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                            color = Color(0xFF0D47A1),
                            trackColor = Color.White,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }

            // Lista Pendente
            item {
                Text("Próximos Cuidados", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            }

            if (carregando) {
                item { Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            } else if (tarefasPendentes.isEmpty()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🎉", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Tudo concluído por hoje!", color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            } else {
                items(tarefasPendentes) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().clickable { navController.navigate("rotina/$emailUsuario") }
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.titulo, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                Text("Horário: ${item.horario}", fontSize = 14.sp, color = Color.Black)
                                if (!item.dose.isNullOrBlank()) Text("Dose: ${item.dose}", fontSize = 14.sp, color = Color.Gray)
                            }

                            Checkbox(
                                checked = item.feita,
                                onCheckedChange = { isChecked ->
                                    atualizarStatusTarefa(item.id, isChecked)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFFE65100),
                                    uncheckedColor = Color(0xFFE65100),
                                    checkmarkColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}