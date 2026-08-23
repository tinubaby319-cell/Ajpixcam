package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

object AppRoutes {
    const val CAMERA = "camera"
    const val GALLERY = "gallery"
    const val PHOTO_DETAIL = "detail/{photoId}"
    const val PHOTO_EDITOR = "editor/{photoId}"
    const val SETTINGS = "settings"
    const val RECIPE_STUDIO = "recipe_studio"
    const val FILM_LAB = "film_lab"
    const val CONTACT_SHEET = "contact_sheet"

    fun photoDetail(photoId: Long) = "detail/$photoId"
    fun photoEditor(photoId: Long) = "editor/$photoId"
}

@Composable
fun MainNavigation(
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.CAMERA,
        modifier = modifier
    ) {
        composable(AppRoutes.CAMERA) {
            CameraScreen(
                viewModel = viewModel,
                onNavigateToGallery = { navController.navigate(AppRoutes.GALLERY) },
                onNavigateToSettings = { navController.navigate(AppRoutes.SETTINGS) },
                onNavigateToRecipeStudio = { navController.navigate(AppRoutes.RECIPE_STUDIO) },
                onNavigateToFilmLab = { navController.navigate(AppRoutes.FILM_LAB) }
            )
        }

        composable(AppRoutes.GALLERY) {
            GalleryScreen(
                viewModel = viewModel,
                onPhotoSelected = { photoId ->
                    navController.navigate(AppRoutes.photoDetail(photoId))
                },
                onOpenFilmLab = { navController.navigate(AppRoutes.FILM_LAB) },
                onOpenContactSheet = { navController.navigate(AppRoutes.CONTACT_SHEET) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.RECIPE_STUDIO) {
            FilmRecipeStudioScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.FILM_LAB) {
            FilmLabScreen(
                viewModel = viewModel,
                onOpenContactSheetMaker = { navController.navigate(AppRoutes.CONTACT_SHEET) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.CONTACT_SHEET) {
            ContactSheetMakerScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoutes.PHOTO_DETAIL,
            arguments = listOf(navArgument("photoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val photoId = backStackEntry.arguments?.getLong("photoId") ?: 0L
            PhotoDetailScreen(
                photoId = photoId,
                viewModel = viewModel,
                onNavigateToEditor = { id ->
                    navController.navigate(AppRoutes.photoEditor(id))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoutes.PHOTO_EDITOR,
            arguments = listOf(navArgument("photoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val photoId = backStackEntry.arguments?.getLong("photoId") ?: 0L
            PhotoEditorScreen(
                photoId = photoId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
