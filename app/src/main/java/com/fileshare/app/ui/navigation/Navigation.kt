package com.fileshare.app.ui.navigation

sealed class Screen(val route: String) {
    object Lock : Screen("lock")
    object Main : Screen("main")
    object DocumentDetail : Screen("document_detail/{documentId}") {
        fun createRoute(documentId: Long) = "document_detail/$documentId"
    }
    object AddEditDocument : Screen("add_edit_document?documentId={documentId}") {
        fun createRoute(documentId: Long? = null) = 
            if (documentId != null) "add_edit_document?documentId=$documentId"
            else "add_edit_document"
    }
    object Settings : Screen("settings")
    object CategoryManagement : Screen("category_management")
    object PinSetup : Screen("pin_setup")
}
