package com.example.gediatest.sdk

interface PosPICCResultListener {
    fun onScanResult(UID:String)
    fun onScanError(error:String)
}