package com.adyen.android.assignment.ui.state

sealed class MainAction {
    object ShowPermissionDialog : MainAction()

    object ClickCurrentLocation : MainAction()
}
