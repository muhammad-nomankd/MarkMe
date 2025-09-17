package com.example.markme.presentation.home

sealed class ScanResult {
    object Idle : ScanResult()
    data class Success(val message: String) : ScanResult()
    data class AlreadyMarked(val message: String) : ScanResult()
    data class Error(val message: String) : ScanResult()
}