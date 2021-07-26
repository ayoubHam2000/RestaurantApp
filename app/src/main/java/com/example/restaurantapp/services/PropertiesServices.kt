package com.example.restaurantapp.services

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.restaurantapp.Structure.property
import com.example.restaurantapp.Utilities.PROPERTY_URL
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


object PropertiesServices {

    val propertyDetails = ArrayList<ArrayList<String>>()
    var actualPropertyId = ""
    var actualPropertyName = ""


    /*###################################################################*/
    /*###################################################################*/
    /*###########--Get all details of an property ---####################*/
    /*###################################################################*/
    /*###################################################################*/


    fun getPropertyDetails(context : Context, id : String, complete : (Boolean) -> Unit){

        //id is property id
        val url = "${PROPERTY_URL}${id}/details"

        val jsonArrayRequest  = JsonArrayRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                try{
                    Log.d("STATUE", "Getting property details ...")
                    Log.d("JSON", "$response")

                    propertyDetails.clear()
                    for (x in 0 until response.length()){
                        val subJsonDetail = response.getJSONArray(x)
                        propertyDetails.add(arrayListOf())
                        for (y in 0 until subJsonDetail.length()){
                            propertyDetails[x].add(subJsonDetail.getString(y))
                        }
                    }
                    Log.d("STATUE", "Getting SUCCESS")
                    actualPropertyId = id
                    actualPropertyName = DataServices.setNameLanguage(ProductsServices.productProperties[findPropertyId(id)].name)
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
        Volley.newRequestQueue(context).add(jsonArrayRequest)
    }

    /*###################################################################*/
    /*###################################################################*/
    /*#######################--post a property--#########################*/
    /*###################################################################*/
    /*###################################################################*/

    fun postProperty(context : Context , theProperty : property , complete : (Boolean) -> Unit ){

        val propertyJSON = makePropertyJsonObject(theProperty, true)
        val requestBody = propertyJSON.toString()

        val url = "${PROPERTY_URL}${ProductsServices.productId}"

        val sendData = object : JsonObjectRequest(Method.POST, url, null, Response.Listener { response ->
            try{
                Log.d("STATUE", "Posting Product...")
                actualPropertyId = response.getString("propertyId")
                complete(true)
                Log.d("STATUE", "Posting Success")
            }catch (e : JSONException){
                Log.d("JsonERROR", "ERROR : ${e.localizedMessage}")
                complete(false)
            }
        }, Response.ErrorListener {error ->
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

    private fun makePropertyJsonObject(theProperty : property, post : Boolean) : JSONObject{

        val propertyJSON = JSONObject()

        val propertyName = JSONArray()
        for(item in theProperty.name){
            propertyName.put(item)
        }
        propertyJSON.put("name", propertyName)
        propertyJSON.put("require", theProperty.require)
        propertyJSON.put("max", theProperty.max)
        propertyJSON.put("min", theProperty.min)

        if(post){
            val detailJson = JSONArray()
            propertyJSON.put("details",detailJson)
        }
        return propertyJSON
    }

    /*###################################################################*/
    /*###################################################################*/
    /*######################--patch a property--#########################*/
    /*###################################################################*/
    /*###################################################################*/


    fun patchProperty(context : Context , theProperty : property ,  complete : (Boolean) -> Unit ){

        val url = "${PROPERTY_URL}${theProperty.id}"
        val propertyJSON = makePropertyJsonObject(theProperty, false)
        val requestBody = propertyJSON.toString()


        val sendData = object : JsonObjectRequest(Method.PATCH, url, null, Response.Listener { response ->
            try{
                Log.d("STATUE", "patching property ...")
                complete(true)
                Log.d("STATUE", "Patching Success")
            }catch (e : JSONException){
                Log.d("JsonERROR", "ERROR : ${e.localizedMessage}")
                complete(false)
            }
        }, Response.ErrorListener {error ->
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

    /*###################################################################*/
    /*###################################################################*/
    /*######################--patch a detail--###########################*/
    /*###################################################################*/
    /*###################################################################*/

    fun patchDetail(context : Context , details : ArrayList<ArrayList<String>> ,  complete : (Boolean) -> Unit ){

        val url = "${PROPERTY_URL}${actualPropertyId}"
        val propertyJSON = JSONObject()

        val detailJson = JSONArray()
        for(element in details){
            val sunDetailJson = JSONArray()
            for (item in element){
                sunDetailJson.put(item)
            }
            detailJson.put(sunDetailJson)
        }

        propertyJSON.put("details",detailJson)

        val requestBody = propertyJSON.toString()

        val sendData = object : JsonObjectRequest(Method.PATCH, url, null, Response.Listener { response ->
            try{
                Log.d("STATUE", "patching detail ...")
                Log.d("JSON", "$response")
                complete(true)
                Log.d("STATUE", "Patching Success")
            }catch (e : JSONException){
                Log.d("JsonERROR", "ERROR : ${e.localizedMessage}")
                complete(false)
            }
        }, Response.ErrorListener {error ->
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

    /*###################################################################*/
    /*###################################################################*/
    /*#######################--Delete a product--########################*/
    /*###################################################################*/
    /*###################################################################*/



    fun deleteProperty(context : Context, id : String, complete : (Boolean) -> Unit){

        val url = "${PROPERTY_URL}${ProductsServices.productId}/${id}"

        val jsonObjectRequest  = JsonObjectRequest(
            Request.Method.DELETE, url, null,
            Response.Listener { response ->
                try{
                    actualPropertyId =  getPreviousId(id)
                    Log.d("STATUE", "Deleting property ...")
                    Log.d("JSON", "$response")
                    complete(true)
                    Log.d("STATUE", "Deleting Success")
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

    private fun getPreviousId(id : String) : String{

        for(x in 0 until ProductsServices.productProperties.size){
            val item = ProductsServices.productProperties[x]
            if(item.id == id){
                return if(x > 0){
                    ProductsServices.productProperties[x - 1].id
                }else{
                    ""
                }
            }
        }
        return ""
    }

    private fun findPropertyId(id : String) : Int{

        for(x in 0 until ProductsServices.productProperties.size){
            val item = ProductsServices.productProperties[x]
            if(item.id == id){
                return x
            }
        }
        return -1
    }

}