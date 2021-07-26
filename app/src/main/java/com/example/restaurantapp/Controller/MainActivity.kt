package com.example.restaurantapp.Controller

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.restaurantapp.Fragement.CategoryFragment
import com.example.restaurantapp.Fragement.ProductsFragment
import com.example.restaurantapp.Fragement.SettingFragement
import com.example.restaurantapp.R
import com.example.restaurantapp.Utilities.*
import com.example.restaurantapp.helper.localeLanguageChanger
import com.example.restaurantapp.services.DataServices
import com.example.restaurantapp.services.RestaurantInfoServices
import com.example.restaurantapp.services.Soket
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess



class MainActivity : AppCompatActivity() {

    private var actualItem = "menu"
    private var statusBar = true
    private lateinit var sharedPreferences: SharedPreferences



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        //get Restaurant ID
        //val id : String? = intent.getStringExtra("RestaurantId")
        RESTAURANT_ID = "5e653b7ae504bf58b049a8a9"


        //initialize sharedPreferences in onCreate method after setContent
        sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)

        //functions
        initializeButtons()

        loadRestaurantInfo()

        openCategoryFragment()

        changeTime()

        //disableStatusBar(this)

    }


    /*###################################################################*/
    /*###################################################################*/
    /*##################--       Initial         --######################*/
    /*###################################################################*/
    /*###################################################################*/


    private fun loadRestaurantInfo(){

        RestaurantInfoServices.getRestaurantInfo(this){ success ->
            if(success){

                RestorantName.text = RestaurantInfoServices.restaurantInfo.name
                DataServices.productPriceUnit = RestaurantInfoServices.restaurantInfo.priceUnit
                DataServices.restaurantName = RestaurantInfoServices.restaurantInfo.name
                DataServices.englishLanguage = RestaurantInfoServices.restaurantInfo.english.toBoolean()

            }else{
                makeMessageToast(R.string.server_error)
            }
        }
    }

    private fun openCategoryFragment(){
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment, CategoryFragment())
        ft.commit()
        changeColorItem()
    }

    /*###################################################################*/
    /*###################################################################*/
    /*##################--       Buttons          --#####################*/
    /*###################################################################*/
    /*###################################################################*/

    private fun initializeButtons(){
        settingButtonFragment.setOnClickListener {
            if (actualItem != "setting"){
                actualItem = "setting"
                loadSettingFragment(SettingFragement())
            }

        }
        menuButtonFragment.setOnClickListener {
            // if (actualItem != "menu"){
            actualItem = "menu"
            loadMenuFragment(CategoryFragment())
            // }
        }
        languageItem.setOnClickListener {
            //languageManagement()
            Soket.sendSocket()
        }
    }


    /*###################################################################*/
    /*###################################################################*/
    /*################--       Menu / Setting      --####################*/
    /*###################################################################*/
    /*###################################################################*/

    private fun loadSettingFragment(frag : SettingFragement){
        DataServices.actualFragment = "categories"
        changeColorItem()
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment, frag)
        ft.commit()
    }

    private fun loadMenuFragment(frag : CategoryFragment){
        changeColorItem()
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment, frag)
        ft.commit()
    }

    private fun changeColorItem(){

        if(actualItem == "setting"){
            settingMainColor.setBackgroundColor(ContextCompat.getColor(this, R.color.leftBarColor))
            settingTrensparentColor.setBackgroundColor(ContextCompat.getColor(this, R.color.blackTransparent))

            menuMainColor.setBackgroundColor(ContextCompat.getColor(this, R.color.Transparent))
            menuTransparentColor.setBackgroundColor(ContextCompat.getColor(this, R.color.Transparent))
        }else{
            settingMainColor.setBackgroundColor(ContextCompat.getColor(this, R.color.Transparent))
            settingTrensparentColor.setBackgroundColor(ContextCompat.getColor(this, R.color.Transparent))

            menuMainColor.setBackgroundColor(ContextCompat.getColor(this, R.color.leftBarColor))
            menuTransparentColor.setBackgroundColor(ContextCompat.getColor(this, R.color.blackTransparent))
        }
    }

    /*###################################################################*/
    /*###################################################################*/
    /*################--       Fragment Control       --#################*/
    /*###################################################################*/
    /*###################################################################*/


    var exit = 0
    override fun onBackPressed() {
        when(DataServices.actualFragment){
            "categories" -> {
                exit++
                exit()
            }
            "products" -> {
                toCategories()
                DataServices.actualFragment = "categories"
            }
            "properties" -> {
                toProducts()
                DataServices.actualFragment = "products"
            }
        }
    }

    private fun exit(){
        println(exit)
        if(exit == 1){
            makeMessageToast(R.string.exit)
            exit++
            Handler().postDelayed({
                exit = 0
            }, 1500)

        }else if(exit > 2){
            moveTaskToBack(true)
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(1)
        }
    }

    private fun toCategories(){
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment, CategoryFragment())
        ft.commit()
    }
    private fun toProducts(){
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment, ProductsFragment())
        ft.commit()
    }

    /*###################################################################*/
    /*###################################################################*/
    /*#####################--       Times       --#######################*/
    /*###################################################################*/
    /*###################################################################*/

    private fun changeTime(){

        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                timeDisplay()
                mainHandler.postDelayed(this, 1000)
            }
        })
    }

    private fun timeDisplay(){
        //Mon Feb 24 20:12:27 GMT+01:00 2020
        /*val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        this.sendBroadcast(it)*/
        val currentDate = Date()
        val sdfF = SimpleDateFormat("k-m-E-MMMM-d", Locale.FRENCH)
        val sdfE = SimpleDateFormat("k-m-E-MMMM-d", Locale.ENGLISH)
        val frenchTime = sdfF.format(currentDate)
        val englishTime = sdfE.format(currentDate)


        if(DataServices.appLanguage){
            val currentTime = frenchTime.split("-")
            setTime(currentTime)
        }else{
            val currentTime = englishTime.split("-")
            setTime(currentTime)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun setTime(time : List<String>){
        //20-16-jeu.-February-28
        timeHour.text = time[0]
        timeMinute.text = time[1]
        timeDay.text = time[2]
        timeMonth.text = "${time[3]}  ${time[4]}"
    }

    /*###################################################################*/
    /*###################################################################*/
    /*#############--       change language        --####################*/
    /*###################################################################*/
    /*###################################################################*/

    private fun languageManagement(){
        val builder = AlertDialog.Builder(this)
        val builderView = layoutInflater.inflate(R.layout.language_changer, null)
        builder.setView(builderView).setPositiveButton("ok", null)
        val dialog = builder.create()

        //set up builder show
        dialog.setOnShowListener {

            //resources
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val frenchLanguage = builderView.findViewById<RadioButton>(R.id.frenchLanguage)
            val englishLanguage = builderView.findViewById<RadioButton>(R.id.englishLanguage)

            //get info
            frenchLanguage.isChecked = DataServices.appLanguage
            englishLanguage.isChecked = !DataServices.appLanguage

            frenchLanguage.setOnClickListener { englishLanguage.isChecked = false }
            englishLanguage.setOnClickListener { frenchLanguage.isChecked = false }

            okButton.setOnClickListener {
                DataServices.appLanguage =  frenchLanguage.isChecked
                if(DataServices.appLanguage){
                    changeApplicationLanguage("fr")
                }else{
                    changeApplicationLanguage("en")
                }
                dialog.dismiss()
            }

        }
        dialog.show()
    }

    private fun changeApplicationLanguage(language:String){
        val sharedPreferencesEditor = sharedPreferences.edit()
        when (language) {
            ENGLISH -> sharedPreferencesEditor?.putString(SELECTED_LANGUAGE, ENGLISH)
            FRENCH -> sharedPreferencesEditor?.putString(SELECTED_LANGUAGE, FRENCH)
        }
        sharedPreferencesEditor.putBoolean(LANGUAGE_IS_SELECTED, true)
        sharedPreferencesEditor?.apply()
        recreate()
    }

    override fun attachBaseContext(newBase: Context?) {
        sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(newBase)
        val lang = sharedPreferences.getString(SELECTED_LANGUAGE, "fr")
        super.attachBaseContext(localeLanguageChanger.wrap(newBase!!, lang!!))
    }

    /*###################################################################*/
    /*###################################################################*/
    /*##################--      Methods         --#######################*/
    /*###################################################################*/
    /*###################################################################*/

    private fun makeMessageToast(theMessage : Int){
        val languageArray = resources.getString(theMessage)
        Toast.makeText(this, languageArray, Toast.LENGTH_SHORT).show()
    }

    /*###################################################################*/
    /*###################################################################*/
    /*##################--       Lock         --#########################*/
    /*###################################################################*/
    /*###################################################################*/


    private fun disableStatusBar(context: Context){

        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                setDisableStatusBar()
                mainHandler.postDelayed(this, 10000)
            }
        })
    }

    private fun setDisableStatusBar(){
        val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        this.sendBroadcast(it)
    }

    /*override fun onStop() {
        super.onStop()
        println("on stop")
        if(statusBar){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }*/

}
