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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.mobile_tcc.ui.theme.*
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.example.mobile_tcc.RelatorioCompletoDTO



@OptIn(ExperimentalMaterial3Api::class)


@Composable
fun TelaRelatorio(navController: NavController, email: String) {
    android.util.Log.d("TESTE_DEBUG", "TelaRelatorio aberta com email: $email")
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var relatorioData by remember { mutableStateOf<RelatorioCompletoDTO?>(null) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isWebViewReady by remember { mutableStateOf(false) }

    // 1. Estado do Filtro
    var selectedDays by remember { mutableIntStateOf(7) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            webViewRef?.let { gerarPDF(context, it) }
        } else {
            Toast.makeText(context, "Permissão negada para salvar PDF", Toast.LENGTH_SHORT).show()
        }
    }

    val jsonProcessor = Json { ignoreUnknownKeys = true }

    // 2. O LaunchedEffect agora vigia 'selectedDays' também!
    LaunchedEffect(email, selectedDays) {
        android.util.Log.d("TESTE_DEBUG", "Iniciando busca completa para: $email")
        scope.launch(Dispatchers.IO) {
            try {
                // 1. Busca os dados métricos do relatório via Retrofit
                val response = RetrofitClient.api.getRelatorioDetalhado(email, selectedDays)
                val data = response.body()
                
                if (data != null) {
                    // 2. Busca dados complementares em paralelo para enriquecer o PDF
                    val perfil = RetrofitClient.api.getPerfil(email).body()
                    val ficha = RetrofitClient.api.getFichaMedica(email).body()
                    val acompanhantes = RetrofitClient.api.listarAcompanhantes(email).body()
                    val rotinas = RetrofitClient.api.listarRotinas(email).body()
                    
                    // Pega os itens da primeira rotina ativa (como exemplo de medicamentos/cuidados)
                    val itens = if (!rotinas.isNullOrEmpty()) {
                        RetrofitClient.api.listarItensDaRotina(rotinas.first().idRotina).body()
                    } else null

                    // Preenche o objeto com os dados extras
                    data.nomePaciente = perfil?.nome ?: "Não informado"
                    data.fichaMedica = ficha
                    data.acompanhantes = acompanhantes
                    data.itensRotina = itens

                    withContext(Dispatchers.Main) {
                        relatorioData = data
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("TESTE_DEBUG", "Erro na busca completa: ${e.message}")
            }
        }
    }
    // 3. Injeção de Dados na WebView (Dynamic PDF)
    LaunchedEffect(relatorioData, isWebViewReady) {
        if (relatorioData != null && isWebViewReady && webViewRef != null) {
            val jsonString = jsonProcessor.encodeToString(relatorioData)
            val jsonSafe = jsonString.replace("'", "\\'")
            
            // Injeta o comando JS para preencher o HTML
            webViewRef?.evaluateJavascript("carregarDados('$jsonSafe')", null)
            android.util.Log.d("WEBVIEW_DEBUG", "Dados injetados com sucesso no HTML do PDF!")
        }
    }

    // 4. WebView Invisível (apenas para processar o PDF)
    if (relatorioData != null) {
        AndroidView<WebView>(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true // Importante para o Chart.js

                    // LÊ O ARQUIVO HTML COMO TEXTO E PASSA PARA A WEBVIEW
                    val htmlContent = ctx.assets.open("Relatorio.html").bufferedReader().use { it.readText() }
                    loadDataWithBaseURL("https://app.assets/", htmlContent, "text/html", "UTF-8", null)

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            // Pequeno delay para garantir que o motor JS da WebView processou o Chart.js
                            view?.postDelayed({
                                webViewRef = view
                                isWebViewReady = true
                            }, 500)
                        }
                    }
                }
            },
            modifier = Modifier.size(1.dp)
        )
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
        LaunchedEffect(relatorioData) {
            android.util.Log.d("TESTE_DEBUG", "Dados do relatório mudaram: $relatorioData")
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Filtro de Período (Chips)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(7, 15, 30).forEach { dias ->
                        FilterChip(
                            selected = (selectedDays == dias),
                            onClick = { selectedDays = dias },
                            label = { Text("Últimos $dias dias", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

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
                        // Usa o dado real vindo do backend
                        val adesao = relatorioData?.taxaAdesaoGlobal ?: 0.0
                        Text("${adesao.toInt()}%", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Primary)

                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { (adesao / 100).toFloat() },
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
                    val totalSintomas = relatorioData?.sintomasFrequentes?.sumOf { it.contagem } ?: 0
                    InfoCard("Sintomas", totalSintomas.toString(), "ocorrências", Modifier.weight(1f))
                    
                    val totalDias = relatorioData?.evolucaoTemporal?.size ?: 0
                    InfoCard("Tratamento", totalDias.toString(), if (totalDias == 1) "dia registrado" else "dias registrados", Modifier.weight(1f))
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
                        
                        val adesao = relatorioData?.taxaAdesaoGlobal ?: 0.0
                        val mediaBemEstar = relatorioData?.mediaBemEstar ?: 0.0
                        
                        val bemEstarMsg = "A média de bem-estar relatada é de ${String.format("%.1f", mediaBemEstar)}/10."

                        Text(
                            text = "O paciente apresenta uma taxa de ${adesao.toInt()}% de adesão nos últimos $selectedDays dias. $bemEstarMsg",
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
                        if (isWebViewReady && webViewRef != null) {
                            // Verifica se precisa pedir permissão (apenas para Android < 10)
                            if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.P) {
                                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            } else {
                                gerarPDF(context, webViewRef!!)
                            }
                        } else {
                            Toast.makeText(context, "Aguardando renderização...", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = isWebViewReady,
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

@Composable
fun InfoCard(titulo: String, valor: String, legenda: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, OutlineVariant),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titulo, fontSize = 14.sp, color = OnSurfaceVariant)
            Text(valor, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Primary)
            Text(legenda, fontSize = 12.sp, color = OnSurfaceVariant)
        }
    }
}

// 3. Função de gerar PDF real usando a WebView carregada
fun gerarPDF(context: android.content.Context, webView: WebView) {
    try {
        val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as android.print.PrintManager
        val jobName = "Relatorio_Medico_${System.currentTimeMillis()}"
        val printAdapter = webView.createPrintDocumentAdapter(jobName)
        printManager.print(jobName, printAdapter, android.print.PrintAttributes.Builder().build())
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Erro ao gerar PDF", Toast.LENGTH_SHORT).show()
    }
}
