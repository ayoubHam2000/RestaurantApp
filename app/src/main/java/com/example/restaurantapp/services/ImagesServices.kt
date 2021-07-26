package com.example.restaurantapp.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import com.android.volley.Request.Method.POST
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.example.restaurantapp.Structure.FileDataPart
import com.example.restaurantapp.Structure.product
import com.example.restaurantapp.Utilities.*
import com.example.restaurantapp.helper.VolleyFileUploadRequest
import org.json.JSONException
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


object ImagesServices {

    var continueDownloading = true

    fun getImageFromUrl(context : Context, productId :  String, imageURL : String, complete : (Boolean) -> Unit){
        val url = context.getExternalFilesDir("productsImages/${productId}.jpg").toString()
        val file = File(url)
        if(file.isDirectory){
            println("image not found start download it")
            file.delete()
            getBitmapFromURL(context, imageURL, productId)
            println("comlete")
            complete(true)
        }
    }


    private fun getBitmapFromURL(context : Context, src: String?, productId : String) {
        try {
            val url = URL(src)
            println(src)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            saveImageToStorage(context, BitmapFactory.decodeStream(input) , productId)
            Log.d("Success", "Success to download image")
        } catch (e: IOException) {
            Log.d("FAILED", "failed to download image")
        }
    }

    private fun saveImageToStorage(context : Context, bitmap: Bitmap, productId : String){
        val externalStorageStats = Environment.getExternalStorageState()
        if(externalStorageStats == Environment.MEDIA_MOUNTED){
            val storageDirectory = context.getExternalFilesDir("productsImages")
            val file = File(storageDirectory, "${productId}.jpg")
            try{
                val stream  = FileOutputStream(file)
                val dataSize = bitmap.rowBytes * bitmap.height
                when {
                    dataSize > HUGE_IMAGE -> {
                        bitmap.compress(CompressFormat.JPEG, HUGE_COMPRESS, stream)
                    }
                    dataSize > BIG_IMAGE -> {
                        bitmap.compress(CompressFormat.JPEG, HEIGHT_COMPRESS, stream)
                    }
                    else -> {
                        bitmap.compress(CompressFormat.JPEG, LOW_COMPRESS, stream)
                    }
                }
                stream.flush()
                stream.close()
            }catch(e : Exception){
                Log.d("IError", "${e.printStackTrace()}")
            }
        }else{
            Log.d("IError", "Enable To Access The Storage")
        }
    }



}