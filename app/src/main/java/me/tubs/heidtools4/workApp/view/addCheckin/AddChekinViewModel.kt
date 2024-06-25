package me.tubs.heidtools4.workApp.view.addCheckin

import androidx.compose.runtime.FloatState
import androidx.compose.runtime.asFloatState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import me.tubs.heidtools4.workApp.models.Checkin


class AddChekinViewModel(): ViewModel() {
    private val _uiState = MutableStateFlow(Checkin())
    val uiState: StateFlow<Checkin> = _uiState.asStateFlow()

    private var _fuelLevel = mutableFloatStateOf(0f)
    val fuelLevel: FloatState = _fuelLevel.asFloatState()

    fun initData(checkin: Checkin){
        _uiState.value = checkin
        _fuelLevel.floatValue = checkin.fuelLevel-8f
    }
    fun setFuelLevel(level:Int){
        _fuelLevel.floatValue = level.toFloat()
        _uiState.value = uiState.value.copy(fuelLevel = 8-level)
    }
    fun setPhotoPath(path:String){
        _uiState.update { it ->
            it.apply {
                this.photoPath = path
            }
        }
    }

    fun setSerial(it: String) {
        _uiState.value = uiState.value.copy(serial = it)
    }

    fun setOdometer(it: Int) {
        _uiState.value = uiState.value.copy(km = it)
    }

}