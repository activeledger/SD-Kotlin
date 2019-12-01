package com.agilitysciences.alsdk

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast

import com.agilitysciences.alsdk.databinding.ActivityMainBinding
import com.example.activeledgersdk.ActiveLedgerSDK
import com.example.activeledgersdk.event.Event
import com.example.activeledgersdk.utility.ApiURL
import com.example.activeledgersdk.utility.KeyType


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {


    lateinit var mainActivityViewModel: MainActivityViewModel
    lateinit var spinner: Spinner
    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActiveLedgerSDK.getInstance().initSDK(applicationContext, "http", "testnet-uk.activeledger.io", "5260")

        mainActivityViewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)

        val activityMainBinding= DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        activityMainBinding.lifecycleOwner = this

        activityMainBinding.viewmodel = mainActivityViewModel


        progressBar = findViewById<View>(R.id.progressbar) as ProgressBar
        progressBar!!.visibility = View.INVISIBLE

        initLayout()

        mainActivityViewModel.getShowToast().observe(this, Observer { message -> showToast(message) })

        progressBar = findViewById<View>(R.id.progressbar) as ProgressBar

        progressBar!!.visibility = View.INVISIBLE

        initLayout()

        //SSE Event
//        mainActivityViewModel.subscribeToEvent("http", "testnet-uk.activeledger.io", "5261", ApiURL.subscribeURL())
//        mainActivityViewModel.eventLiveData.observe(this, Observer { event -> Log.d("SSE", "event -->" + event!!.message) })

    }


    fun initLayout() {

        spinner = findViewById<View>(R.id.keytype_spinner) as Spinner
        val adapter = ArrayAdapter.createFromResource(this,
                R.array.keytype_array, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this


    }


    fun showProgressbar() {
        if (progressBar!!.visibility == View.INVISIBLE) {
            progressBar!!.visibility = View.VISIBLE
        }
    }


    fun hideProgressbar() {
        if (progressBar!!.visibility == View.VISIBLE) {
            progressBar!!.visibility = View.INVISIBLE
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View,
                                pos: Int, id: Long) {

        if (pos == 0) {
            mainActivityViewModel.keyType = KeyType.RSA
        } else {
            mainActivityViewModel.keyType = KeyType.EC
        }

    }

    override fun onNothingSelected(parent: AdapterView<*>) {

    }


    override fun onDestroy() {
        super.onDestroy()
        mainActivityViewModel.activityOnDestroy()
    }


    fun showToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}
