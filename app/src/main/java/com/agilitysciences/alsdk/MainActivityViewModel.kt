package com.agilitysciences.alsdk

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import android.view.View

import com.example.activeledgersdk.ActiveLedgerSDK
import com.example.activeledgersdk.event.Event
import com.example.activeledgersdk.event.SSEUtil
import com.example.activeledgersdk.model.Territoriality
import com.example.activeledgersdk.utility.KeyType
import com.example.activeledgersdk.utility.PreferenceManager
import com.example.activeledgersdk.utility.Utility

import org.json.JSONException

import java.io.IOException
import java.security.KeyPair

import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class MainActivityViewModel : ViewModel() {


    var eventLiveData: LiveData<Event> = SSEUtil.getInstance().eventLiveData
    private var disposable: Disposable? = null
    var key_Pair: KeyPair? = null
    var keyType = KeyType.RSA
    private var publickey = MutableLiveData<String>()
    private var privatekey = MutableLiveData<String>()
    var keyname: String? = null
    private var onBoardId = MutableLiveData<String>()
    private var onBoardName = MutableLiveData<String>()
    private var showToast: MutableLiveData<String> = MutableLiveData()

    fun generateKeys(view: View) {

        ActiveLedgerSDK.getInstance().generateAndSetKeyPair(keyType, true, "")
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(object : Observer<KeyPair> {

                    override fun onSubscribe(d: Disposable) {
                        disposable = d
                    }

                    override fun onError(e: Throwable) {}

                    override fun onComplete() {
                        Log.d("MainActivity", "onComplete")

                        try {
                            setPublickey(ActiveLedgerSDK.getInstance().readFileAsString(Utility.getInstance().getPublicKeyFileName("")))
                            setPrivatekey(ActiveLedgerSDK.getInstance().readFileAsString(Utility.getInstance().getPrivateKeyFileName("")))
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                    }

                    override fun onNext(keyPair: KeyPair) {
                        key_Pair = keyPair
                    }
                })
    }


    fun onboardkeys(view: View) {

        if (key_Pair != null) {

            ActiveLedgerSDK.getInstance().onBoardKeys(key_Pair!!, keyname!!, "")
                    .subscribe(object : Observer<String> {
                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onNext(response: String) {
                            try {
                                Utility.getInstance().extractID(response)
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }

                            Log.e("----->", response)

                        }

                        override fun onError(e: Throwable) {

                        }

                        override fun onComplete() {

                            setOnBoardId(PreferenceManager.getInstance().getStringValueFromKey(Utility.context.getString(R.string.onboard_id)))
                            setOnBoardName(PreferenceManager.getInstance().getStringValueFromKey(Utility.context.getString(R.string.onboard_name))!!)
                        }
                    })

        } else {
            setShowToast("Generate Keys First")
        }

    }

    fun activityOnDestroy() {
        if (disposable != null && !disposable!!.isDisposed)
            disposable!!.dispose()
        ActiveLedgerSDK.getInstance().tearDown()
    }


    fun getPublickey(): MutableLiveData<String> {
        return publickey
    }

    fun setPublickey(publickey: String) {
        this.publickey.postValue(publickey)
    }

    fun getPrivatekey(): MutableLiveData<String> {
        return privatekey
    }

    fun setPrivatekey(privatekey: String) {
        this.privatekey.postValue(privatekey)
    }

    fun getOnBoardId(): MutableLiveData<String> {
        return onBoardId
    }

    fun setOnBoardId(onBoardId: String?) {
        this.onBoardId.postValue(onBoardId)
    }

    fun getOnBoardName(): MutableLiveData<String> {
        return onBoardName
    }

    fun setOnBoardName(onBoardName: String) {
        this.onBoardName.postValue(onBoardName)
    }

    fun getShowToast(): MutableLiveData<String> {
        return showToast
    }

    fun setShowToast(message: String) {
        this.showToast.postValue(message)
    }

    fun getTerritorialityList() {
        ActiveLedgerSDK.getInstance().territorialityStatus
                .subscribe(object : Observer<Territoriality> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onNext(response: Territoriality) {
                        //Territoriality object has a list of neighbours and reference to left and right node
                        Log.e("Territoriality --->", response.status)

                    }

                    override fun onError(e: Throwable) {

                    }

                    override fun onComplete() {}
                })
    }

    fun getTransactionData(id: String) {
        ActiveLedgerSDK.getInstance().getTransactionData(id)
                .subscribe(object : Observer<String> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onNext(response: String) {
                        //Territoriality object has a list of neighbours and reference to left and right node
                        Log.e("transaction data --->", response)

                    }

                    override fun onError(e: Throwable) {

                    }

                    override fun onComplete() {}
                })
    }

    fun subscribeToEvent(protocol: String, ip: String, port: String, url: String) {
        //null to use default listener and observe the messages through 'eventLiveData' or create own listener to observe
        ActiveLedgerSDK.getInstance().subscribeToEvent(protocol, ip, port, url, null)
    }

}
