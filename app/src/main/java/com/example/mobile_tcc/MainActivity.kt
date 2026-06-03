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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Mobile_TCCTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        // TELA DE LOGIN
        composable("login") {
            TelaLogin(navController)
        }

        // TELA DE CADASTRO
        composable("cadastro") {
            TelaCadastro(navController)
        }

        // TELA HOME
        composable(
            route = "home/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            TelaHome(navController, email)
        }

        // 1. Lista de Pastas de Rotinas (Aba de Rotinas Principal)
        composable(
            route = "rotina/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            TelaListaRotinas(navController, email)
        }

        // 2. Criar Nova Pasta de Rotina
        composable(
            route = "criar_rotina/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            TelaCriarRotina(navController, email)
        }

        // 3. Detalhes da Rotina (Lista os cuidados/itens dentro de uma pasta especifica)
        composable(
            route = "rotina_detalhes/{email}/{idRotina}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("idRotina") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val idRotina = backStackEntry.arguments?.getInt("idRotina") ?: 0
            TelaRotina(navController, email, idRotina)
        }

        // 4. Adicionar um Cuidado/Item dentro de uma Rotina especifica
        composable(
            route = "adicionar_item_rotina/{idRotina}",
            arguments = listOf(
                navArgument("idRotina") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val idRotina = backStackEntry.arguments?.getInt("idRotina") ?: 0
            TelaAdicionarRotina(navController, idRotina)
        }

        // TELA DE PERFIL
        composable(
            route = "perfil/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            TelaPerfil(navController, email)
        }

        // TELA DE REGISTRO DE SINTOMAS
        composable(
            route = "registro_sintomas/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            TelaRegistroSintomas(navController, email)
        }

        // TELA DE CONFIGURACOES
        composable(
            route = "configuracoes/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            TelaConfiguracoes(navController, email)
        }

        // TELA DE DADOS MEDICOS
        composable(
            route = "dados_medicos/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            TelaDadosMedicos(navController, email)
        }

        // TELA DE ACOMPANHANTES
        composable(
            route = "acompanhantes/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            TelaAcompanhantes(navController, email)
        }

        // TELA EDITAR PERFIL
        composable(
            route = "editar_perfil/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            TelaEditarPerfil(navController, email)
        }

        // TELA TROCAR SENHA
        composable(
            route = "trocar_senha/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            TelaTrocarSenha(navController, email)
        }

        // TELA DE NOTIFICACOES
        composable(
            route = "notificacoes/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            TelaNotificacoes(navController, email)
        }

        composable("selecionar_paciente/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            TelaSelecionarPaciente(navController, email)
        }
    }
}