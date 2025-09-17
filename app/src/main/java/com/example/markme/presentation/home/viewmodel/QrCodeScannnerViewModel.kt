package com.example.markme.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.markme.domain.repository.AttendanceRepository
import com.example.markme.presentation.home.ScanResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class QrCodeScannerViewModel @Inject constructor(private val repository: AttendanceRepository): ViewModel() {

    private val _scanResult = MutableStateFlow<ScanResult>(ScanResult.Idle)
    val scanResult: StateFlow<ScanResult> = _scanResult.asStateFlow()

    fun processQrCode(qrCode: String){
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val user = repository.getUserByQrCode(qrCode)
                if (user != null){
                    val success = repository.markAttendance(user.id,user.fullName)
                    if (success){
                        _scanResult.value = ScanResult.Success("Attendance marked for ${user.fullName}")
                    } else {
                        _scanResult.value = ScanResult.AlreadyMarked("Attendance already marked for ${user.fullName}")
                    }
                } else{
                _scanResult.value = ScanResult.Error("Invalid QR Code")
                }
            }catch (e: Exception){
                _scanResult.value = ScanResult.Error(e.message ?: "An error occurred")
            }

        }
    }

    fun resetScanResult(){
        _scanResult.value = ScanResult.Idle
    }
}