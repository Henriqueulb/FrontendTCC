package com.example.mobile_tcc

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Folder
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
fun TelaListaRotinas(navController: NavController, emailUsuario: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var rotinas by remember { mutableStateOf<List<RotinaResumoDTO>>(emptyList()) }
    var carregando by remember { mutableStateOf(true) }
    var menuExpandido by remember { mutableStateOf(false) }
    var mostrarConcluidas by remember { mutableStateOf(false) }

    // Verifica se precisa atualizar a lista
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

    // REUTILIZAR ROTINA
    fun reutilizarRotina(idRotina: Int) {
        scope.launch {
            try {
                val response = RetrofitClient.api.reutilizarRotina(idRotina)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Rotina clonada! Ela já está ativa novamente.", Toast.LENGTH_SHORT).show()
                    mostrarConcluidas = false // OCULTA AS CONCLUÍDAS APOS REUTILIZAR
                    carregarRotinas()
                } else {
                    Toast.makeText(context, "Erro ao reutilizar rotina", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro de conexão", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Minhas Rotinas", color = Primary, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home/$emailUsuario") { popUpTo(0) } }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = OnSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { menuExpandido = true },
                    containerColor = Primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Opções de Rotina")
                }

                DropdownMenu(
                    expanded = menuExpandido,
                    onDismissRequest = { menuExpandido = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = { Text("Cadastrar nova rotina", color = OnSurface) },
                        onClick = {
                            menuExpandido = false
                            navController.navigate("criar_rotina/$emailUsuario")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (mostrarConcluidas) "Ocultar concluídas" else "Reutilizar rotina anterior", color = OnSurface) },
                        onClick = {
                            menuExpandido = false
                            mostrarConcluidas = !mostrarConcluidas
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp) // Espacamento padronizado
        ) {
            if (carregando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (rotinas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Folder, contentDescription = null, tint = OutlineVariant, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Você ainda não possui rotinas cadastradas.", color = OnSurfaceVariant, fontSize = 16.sp)
                    }
                }
            } else {
                val rotinasFiltradas = if (mostrarConcluidas) {
                    rotinas
                } else {
                    rotinas.filter { it.status != "CONCLUIDA" }
                }

                if (rotinasFiltradas.isEmpty() && !mostrarConcluidas) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Folder, contentDescription = null, tint = OutlineVariant, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Não há rotinas ativas no momento.", color = OnSurfaceVariant, fontSize = 16.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(rotinasFiltradas) { rotina ->
                            val estaConcluida = rotina.status == "CONCLUIDA"

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (estaConcluida) SecondaryContainer.copy(alpha = 0.15f) else Color.White
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (estaConcluida) SecondaryContainer else OutlineVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("rotina_detalhes/$emailUsuario/${rotina.idRotina}")
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = rotina.nomeRotina,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = if (estaConcluida) OnSurfaceVariant else Primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Criada em: ${rotina.dataCriacao}",
                                        fontSize = 12.sp,
                                        color = OnSurfaceVariant
                                    )
                                }

                                if (!estaConcluida) {
                                    // Botao de concluir se estiver ativa
                                    IconButton(
                                        onClick = { concluirRotina(rotina.idRotina) },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(PrimaryFixed, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Concluir Rotina",
                                            tint = Primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                } else {
                                    // botao de reutilizar se estiver concluida
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "CONCLUÍDA",
                                            color = Secondary,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 10.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        IconButton(
                                            onClick = { reutilizarRotina(rotina.idRotina) },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color.White, CircleShape)
                                                .border(1.dp, SecondaryContainer, CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Reutilizar",
                                                tint = Secondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
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
}
