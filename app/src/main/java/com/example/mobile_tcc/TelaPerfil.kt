package com.example.mobile_tcc

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobile_tcc.ui.theme.* // Importando as cores do seu tema

@Composable
fun TelaPerfil(navController: NavController, emailUsuario: String) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
    val emailPaciente = remember { sharedPrefs.getString("emailPaciente", emailUsuario) ?: emailUsuario }

    // Funcao de Logout
    fun realizarLogout() {
        Toast.makeText(context, "Saindo...", Toast.LENGTH_SHORT).show()

        sharedPrefs.edit().clear().apply()

        navController.navigate("login") {
            popUpTo(0) { inclusive = true }
        }
    }

    Scaffold(
        containerColor = Background, // Fundo claro padrão
        bottomBar = {
            // Barra de Navegação Padronizada
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 4.dp,
                modifier = Modifier.border(1.dp, OutlineVariant.copy(alpha = 0.5f))
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Home, "Home") },
                    label = { Text("Home", fontSize = 12.sp) },
                    selected = false,
                    onClick = { navController.navigate("home/$emailPaciente") },
                    colors = NavigationBarItemDefaults.colors(unselectedIconColor = OnSurfaceVariant, unselectedTextColor = OnSurfaceVariant)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.List, "Rotina") },
                    label = { Text("Rotina", fontSize = 12.sp) },
                    selected = false,
                    onClick = { navController.navigate("rotina/$emailPaciente") },
                    colors = NavigationBarItemDefaults.colors(unselectedIconColor = OnSurfaceVariant, unselectedTextColor = OnSurfaceVariant)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Face, "Sintomas") },
                    label = { Text("Sintomas", fontSize = 12.sp) },
                    selected = false,
                    onClick = { navController.navigate("registro_sintomas/$emailPaciente") },
                    colors = NavigationBarItemDefaults.colors(unselectedIconColor = OnSurfaceVariant, unselectedTextColor = OnSurfaceVariant)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, "Perfil") }, // Preenchido por estar ativo
                    label = { Text("Perfil", fontSize = 12.sp) },
                    selected = true,
                    onClick = { /* Já está aqui */ },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary,
                        selectedTextColor = Primary,
                        indicatorColor = PrimaryFixed
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()) // Rolar tela em celulares menores
        ) {
            // HEADER DO PERFIL (Destaque visual)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryContainer)
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Foto",
                            modifier = Modifier.size(56.dp),
                            tint = PrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = emailUsuario,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // LISTA DE OPÇÕES (Cards padronizados)
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Meus Dados
                OpcaoPerfil(
                    icone = Icons.Outlined.Person,
                    titulo = "Meus Dados",
                    subtitulo = "Alterar nome, telefone ou senha",
                    onClick = { navController.navigate("editar_perfil/$emailUsuario") }
                )

                // Configuracoes
                OpcaoPerfil(
                    icone = Icons.Outlined.Settings,
                    titulo = "Configurações",
                    subtitulo = "Segurança, Notificações",
                    onClick = { navController.navigate("configuracoes/$emailUsuario") }
                )

                // Dados medicos
                OpcaoPerfil(
                    icone = Icons.Outlined.MedicalServices,
                    titulo = "Dados Médicos",
                    subtitulo = "Alergias, Doenças e Medicamentos",
                    onClick = { navController.navigate("dados_medicos/$emailUsuario") }
                )

                // Acompanhantes
                OpcaoPerfil(
                    icone = Icons.Outlined.Group,
                    titulo = "Acompanhantes",
                    subtitulo = "Código de convite e acessos",
                    onClick = { navController.navigate("acompanhantes/$emailUsuario") }
                )
                // Relatórios
                OpcaoPerfil(
                    icone = Icons.Outlined.Description,
                    titulo = "Relatórios",
                    subtitulo = "Visualizar relatórios de saúde",
                    onClick = { navController.navigate("relatorio/$emailUsuario") }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Botão de Sair Estilizado
                Button(
                    onClick = { realizarLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, null, tint = ErrorColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sair da Conta", color = ErrorColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun OpcaoPerfil(icone: ImageVector, titulo: String, subtitulo: String, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, OutlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quadradinho do Ícone
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryFixed),
                contentAlignment = Alignment.Center
            ) {
                Icon(icone, null, tint = Primary, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Textos
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnSurface)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitulo, fontSize = 12.sp, color = OnSurfaceVariant)
            }

            // Seta
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = OutlineVariant)
        }
    }
}