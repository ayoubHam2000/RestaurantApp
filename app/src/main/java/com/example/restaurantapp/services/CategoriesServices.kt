package com.example.restaurantapp.services

import android.content.Context
import android.util.Log
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.restaurantapp.Structure.category
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.example.restaurantapp.Structure.product
import com.example.restaurantapp.Utilities.CATEGORIES_URL
import com.example.restaurantapp.Utilities.RESTAURANT_ID
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object CategoriesServices {

    val categories = ArrayList<category>()
    val categoryProducts = ArrayList<product>()
    val deletetedProduct = ArrayList<String>()
    var categoryId = ""

    fun getAllCategories(context : Context, complete : (Boolean) -> Unit){

        val url = "${CATEGORIES_URL}${RESTAURANT_ID}"

        val jsonObjectRequest  = JsonArrayRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                try{
                    Log.d("STATUE", "getting All categories ...")
                    categories.clear()
                    Log.d("JSON", "$response")
                    for(y in 0 until response.length()){
                        val categoryItem = response.getJSONObject(y)
                        val categoryId = categoryItem.getString("_id")

                        //get name
                        val categoryName = arrayListOf<String>()
                        val categoryJsonName= categoryItem.getJSONArray("name")
                        for(x in 0 until categoryJsonName.length()){
                            categoryName.add(categoryJsonName.getString(x))
                        }

                        //get products
                        val categoryProducts = ArrayList<String>()
                        val jsonProducts = categoryItem.getJSONArray("products")
                        for(x in 0 until jsonProducts.length()){
                            categoryProducts.add(jsonProducts.getString(x))
                        }

                        val newCategory = category(categoryId, categoryName, categoryProducts)
                        categories.add(newCategory)
                    }
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
    /*#######--get all products of an category by id--###################*/
    /*###################################################################*/
    /*###################################################################*/


    fun getCategoryProducts(context : Context, id : String, complete : (Boolean) -> Unit){

        //id is category id
        val url = "${CATEGORIES_URL}${id}/products"
        categoryId = id

        val jsonArrayRequest  = JsonArrayRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                try{
                    Log.d("STATUE", "getting category products ...")
                    Log.d("JSON", "$response")

                    categoryProducts.clear()
                    for(x in 0 until response.length()){
                        val theProduct = response.getJSONObject(x)

                        val productId = theProduct.getString("_id")

                        //get name
                        val productName = arrayListOf<String>()
                        val productJsonName = theProduct.getJSONArray("name")
                        for(x in 0 until productJsonName.length() ){
                            productName.add(productJsonName.getString(x))
                        }

                        val productPrice = theProduct.getDouble("price")
                        val productDiscount = theProduct.getInt("discount")

                        //get details
                        val productDetails = ArrayList<String>()
                        val productJsonDetails = theProduct.getJSONArray("details")
                        for (x in 0 until  productJsonDetails.length()){
                            productDetails.add(productJsonDetails.getString(x))
                        }

                        //get properties
                        val productProperties = ArrayList<String>()
                        val properties = theProduct.getJSONArray("properties")
                        for (x in 0 until  properties.length()){
                            productProperties.add(properties.getString(x))
                        }
                        val productImageURl = theProduct.getString("imageUrl")

                        //create product and adding it
                        val newProduct = product(productId, productName, productPrice, productDiscount, productDetails, productProperties, productImageURl)
                        categoryProducts.add(newProduct)
                    }
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
        Volley.newRequestQueue(context).add(jsonArrayRequest)
    }

    /*###################################################################*/
    /*###################################################################*/
    /*#######################--post category---##########################*/
    /*###################################################################*/
    /*###################################################################*/


    fun postCategory(context : Context , theCategoryName: ArrayList<String> , complete : (Boolean) -> Unit ){

        val url = "${CATEGORIES_URL}${RESTAURANT_ID}"

        val categoryJSON = makeCategoryJsonBody(theCategoryName)
        val requestBody = categoryJSON.toString()

        val sendData = object : JsonObjectRequest(Method.POST, url, null, Response.Listener { response ->
            try{
                Log.d("STATUE", "Post category")
                Log.d("JSON", "$response")
                complete(true)
                Log.d("STATUE", "Success")
            }catch (e : JSONException){
                Log.d("JSONERROR", "ERROR : ${e.localizedMessage}")
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

    private fun makeCategoryJsonBody(takenCategory : ArrayList<String>) : JSONObject {

        val categoryJSON = JSONObject()
        val categoryName = JSONArray()
        val productsJSON = JSONArray()

        for(item in takenCategory){
            categoryName.put(item)
        }

        categoryJSON.put("name", categoryName)
        categoryJSON.put("products", productsJSON)
        return (categoryJSON)
    }


    /*###################################################################*/
    /*###################################################################*/
    /*#####################--patch a category--##########################*/
    /*###################################################################*/
    /*###################################################################*/

    fun patchCategory(context : Context , theCategory : category ,  complete : (Boolean) -> Unit ){

        val id = theCategory.id
        val url = "${CATEGORIES_URL}${id}"
        val categoryJSON = JSONObject()

        //get category name
        val categoryName = JSONArray()
        for(item in theCategory.name){
            categoryName.put(item)
        }

        categoryJSON.put("name", categoryName)
        val requestBody = categoryJSON.toString()

        val sendData = object : JsonObjectRequest(Method.PATCH, url, null, Response.Listener { response ->
            try{
                Log.d("STATUE", "Patching category")
                Log.d("JSON", "$response")
                complete(true)
                Log.d("STATUE", "Success")
            }catch (e : JSONException){
                Log.d("JSONERROR", "ERROR : ${e.localizedMessage}")
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
    /*#######################--Delete a Category--########################*/
    /*###################################################################*/
    /*###################################################################*/

    fun deleteCategory(context : Context, id : String, complete : (Boolean) -> Unit){

        val url = "${CATEGORIES_URL}${RESTAURANT_ID}/${id}"

        val jsonObjectRequest  = JsonObjectRequest(
            Request.Method.DELETE, url, null,
            Response.Listener { response ->
                try{
                    Log.d("STATUE", "Trying to Delete Category ...")
                    Log.d("JSON", "$response")
                    getDeletedProductId(response.getJSONArray("productsId"))
                    complete(true)
                    Log.d("STATUE", "Success")
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

    private fun getDeletedProductId(theProductsId : JSONArray){
        for(x in 0 until theProductsId.length()){
            deletetedProduct.add(theProductsId.getString(x))
        }
    }

}