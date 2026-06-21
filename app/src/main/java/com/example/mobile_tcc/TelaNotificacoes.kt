package com.example.mobile_tcc

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun TelaNotificacoes(navController: NavController, emailUsuario: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var notificacoesAtivas by remember { mutableStateOf(true) }
    var somAtivo by remember { mutableStateOf(true) }
    var carregando by remember { mutableStateOf(true) }

    // Carregar configuracoes do Back-end
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = RetrofitClient.api.getConfigNotificacao(emailUsuario)
                if (response.isSuccessful) {
                    val config = response.body()
                    if (config != null) {
                        notificacoesAtivas = config.ativo
                        somAtivo = config.som
                    }
                }
            } catch (e: Exception) {
                // Silencioso ou log
            } finally {
                carregando = false
            }
        }
    }

    fun salvarConfig() {
        scope.launch {
            try {
                val dto = NotificacaoConfigDTO(emailUsuario, notificacoesAtivas, somAtivo)
                RetrofitClient.api.salvarConfigNotificacao(dto)
                Toast.makeText(context, "Configurações salvas", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao salvar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Notificações", color = Primary, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = OnSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { paddingValues ->
        if (carregando) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Preferências de Alerta",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Switch 1: Receber Lembretes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Receber Lembretes", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text("Alertar nos horários da rotina", fontSize = 12.sp, color = OnSurfaceVariant)
                    }
                    Switch(
                        checked = notificacoesAtivas,
                        onCheckedChange = { notificacoesAtivas = it },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Primary,
                            checkedThumbColor = Color.White
                        )
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = OutlineVariant)

                // Switch 2: Som
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Emitir Som", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Switch(
                        checked = somAtivo,
                        onCheckedChange = { somAtivo = it },
                        enabled = notificacoesAtivas,
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Primary,
                            checkedThumbColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { salvarConfig() },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Outlined.Notifications, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SALVAR PREFERÊNCIAS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}