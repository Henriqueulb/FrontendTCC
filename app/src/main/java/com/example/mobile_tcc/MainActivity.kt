package com.example.mobile_tcc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mobile_tcc.ui.theme.Mobile_TCCTheme
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                "canal_medicamentos",
                "Lembretes de Rotina",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações para horário de medicamentos"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(canal)
        }

        setContent {
            Mobile_TCCTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    // Cria o controlador de navegacao
    val navController = rememberNavController()

    // Define o mapa de navegacao
    NavHost(navController = navController, startDestination = "login") {

        // TELA DE LOGIN
        composable("login") {
            TelaLogin(navController)
        }

        // TELA DE CADASTRO
        composable("cadastro") {
            TelaCadastro(navController)
        }

        //  TELA HOME
        composable(
            route = "home/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            // Recupera o email passado na rota
            val email = backStackEntry.arguments?.getString("email") ?: ""
            TelaHome(navController, email)
        }

        // TELA DE ROTINA
        composable(
            route = "rotina/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            TelaRotina(navController, email)
        }

        // TELA DE ADICIONAR ITEM NA ROTINA
        composable(
            route = "adicionar_rotina/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            TelaAdicionarRotina(navController, email)
        }

        composable(
            route = "sintomas/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            TelaRegistroSintomas(navController, email)
        }

        composable(
            route = "perfil/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            TelaPerfil(navController, email)
        }

        composable(
            route = "editar_perfil/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            TelaEditarPerfil(navController, email)
        }

        composable(
            route = "configuracoes/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            TelaConfiguracoes(navController, email)
        }

        composable(
            route = "trocar_senha/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            TelaTrocarSenha(navController, email)
        }

        composable(
            route = "dados_medicos/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            TelaDadosMedicos(navController, email)
        }

        composable(
            route = "notificacoes/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            TelaNotificacoes(navController, email)
        }

        composable(
            route = "acompanhantes/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            TelaAcompanhantes(navController, email)
        }
    }
}