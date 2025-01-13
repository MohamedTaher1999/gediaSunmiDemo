package com.example.gediatest.sdk

import android.annotation.SuppressLint
import android.app.Application
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2
import com.sunmi.pay.hardware.aidlv2.system.BasicOptV2
import com.sunmi.peripheral.printer.InnerPrinterCallback
import com.sunmi.peripheral.printer.InnerPrinterException
import com.sunmi.peripheral.printer.InnerPrinterManager
import com.sunmi.peripheral.printer.SunmiPrinterService
import sunmi.paylib.SunmiPayKernel


object PosSDK {
    //    var selectPrinter: PrinterSdk.Printer? = null
    var readCardOptV2: ReadCardOptV2? = null // 获取读卡模块
    private var connectPaySDK = false //是否已连接PaySDK
    var sunmiPrinterService: SunmiPrinterService? = null // 打印模块
    var basicOptV2: BasicOptV2? = null // 获取基础操作模块

    fun initPosSDK(context: Application, launcherClassName: String? = null) {
        val sunmiPrinterHelper = SunmiPrinterHelper()
        sunmiPrinterHelper.initSunmiPrinterService(context)
        PrinterUtils.sunmiHelper = sunmiPrinterHelper
        bindPaySDKService(context)
        bindPrintService(context)
        PosHelper.init(context)
    }
    private fun bindPaySDKService(context: Application) {
        val payKernel = SunmiPayKernel.getInstance()
        payKernel.initPaySDK(context, object : SunmiPayKernel.ConnectCallback {
            override fun onConnectPaySDK() {
                readCardOptV2 = payKernel.mReadCardOptV2
                basicOptV2 = payKernel.mBasicOptV2
                connectPaySDK = true
                PosHelper.closeSettingsAndBack()
            }

            override fun onDisconnectPaySDK() {
                connectPaySDK = false
                readCardOptV2 = null
            }
        })
    }

    private fun bindPrintService(context: Application) {
        try {
            InnerPrinterManager.getInstance().bindService(context, object : InnerPrinterCallback() {
                override fun onConnected(service: SunmiPrinterService) {
                    sunmiPrinterService = service
                }

                override fun onDisconnected() {
                    sunmiPrinterService = null
                }
            })
        } catch (e: InnerPrinterException) {
            e.printStackTrace()
        }
    }
    private var isLocked = true
    val isLockedScreen: Boolean
        get() = isLocked

    @SuppressLint("HardwareIds")
    //  fun getSerialNumber(): String? = DAL.sys.termInfo[ETermInfoKey.SN]
    fun returnDeviceDefaults(boolean: Boolean) {
        isLocked = boolean
        if(boolean){
            PosHelper.showSettingsAndBack()}
        else
            PosHelper.closeSettingsAndBack()
    }
}
