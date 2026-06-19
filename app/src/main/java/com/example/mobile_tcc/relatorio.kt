package com.example.mobile_tcc

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaRelatorio(navController: NavController) {
    val context = LocalContext.current

    // Launcher para pedir permissão
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Se o usuário aceitou, pode chamar a função de salvar
            gerarPDF(context, "Conteúdo do Relatório...")
        } else {
            Toast.makeText(context, "Permissão negada, não foi possível salvar o PDF", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Relatório Clínico", color = Primary, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = OnSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Métrica Principal: Adesão
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, OutlineVariant),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Adesão Total", fontSize = 14.sp, color = OnSurfaceVariant)
                        Text("88%", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { 0.88f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Primary,
                            trackColor = OutlineVariant.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            // Cards de Sintomas (Bento style)
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoCard("Sintomas", "12", "ocorrências", Modifier.weight(1f))
                    InfoCard("Tratamento", "15/30", "dias", Modifier.weight(1f))
                }
            }

            // Observações Clínicas
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, OutlineVariant),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Observações Clínicas", fontWeight = FontWeight.Bold, color = Primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "O paciente apresenta uma taxa de 88 % de adesão nos primeiros 15 dias. Os episódios de dor de cabeça parecem concentrar-se no período matutino.",
                            color = OnSurfaceVariant,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Botão de Ação Exportar
            item {
                Button(
                    onClick = {
                        // Verifica se precisa pedir permissão (apenas para Android < 10)
                        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.P) {
                            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        } else {
                            // Android 10+ não precisa de permissão de escrita para pastas privadas
                            gerarPDF(context, "Conteúdo do Relatório...")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Outlined.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EXPORTAR RELATÓRIO PDF", fontWeight = FontWeight.Bold)
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// Stub para evitar erro de compilação se gerarPDF não estiver definido em outro lugar
fun gerarPDF(context: android.content.Context, conteudo: String) {
    // Implementação pendente
    Toast.makeText(context, "Gerando PDF: $conteudo", Toast.LENGTH_SHORT).show()
}

@Composable
fun InfoCard(titulo: String, valor: String, desc: String, modifier: Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, OutlineVariant),
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titulo, fontSize = 12.sp, color = OnSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(valor, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            Text(desc, fontSize = 12.sp, color = OnSurfaceVariant)
        }
    }
}
