package com.example.mobile_tcc

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.border


// CORES EXTRAÍDAS DO (Tailwind Config)
private val ColorBackground = Color(0xFFF7F9FB)
private val ColorSurfaceLowest = Color(0xFFFFFFFF)
private val ColorOnSurface = Color(0xFF191C1E)
private val ColorOnSurfaceVariant = Color(0xFF424654)
private val ColorPrimary = Color(0xFF0040A1)
private val ColorPrimaryContainer = Color(0xFF0056D2)
private val ColorOutlineVariant = Color(0xFFC3C6D6)
private val ColorSecondaryContainer = Color(0xFFA0F399)
private val ColorSecondary = Color(0xFF1B6D24)
private val ColorPrimaryFixed = Color(0xFFDAE2FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaHome(navController: NavController, emailUsuario: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Verifica se o usuario atual esta logado como acompanhante
    val sharedPrefs = remember { context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }
    val emailLogado = remember { sharedPrefs.getString("emailLogado", "") ?: "" }
    val isAcompanhante = remember { sharedPrefs.getBoolean("isAcompanhante", false) }

    var progresso by remember { mutableStateOf(0.0f) }
    var tarefasPendentes by remember { mutableStateOf<List<ItemRotinaDTO>>(emptyList()) }
    var nomeExibicao by remember { mutableStateOf("Carregando...") }
    var carregando by remember { mutableStateOf(true) }

    fun carregarHome() {
        scope.launch {
            try {
                // Busca os dados clinicos do paciente que esta na rota
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

    fun atualizarStatusTarefa(tarefa: ItemRotinaDTO, foiFeito: Boolean) {
        val dataHoje = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        scope.launch {
            try {
                val statusDto = StatusRotinaDTO(
                    idItem = tarefa.id,
                    feito = foiFeito,
                    data = dataHoje
                )
                val emailExecutor = emailLogado.ifEmpty { emailUsuario }
                val response = RetrofitClient.api.atualizarStatus(emailExecutor, statusDto)

                if (response.isSuccessful) {

                    if (foiFeito) {
                        val idParaCancelar = (tarefa.titulo + tarefa.horario).hashCode()
                        AgendadorNotificacoes.cancelarReforco(context, idParaCancelar)
                    }

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
        containerColor = ColorBackground, // Fundo claro geral do HTML
        bottomBar = {
            // Barra de navegacao
            NavigationBar(
                containerColor = ColorSurfaceLowest,
                tonalElevation = 8.dp,
                modifier = Modifier.border(1.dp, ColorOutlineVariant.copy(alpha = 0.5f))
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = true,
                    onClick = { /* Já estamos na Home */ },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ColorPrimaryContainer,
                        indicatorColor = ColorPrimaryContainer.copy(alpha = 0.2f),
                        selectedTextColor = ColorPrimaryContainer,
                        unselectedIconColor = ColorOnSurfaceVariant,
                        unselectedTextColor = ColorOnSurfaceVariant
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Rotina") },
                    label = { Text("Rotina") },
                    selected = false,
                    onClick = { navController.navigate("rotina/$emailUsuario") },
                    colors = NavigationBarItemDefaults.colors(unselectedIconColor = ColorOnSurfaceVariant, unselectedTextColor = ColorOnSurfaceVariant)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Face, contentDescription = "Sintomas") },
                    label = { Text("Sintomas") },
                    selected = false,
                    onClick = { navController.navigate("registro_sintomas/$emailUsuario") },
                    colors = NavigationBarItemDefaults.colors(unselectedIconColor = ColorOnSurfaceVariant, unselectedTextColor = ColorOnSurfaceVariant)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = false,
                    onClick = {
                        val emailParaPerfil = emailLogado.ifEmpty { emailUsuario }
                        navController.navigate("perfil/$emailParaPerfil")
                    },
                    colors = NavigationBarItemDefaults.colors(unselectedIconColor = ColorOnSurfaceVariant, unselectedTextColor = ColorOnSurfaceVariant)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Banner Acompanhante
            if (isAcompanhante) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ColorSecondaryContainer.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, ColorSecondaryContainer),
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
                                    color = ColorSecondary
                                )
                                Text(
                                    text = "Visualizando: $nomeExibicao",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = ColorOnSurface
                                )
                            }
                            TextButton(
                                onClick = {
                                    val destino = if (emailLogado.isNotEmpty()) "selecionar_paciente/$emailLogado" else "login"
                                    navController.navigate(destino) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            ) {
                                Text("Trocar", color = ColorSecondary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Cabeçalho
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(ColorOutlineVariant.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Foto",
                            modifier = Modifier.size(48.dp),
                            tint = ColorOnSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "Olá,", fontSize = 14.sp, color = ColorOnSurfaceVariant)
                        Text(text = nomeExibicao, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ColorPrimary)
                    }
                }
            }

            // Seu Progresso Hoje
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ColorSurfaceLowest),
                    border = BorderStroke(1.dp, ColorOutlineVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Seu Progresso Hoje",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ColorOnSurface,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Circulo de Progresso
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(150.dp)) {
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.fillMaxSize(),
                                color = ColorOutlineVariant.copy(alpha = 0.3f),
                                strokeWidth = 12.dp,
                                strokeCap = StrokeCap.Round
                            )
                            CircularProgressIndicator(
                                progress = { progresso },
                                modifier = Modifier.fillMaxSize(),
                                color = ColorPrimary,
                                strokeWidth = 12.dp,
                                strokeCap = StrokeCap.Round
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${(progresso * 100).toInt()}%",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorPrimary
                                )
                                Text(
                                    text = "Aderência",
                                    fontSize = 12.sp,
                                    color = ColorOnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Proximos Cuidados
            // CABEÇALHO DA LISTA DE TAREFAS ("Proximos Cuidados" e "Ver tudo")
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Próximos Cuidados",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ColorOnSurface
                    )
                    Text(
                        text = "Ver tudo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ColorPrimary,
                        modifier = Modifier.clickable { navController.navigate("rotina/$emailUsuario") }
                    )
                }
            }

            // CARDS DE MEDICACAO
            if (carregando) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ColorPrimary)
                    }
                }
            } else if (tarefasPendentes.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ColorSurfaceLowest),
                        border = BorderStroke(1.dp, ColorOutlineVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🎉", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Tudo concluído por hoje!", color = ColorOnSurfaceVariant, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            } else {
                items(tarefasPendentes) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ColorSurfaceLowest),
                        border = BorderStroke(1.dp, ColorOutlineVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().clickable { navController.navigate("rotina/$emailUsuario") }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // LADO ESQUERDO: Icone e Nomes
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Icone Médico
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ColorPrimaryFixed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MedicalServices, // Icone de Medicamento
                                        contentDescription = "Medicamento",
                                        tint = ColorPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Titulo e Dose
                                Column {
                                    Text(
                                        text = item.titulo,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ColorOnSurface
                                    )
                                    if (!item.dose.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Dose: ${item.dose}",
                                            fontSize = 12.sp,
                                            color = ColorOnSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // LADO DIREITO: Horário e Checkbox
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.horario,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorPrimary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Checkbox(
                                    checked = item.feita,
                                    onCheckedChange = { isChecked ->
                                        atualizarStatusTarefa(item, isChecked)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = ColorPrimary,
                                        uncheckedColor = ColorOutlineVariant,
                                        checkmarkColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}