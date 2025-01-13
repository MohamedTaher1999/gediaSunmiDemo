package com.example.gediatest.sdk

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import com.example.gediatest.R
import com.example.gediatest.sdk.PosHelper.printBitmap
import com.example.gediatest.image.Cekrek

object PrinterUtils {

    var sunmiHelper: SunmiPrinterHelper? = null



    fun getLayoutBitmap(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) bgDrawable.draw(canvas)
        else canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return returnedBitmap
    }

    @SuppressLint("StringFormatInvalid")
    fun Context.printTransaction(

        printingAction: (PrintingStatus) -> Unit,
    ) {
        printingAction(PrintingStatus.Printing)

        val view = LayoutInflater.from(this).inflate(R.layout.transaction, null, false).apply {


        }
        printBitmap(Cekrek.toBitmap(view),printingAction)
    }





    fun print(view: View, onSuccess: () -> Unit, onError: () -> Unit) {

    }


    sealed interface PrintingStatus {
        object Printing : PrintingStatus
        object Success : PrintingStatus
        data class Failed(val error: String) : PrintingStatus
    }

}

fun String.toArabic(): String {
    return this
        .replace("1".toRegex(), "١").replace("2".toRegex(), "٢")
        .replace("3".toRegex(), "٣").replace("4".toRegex(), "٤")
        .replace("5".toRegex(), "٥").replace("6".toRegex(), "٦")
        .replace("7".toRegex(), "٧").replace("8".toRegex(), "٨")
        .replace("9".toRegex(), "٩").replace("0".toRegex(), "٠")
}

