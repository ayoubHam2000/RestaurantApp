package com.example.restaurantapp.services

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Request.Method.PATCH
import com.android.volley.Request.Method.POST
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.restaurantapp.Structure.FileDataPart
import com.example.restaurantapp.Structure.product
import com.example.restaurantapp.Structure.property
import com.example.restaurantapp.Utilities.*
import com.example.restaurantapp.helper.VolleyFileUploadRequest
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream


object ProductsServices {

    // get all products and put it products arrayList
    val productProperties = ArrayList<property>()
    var productId = ""
    var productName2 = ""


    /*###################################################################*/
    /*###################################################################*/
    /*###########--Get all properties of an product--####################*/
    /*###################################################################*/
    /*###################################################################*/


    fun getProductProperties(context : Context, id : String, complete : (Boolean) -> Unit){

        //id is product id
        val url = "${PRODUCTS_URL}${id}/properties"
        productId = id

        val jsonArrayRequest  = JsonArrayRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                try{
                    Log.d("STATUE", "Getting product properties ...")
                    Log.d("JSON", "$response")

                    productProperties.clear()
                    PropertiesServices.propertyDetails.clear()
                    for (x in 0 until response.length()){
                        val theProperty = response.getJSONObject(x)

                        val propertyId = theProperty.getString("_id")

                        //get property name
                        val propertyName = arrayListOf<String>()
                        val propertyJsonName = theProperty.getJSONArray("name")
                        for (x in 0 until  propertyJsonName.length()){
                            propertyName.add(propertyJsonName.getString(x))
                        }

                        val propertyRequire = theProperty.getBoolean("require")
                        val propertyMin = theProperty.getInt("min")
                        val propertyMax = theProperty.getInt("max")

                        val propertyDetails = ArrayList<ArrayList<String>>()
                        val jsonDetail = theProperty.getJSONArray("details")
                        for (x in 0 until jsonDetail.length()){
                            val subJsonDetail = jsonDetail.getJSONArray(x)
                            propertyDetails.add(arrayListOf())
                            for (y in 0 until subJsonDetail.length()){
                                propertyDetails[x].add(subJsonDetail.getString(y))
                            }
                        }
                        val newProperty = property(propertyId, propertyName,propertyRequire,propertyMin,propertyMax, propertyDetails)
                        productProperties.add(newProperty)
                    }
                    if(productProperties.size > 0 && PropertiesServices.actualPropertyId == ""){
                        PropertiesServices.actualPropertyId = productProperties[0].id
                        PropertiesServices.actualPropertyName = DataServices.setNameLanguage(productProperties[0].name)
                        for(item in productProperties[0].details){
                            PropertiesServices.propertyDetails.add(item)
                            println("1")
                        }
                    }
                    Log.d("STATUE", "Getting SUCCESS")
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
    /*#######################--post a product--##########################*/
    /*###################################################################*/
    /*###################################################################*/

    fun postProduct(context : Context,theProduct : product, image : Bitmap, complete : (Boolean, String) -> Unit) {

        val url = "${PRODUCT_IMAGE_URL}${CategoriesServices.categoryId}"
        val imageData = getBytesFromBitmap(image)

        val request = object : VolleyFileUploadRequest(POST, url,
            Response.Listener {response ->
                try{
                    Log.d("STATUE", "Posting Product...")
                    Log.d("DATA", "${response.data}")

                    val jsonProductId = getJsonDataOfProduct(String(response.data))

                    Log.d("STATUE", "Posting Success")
                    complete(true, jsonProductId)
                }catch (e : VolleyError){
                    Log.d("ERROR", "error is ---> $e")
                    complete(false, "")
                }
            },
            Response.ErrorListener {error ->
                Log.d("JsonERROR", "ERROR : $error")
                complete(false, "")
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val p = mutableMapOf<String, String>()

                //productName
                p["frenchName"] = theProduct.productName[0]
                p["englishName"] = theProduct.productName[1]
                //productDetail
                p["frenchDetail"] = theProduct.productDetails[0]
                p["englishDetail"] = theProduct.productDetails[1]
                //product price and discount
                p["price"] = theProduct.productPrice.toString()
                p["discount"] = theProduct.productDiscount.toString()
                //restaurantId (the name of folder in the site)
                p["restaurantId"] = RESTAURANT_ID

                return p
            }
            override fun getByteData(): MutableMap<String, FileDataPart> {
                val params = HashMap<String, FileDataPart>()
                params["productImage"] = FileDataPart("${RESTAURANT_ID}.jpg", imageData!!, "jpeg")
                return params
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
            20000,
            0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(context).add(request)
    }




    /*###################################################################*/
    /*###################################################################*/
    /*#######################--patch a product--##########################*/
    /*###################################################################*/
    /*###################################################################*/


    fun patchProductWithOutImage(context : Context, theProduct : product, complete : (Boolean) -> Unit){

        val url = "${PRODUCTS_URL}${theProduct.id}"
        val productJson = makeProductJsonObject(theProduct)
        val requestBody = productJson.toString()

        val request = object : JsonObjectRequest(PATCH, url, null, Response.Listener {response ->
            Log.d("STATUE", "Patching Product...")
            Log.d("STATUE", "Posting Success")
            complete(true)
        }, Response.ErrorListener {error ->
            Log.d("JsonERROR", "ERROR : $error")
            complete(false)
        }){
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        Volley.newRequestQueue(context).add(request)
    }

    fun patchProductWithImage(context : Context , theProduct : product , image : Bitmap, complete : (Boolean, String) -> Unit ){

        val url = "${PRODUCT_IMAGE_URL}${theProduct.id}"
        val imageData = getBytesFromBitmap(image)

        val request = object : VolleyFileUploadRequest(PATCH, url,
            Response.Listener {response ->
                try{
                    Log.d("STATUE", "Patching Product...")
                    Log.d("DATA", "${response.data}")

                    val jsonProductId = getJsonDataOfProduct(String(response.data))

                    Log.d("STATUE", "Posting Success")
                    complete(true, jsonProductId)
                }catch (e : VolleyError){
                    Log.d("ERROR", "error is ---> $e")
                    complete(false, "")
                }
            },
            Response.ErrorListener {error ->
                Log.d("JsonERROR", "ERROR : $error")
                complete(false, "")
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val p = mutableMapOf<String, String>()

                //productName
                p["frenchName"] = theProduct.productName[0]
                p["englishName"] = theProduct.productName[1]
                //productDetail
                p["frenchDetail"] = theProduct.productDetails[0]
                p["englishDetail"] = theProduct.productDetails[1]
                //product price and discount
                p["price"] = theProduct.productPrice.toString()
                p["discount"] = theProduct.productDiscount.toString()
                //restaurantId (the name of folder in the site)
                p["restaurantId"] = RESTAURANT_ID

                return p
            }
            override fun getByteData(): MutableMap<String, FileDataPart> {
                val params = HashMap<String, FileDataPart>()
                params["productImage"] = FileDataPart("${RESTAURANT_ID}.jpg", imageData!!, "jpeg")
                return params
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
            20000,
            0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(context).add(request)
    }


    /*###################################################################*/
    /*###################################################################*/
    /*#########--functions belong to post and patch--####################*/
    /*###################################################################*/
    /*###################################################################*/

    private fun getJsonDataOfProduct(data : String) : String{
        //data {"result" : "16574fsd4fs68gf2"}

        val arrayData = data.split(":")
        if(arrayData.size > 1){
            println("----->${arrayData[1]}")
            println("-----> ${arrayData[1].substring(1, arrayData[1].length - 2)}")
            return arrayData[1].substring(1, arrayData[1].length - 2)
        }
        return ""
    }

    private fun getBytesFromBitmap(bitmap: Bitmap): ByteArray? {
        val stream = ByteArrayOutputStream()
        val dataSize = bitmap.rowBytes * bitmap.height
        when {
            dataSize > HUGE_IMAGE -> {
                bitmap.compress(Bitmap.CompressFormat.JPEG, HUGE_COMPRESS, stream)
            }
            dataSize > BIG_IMAGE -> {
                bitmap.compress(Bitmap.CompressFormat.JPEG, HEIGHT_COMPRESS, stream)
            }
            else -> {
                bitmap.compress(Bitmap.CompressFormat.JPEG, LOW_COMPRESS, stream)
            }
        }
        return stream.toByteArray()
    }

    private fun makeProductJsonObject(theProduct : product) : JSONObject{
        val productJason = JSONObject()

        val productName = JSONArray()
        productName.put(theProduct.productName[0])
        productName.put(theProduct.productName[1])

        val productDetail = JSONArray()
        productDetail.put(theProduct.productDetails[0])
        productDetail.put(theProduct.productDetails[1])

        productJason.put("name", productName)
        productJason.put("details", productDetail)
        productJason.put("price", theProduct.productPrice)
        productJason.put("discount", theProduct.productDiscount)

        return productJason
    }

    /*###################################################################*/
    /*###################################################################*/
    /*#######################--Delete a product--########################*/
    /*###################################################################*/
    /*###################################################################*/



    fun deleteProduct(context : Context, id : String, complete : (Boolean) -> Unit){

        val url = "${PRODUCTS_URL}${CategoriesServices.categoryId}/${id}"

        val jsonObjectRequest  = JsonObjectRequest(
            Request.Method.DELETE, url, null,
            Response.Listener { response ->
                try{

                    Log.d("STATUE", "Deleting product ...")
                    Log.d("JSON", "$response")
                    productId = response.getString("productId")
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

}

