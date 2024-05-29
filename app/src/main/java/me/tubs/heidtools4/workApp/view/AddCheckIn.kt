@file:OptIn(ExperimentalFoundationApi::class)

package me.tubs.heidtools4.workApp.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import me.tubs.heidtools4.workApp.models.Checkin
import me.tubs.heidtools4.workApp.models.CheckinViewModel
import me.tubs.heidtools4.workApp.navStates
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import me.tubs.heidtools4.workApp.view.addCheckin.AddCheckIn

@Preview
@Composable
private fun Preview(){
    AddCheckIn(navStates(rememberNavController()),null).View(
        CheckinViewModel(Checkin())
    )
}

