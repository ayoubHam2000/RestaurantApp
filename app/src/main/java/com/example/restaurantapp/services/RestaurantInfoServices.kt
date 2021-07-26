package com.example.restaurantapp.services

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.restaurantapp.Structure.restaurantInfo
import com.example.restaurantapp.Utilities.RESTAURANTINFOURL
import com.example.restaurantapp.Utilities.RESTAURANT_ID
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object RestaurantInfoServices {

    lateinit var restaurantInfo : restaurantInfo


    /*###################################################################*/
    /*###################################################################*/
    /*####################--get     restaurantInfo--#####################*/
    /*###################################################################*/
    /*###################################################################*/

    fun getRestaurantInfo(context : Context, complete : (Boolean) -> Unit){

        val url = "${RESTAURANTINFOURL}${RESTAURANT_ID}"

        val jsonObjectRequest  = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                try{
                    Log.d("STATUE", "getting Restaurant Info ...")
                    println(url)

                    val restaurantInfoId = response.getString("_id")
                    val restaurantName = response.getString("name")
                    val english = response.getString("english")
                    val restaurantInfoPriceUnit = response.getString("priceUnit")

                    val newRestaurantInfo = restaurantInfo(restaurantInfoId,restaurantName,
                        english, restaurantInfoPriceUnit)
                    restaurantInfo = newRestaurantInfo
                    Log.d("STATUE", "Success")
                    complete(true)
                }
                catch (e : JSONException){
                    Log.d("JSON", "ERROR ${e.localizedMessage}")
                    complete(false)
                }

            }, Response.ErrorListener { error ->
                Log.d("ERROR", "ERROR $error")
                complete(false)
            })
        Volley.newRequestQueue(context).add(jsonObjectRequest)
    }

    /*###################################################################*/
    /*###################################################################*/
    /*####################--patch   restaurantInfo--#####################*/
    /*###################################################################*/
    /*###################################################################*/


    fun patchRestaurantInfo(context : Context, restaurantInfo : restaurantInfo, complete : (Boolean) -> Unit ){

        val url = "${RESTAURANTINFOURL}${RESTAURANT_ID}"

        val restaurantJSON = makeRestaurantJSONBody(restaurantInfo)
        val requestBody = restaurantJSON.toString()

        val sendData = object : JsonObjectRequest(Method.PATCH, url, null, Response.Listener {
            try{
                Log.d("STATUE", "Patch restaurantInfo")
                complete(true)
                Log.d("STATUE", "Success")
            }catch (e : JSONException){
                Log.d("JSONERROR", "ERROR : ${e.localizedMessage}")
                complete(false)
            }
        }, Response.ErrorListener { error ->
            Log.d("ERROR", "ERROR $error")
            complete(false)
        }){
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        Volley.newRequestQueue(context).add(sendData)
    }

    private fun makeRestaurantJSONBody(newRestaurantInfo : restaurantInfo) : JSONObject{

        val restaurantJsonInfo = JSONObject()

        if(newRestaurantInfo.name != ""){
            restaurantJsonInfo.put("name", newRestaurantInfo.name)
        }
        if(newRestaurantInfo.english != ""){
            restaurantJsonInfo.put("english", newRestaurantInfo.english)
        }
        if(newRestaurantInfo.priceUnit != ""){
            restaurantJsonInfo.put("priceUnit", newRestaurantInfo.priceUnit)
        }

        return restaurantJsonInfo
    }
}