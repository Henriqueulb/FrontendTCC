package com.example.mobile_tcc

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.material.icons.filled.Group

@Composable
fun TelaPerfil(navController: NavController, emailUsuario: String) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
    val emailPaciente = remember { sharedPrefs.getString("emailPaciente", emailUsuario) ?: emailUsuario }

    // Funcao de Logout
    fun realizarLogout() {
        Toast.makeText(context, "Saindo...", Toast.LENGTH_SHORT).show()

        navController.navigate("login") {
            popUpTo(0) { inclusive = true }
        }
    }

    Scaffold(
        bottomBar = {
            // Barra de Navegacao
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, "Home") },
                    label = { Text("Home") },
                    selected = false,
                    onClick = { navController.navigate("home/$emailPaciente") },
                    colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, "Rotina") },
                    label = { Text("Rotina") },
                    selected = false,
                    onClick = { navController.navigate("rotina/$emailPaciente") },
                    colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Face, "Sintomas") },
                    label = { Text("Sintomas") },
                    selected = false,
                    onClick = { navController.navigate("registro_sintomas/$emailPaciente") },
                    colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, "Perfil") },
                    label = { Text("Perfil") },
                    selected = true,
                    onClick = { /* Já está aqui */ },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF0D47A1))
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5)) // Fundo cinza claro
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D47A1))
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Foto",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        tint = Color(0xFF0D47A1)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = emailUsuario,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // LISTA DE OPCOES
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {

                // Meus Dados
                OpcaoPerfil(
                    icone = Icons.Default.Person,
                    titulo = "Meus Dados",
                    subtitulo = "Alterar nome, telefone ou senha",
                    onClick = { navController.navigate("editar_perfil/$emailUsuario") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Configuracoes
                OpcaoPerfil(
                    icone = Icons.Default.Settings,
                    titulo = "Configurações",
                    subtitulo = "Segurança, Notificações",
                    onClick = { navController.navigate("configuracoes/$emailUsuario") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dados medicos
                OpcaoPerfil(
                    icone = Icons.Default.MedicalServices,
                    titulo = "Dados Médicos",
                    subtitulo = "Alergias, Doenças e Medicamentos",
                    onClick = { navController.navigate("dados_medicos/$emailUsuario") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Acompanhantes
                OpcaoPerfil(
                    icone = Icons.Default.Group,
                    titulo = "Acompanhantes",
                    subtitulo = "Código de convite e acessos",
                    onClick = { navController.navigate("acompanhantes/$emailUsuario") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Ajuda
                OpcaoPerfil(
                    icone = Icons.Default.Info,
                    titulo = "Ajuda",
                    subtitulo = "FAQ, Suporte",
                    onClick = { Toast.makeText(context, "Ajuda em breve", Toast.LENGTH_SHORT).show() }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Botão de Sair
                Button(
                    onClick = { realizarLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sair da Conta", color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun OpcaoPerfil(icone: ImageVector, titulo: String, subtitulo: String, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icone, null, tint = Color(0xFF0D47A1), modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text(subtitulo, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.Gray)
        }
    }
}