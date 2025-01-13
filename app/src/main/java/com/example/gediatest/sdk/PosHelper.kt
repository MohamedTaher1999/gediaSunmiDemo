package com.example.gediatest.sdk

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import com.sunmi.pay.hardware.aidl.AidlConstants
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2
import com.sunmi.peripheral.printer.InnerPrinterCallback
import com.sunmi.peripheral.printer.InnerPrinterManager
import com.sunmi.peripheral.printer.InnerResultCallback
import com.sunmi.peripheral.printer.SunmiPrinterService
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object PosHelper {
    const val TAG = "*********"

  private var sunmiPrinterService: SunmiPrinterService? = null
    private var paxQRResultListener: PosPICCResultListener? = null
    private lateinit var cardSharedPreferences: SharedPreferences
    private val exceptionHelper = CoroutineExceptionHandler { coroutineContext, throwable ->
        Log.e(TAG, "exceptionHelper ${throwable.message}")
    }
    private val handler = Handler()

    private val coroutineScope = CoroutineScope(Dispatchers.IO + exceptionHelper)
    private var piccJob: Job? = null
    private val innerPrinterCallback = object : InnerPrinterCallback() {
        override fun onConnected(service: SunmiPrinterService) {
            sunmiPrinterService = service
           // getPrinterStatus()
        }

        override fun onDisconnected() {
            sunmiPrinterService = null
        }
    }

    fun init(context: Context) {
        val result = InnerPrinterManager.getInstance().bindService(context, innerPrinterCallback)
        if (!result) {
            Toast.makeText(context, "Failed to connect to printer service", Toast.LENGTH_SHORT)
                .show()
        }
    }
    private fun startPiccJob(delay: Long? = null) {
        piccJob = coroutineScope.launch {
            Log.d(TAG, "start picc job")
            delay?.let { delay(it) }

        }
    }


//    fun unbindService() {
//        InnerPrinterManager.getInstance().unBindService(context, innerPrinterCallback)
//    }

    fun startListenTOPiccCard(paxQRResultListenerVal: PosPICCResultListener) {
        paxQRResultListener = paxQRResultListenerVal;
        checkCard()

    }

    fun setcardSharedPreferences(cardSharedPreferences: SharedPreferences) {
        PosHelper.cardSharedPreferences = cardSharedPreferences
    }


    fun stopListenToPiccCard(onStop: (() -> Unit)?=null) {
        paxQRResultListener = null
        stopCard()
        stopPiccJob(onStop)
    }
    private fun stopPiccJob(onStop: (() -> Unit)? = null) {
        piccJob?.cancel()
        piccJob?.invokeOnCompletion {
            onStop?.invoke()
        }
    }
//    fun getSN(): String {
//        var serial: String? = null
//        val c = Class.forName("android.os.SystemProperties")
//        val get = c.getMethod("get", String::class.java)
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            try {
//                serial = get.invoke(c, "ro.sunmi.serial") as String
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            try {
//                serial = Build.getSerial()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        } else {
//            try {
//                serial = get.invoke(c, "ro.serialno") as String
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//        // binding.tvText2?.text = serial
//        return serial.toString()
//    }

    fun closeSettingsAndBack(){
        setNavigationBarVisibility(AidlConstants.SystemUI.HIDE_NAV_BAR)
        setStatusBarDropDownMode(AidlConstants.SystemUI.DISABLE_STATUS_BAR_DROP_DOWN)
    }

    fun showSettingsAndBack(){
        setNavigationBarVisibility(AidlConstants.SystemUI.SHOW_NAV_BAR)
        setStatusBarDropDownMode(AidlConstants.SystemUI.ENABLE_STATUS_BAR_DROP_DOWN)

    }
    private fun setNavigationBarVisibility(key: Int) {
        try {
            PosSDK.basicOptV2?.setNavigationBarVisibility(key)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun setStatusBarDropDownMode(key: Int) {
        try {
            PosSDK.basicOptV2?.setStatusBarDropDownMode(key)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    fun getPrinterStatus(): String {
        val status = sunmiPrinterService?.updatePrinterState() ?: -1
        val statusMessage = when (status) {
            1 -> "Printer is under normal operation"
            2 -> "Printer is under preparation"
            3 -> "Communication is abnormal"
            4 -> "Out of paper"
            5 -> "Printer is overheated"
            6 -> "Cover is open"
            7 -> "Cutter error"
            8 -> "Cutter recovered"
            9 -> "Black mark not detected"
            505 -> "Printer not detected"
            507 -> "Printer firmware update failed"
            else -> "Unknown status"
        }
        //Toast.makeText(context, "Printer Status: $statusMessage", Toast.LENGTH_LONG).show()
        return statusMessage
    }

    fun printBitmap(bitmap: Bitmap,printingAction: (PrinterUtils.PrintingStatus) -> Unit) {
        stopCard()
        try {
            if (PosSDK.sunmiPrinterService == null) {
                return
            }
            PosSDK.sunmiPrinterService!!.enterPrinterBuffer(true)
            PosSDK.sunmiPrinterService!!.printBitmap(
                bitmap,
                object : InnerResultCallback() {
                    @Throws(RemoteException::class)
                    override fun onRunResult(isSuccess: Boolean) {
                    }

                    @Throws(RemoteException::class)
                    override fun onReturnString(result: String) {

                    }

                    @Throws(RemoteException::class)
                    override fun onRaiseException(code: Int, msg: String) {

                    }

                    @Throws(RemoteException::class)
                    override fun onPrintResult(code: Int, msg: String) {

                    }
                })
            PosSDK.sunmiPrinterService!!.lineWrap(4, null)
            PosSDK.sunmiPrinterService!!.exitPrinterBuffer(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        printingAction(PrinterUtils.PrintingStatus.Success)

        delay()
    }


    private val mCheckCardCallback: CheckCardCallbackV2 = object : CheckCardCallbackV2Wrapper() {
        @Throws(RemoteException::class)
        override fun findMagCard(info: Bundle) {
            print("findMagCard")
        }

        @Throws(RemoteException::class)
        override fun findICCardEx(info: Bundle) {
            //    addEndTime("checkCard()");
            // LogUtil.e(Constant.TAG, "findICCard:" + Utility.bundle2String(info))
            //  showSpendTime();
            print("findMagCard")
        }

        @Throws(RemoteException::class)
        override fun findRFCardEx(info: Bundle) {
//            printBitmap(bitmap!!)
            val uid = info.getString("uuid")
            paxQRResultListener?.onScanResult("CARD ${info.getString("uuid")}")
        }

        @Throws(RemoteException::class)
        override fun onErrorEx(info: Bundle) {
            delay()
        }
    }

    private fun checkCard() {
        try {
            val cardType =
                CardType.NFC.value or CardType.MIFARE.value or CardType.FELICA.value or CardType.ISO15693.value
            PosSDK.readCardOptV2?.checkCard(cardType, mCheckCardCallback, 90000)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun stopCard() {
        handler.removeCallbacksAndMessages(null)
        try {
            PosSDK.readCardOptV2?.cardOff(CardType.NFC.value)
            PosSDK.readCardOptV2?.cancelCheckCard()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun delay() {
        handler.post(Runnable {

            handler.postDelayed(Runnable { this.checkCard() }, 5000)

        })
    }
    fun saveCardTime(balance: Double) {

    }
    private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

    private fun ByteArray.toHex(): String {
        val result = StringBuffer()

        forEach {
            val octet = it.toInt()
            val firstIndex = (octet and 0xF0).ushr(4)
            val secondIndex = octet and 0x0F
            result.append(HEX_CHARS[firstIndex])
            result.append(HEX_CHARS[secondIndex])
        }

        return result.toString()
    }
    fun shouldPay(): Boolean {
//        val cardID = currentCard.serialInfo.toHex()
//        if (!cardSharedPreferences.contains(cardID)) return true

//        val transactionTime = cardSharedPreferences.getLong(cardID, -1)
//        val currentTimeLong = System.currentTimeMillis()

        return true
    }
    fun removeCard() {

    }
}