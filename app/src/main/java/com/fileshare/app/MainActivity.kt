package com.fileshare.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fileshare.app.ui.navigation.Screen
import com.fileshare.app.ui.screens.add.AddEditDocumentScreen
import com.fileshare.app.ui.screens.detail.DocumentDetailScreen
import com.fileshare.app.ui.screens.lock.LockScreen
import com.fileshare.app.ui.screens.main.MainScreen
import com.fileshare.app.ui.screens.settings.CategoryManagementScreen
import com.fileshare.app.ui.screens.settings.SettingsScreen
import com.fileshare.app.ui.theme.FileShareAppTheme
import com.fileshare.app.viewmodel.CategoryViewModel
import com.fileshare.app.viewmodel.DocumentViewModel
import com.fileshare.app.viewmodel.LockViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            FileShareAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val documentViewModel: DocumentViewModel = viewModel()
                    val categoryViewModel: CategoryViewModel = viewModel()
                    val lockViewModel: LockViewModel = viewModel()
                    
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Main.route
                    ) {
                        composable(Screen.Lock.route) {
                            LockScreen(
                                lockViewModel = lockViewModel,
                                onUnlocked = {
                                    navController.navigate(Screen.Main.route) {
                                        popUpTo(Screen.Lock.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                        
                        composable(Screen.Main.route) {
                            MainScreen(
                                documentViewModel = documentViewModel,
                                categoryViewModel = categoryViewModel,
                                onNavigateToDetail = { documentId ->
                                    navController.navigate(Screen.DocumentDetail.createRoute(documentId))
                                },
                                onNavigateToAdd = {
                                    navController.navigate(Screen.AddEditDocument.createRoute())
                                },
                                onNavigateToSettings = {
                                    navController.navigate(Screen.Settings.route)
                                }
                            )
                        }
                        
                        composable(Screen.DocumentDetail.route) { backStackEntry ->
                            val documentId = backStackEntry.arguments?.getString("documentId")?.toLongOrNull()
                            if (documentId != null) {
                                DocumentDetailScreen(
                                    documentId = documentId,
                                    documentViewModel = documentViewModel,
                                    categoryViewModel = categoryViewModel,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToEdit = { id ->
                                        navController.navigate(Screen.AddEditDocument.createRoute(id))
                                    }
                                )
                            }
                        }
                        
                        composable(Screen.AddEditDocument.route) { backStackEntry ->
                            val documentId = backStackEntry.arguments?.getString("documentId")?.toLongOrNull()
                            AddEditDocumentScreen(
                                documentId = documentId,
                                documentViewModel = documentViewModel,
                                categoryViewModel = categoryViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                lockViewModel = lockViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToCategoryManagement = {
                                    navController.navigate(Screen.CategoryManagement.route)
                                }
                            )
                        }
                        
                        composable(Screen.CategoryManagement.route) {
                            CategoryManagementScreen(
                                categoryViewModel = categoryViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
