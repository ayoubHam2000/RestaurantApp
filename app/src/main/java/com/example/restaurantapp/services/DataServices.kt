package com.example.restaurantapp.services

import android.graphics.Color
import kotlin.collections.ArrayList

object DataServices {


    var productPriceUnit = "MAD"
    var englishLanguage = true
    var appLanguage = true //french
    var restaurantName = "test"
    var actualFragment = "categories"


    fun setNameLanguage(nameList : ArrayList<String>) : String{
        return if (!appLanguage){
            if(!englishLanguage){
                nameList[0]
            }else{
                if(nameList[1] == ""){
                    nameList[0]
                }else{
                    nameList[1]
                }
            }
        }else{
            nameList[0]
        }

    }

     fun stringToRgb(ColorComponents : String) : Int{
        //[0.23, 0.24, 0.45, 1]
        val StripedColor = ColorComponents.
            replace("[", "").
            replace("]", "").
            replace(",", "")

         val colorArray = StripedColor.split(" ")
         println(colorArray)
         val r = (colorArray[0].toDouble() * 255).toInt()
         val g = (colorArray[1].toDouble() * 255).toInt()
         val b = (colorArray[2].toDouble() * 255).toInt()
        return Color.rgb(r,g,b)
    }

    fun NextContrastColor(position : Int) : String{
        val colors = arrayListOf<String>()
        //look at excel
        colors.add( "[0.156862745, 0.478431373, 0.843137255, 1]" )
        colors.add( "[0.000000000, 0.603921569, 0.396078431, 1]" )
        colors.add( "[0.462745098, 0.156862745, 0.843137255, 1]" )
        colors.add( "[0.223529412, 0.384313725, 0.564705882, 1]" )
        colors.add( "[0.788235294, 0.156862745, 0.705882353, 1]" )
        colors.add( "[1.000000000, 0.635294118, 0.113725490, 1]" )
        colors.add( "[1.000000000, 0.113725490, 0.466666667, 1]" )
        colors.add( "[0.466666667, 0.113725490, 1.000000000, 1]" )
        colors.add( "[0.031372549, 0.705882353, 0.729411765, 1]" )
        colors.add( "[0.137254902, 0.462745098, 0.509803922, 1]" )
        colors.add( "[0.207843137, 0.564705882, 0.082352941, 1]" )
        colors.add( "[0.850980392, 0.301960784, 0.211764706, 1]" )

        return (colors[(position + 1) % colors.size])

    }

/*
    fun languageArray(name : String) : Int {

        val nameArray =  setAppropriateId(name)

        return when (appLanguage) {
            "English" -> {
                nameArray[0]
            }
            "French" -> {
                nameArray[1]
            }
            "Arabic" -> {
                nameArray[2]
            }
            else -> {
                nameArray[0]
            }
        }
    }*/
/*
    fun setAppropriateId(name : String) : ArrayList<Int> {

        return when (name){
            "product post input" -> {
                arrayListOf(R.array.product_input_english, R.array.product_input_french,  R.array.product_input_arabic)
            }
            "product detail input" -> {
                arrayListOf(R.array.product_detail_input_english, R.array.product_detail_input_french,  R.array.product_detail_input_arabic)
            }
            else -> {
                arrayListOf(R.array.default_english, R.array.default_french,  R.array.default_arabic)
            }
        }


    }*/


    /*###################################################################*/
    /*###################################################################*/
    /*#######################--test--------------########################*/
    /*###################################################################*/
    /*###################################################################*/


    /*
    fun GenerateRandomColor() : String {

        val random = Random()
        val r = random.nextInt(255)
        val g = random.nextInt(255)
        val b = random.nextInt(255)

        val sevedR = r.toDouble() / 255
        val sevedG = g.toDouble() / 255
        val sevedB = b.toDouble() / 255
        val avatarColor = "[$sevedR, $sevedG, $sevedB, 1]"
        return avatarColor
    }*/

}