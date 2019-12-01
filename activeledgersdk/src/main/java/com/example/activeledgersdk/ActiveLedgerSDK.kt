/*
 * MIT License (MIT)
 * Copyright (c) 2018
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.example.activeledgersdk

import android.content.Context
import android.util.Log

import com.example.activeledgersdk.event.SSEUtil
import com.example.activeledgersdk.event.ServerEventListener
import com.example.activeledgersdk.http.HttpClient
import com.example.activeledgersdk.key.KeyGenApi
import com.example.activeledgersdk.model.Territoriality
import com.example.activeledgersdk.onboard.OnboardIdentity
import com.example.activeledgersdk.utility.KeyType
import com.example.activeledgersdk.utility.Utility

import org.json.JSONObject

import java.io.IOException
import java.security.InvalidKeyException
import java.security.KeyPair
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.SignatureException
import java.util.ArrayList

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ActiveLedgerSDK {
    var keyPair: KeyPair? = null

    // does an HTTP hit and return territoriality details
    val territorialityStatus: Observable<Territoriality>
        get() = HttpClient.getInstance().territorialityStatus
                .flatMap { s ->
                    val jsonObject = JSONObject(s)


                    val territorialityObj = Territoriality()
                    territorialityObj.status = jsonObject.getString("status")
                    territorialityObj.reference = jsonObject.getString("reference")
                    territorialityObj.left = jsonObject.getString("left")
                    territorialityObj.right = jsonObject.getString("right")
                    territorialityObj.pem = jsonObject.getString("pem")

                    val keys = jsonObject.getJSONObject("neighbourhood").getJSONObject("neighbours").keys()


                    val neighbours = ArrayList<String>()
                    while (keys.hasNext()) {
                        neighbours.add(keys.next())
                    }
                    territorialityObj.neighbours = neighbours

                    Observable.just(territorialityObj)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

    // base method that has to be called before using SDK
    fun initSDK(context: Context, protocol: String, url: String, port: String) {
        Utility.getInstance().initSDK(context, protocol, url, port)
    }

    // function generates and set the default keypair of the SDK
    fun generateAndSetKeyPair(keyType: KeyType, saveKeysToFile: Boolean, identifier: String): Observable<KeyPair> {

        val keyGenApi = KeyGenApi()
        setKeyType(keyType)
        return Observable.just(keyGenApi.generateKeyPair(keyType, saveKeysToFile, identifier)!!)
    }

    // creates an onboard transaction and execute the http request to the ledger
    fun onBoardKeys(keyPair: KeyPair, keyName: String, identifier: String): Observable<String> {

        KEYNAME = keyName
        val transaction = OnboardIdentity.getInstance().onboard(keyPair, getKeyType(), identifier)

        val transactionJson = Utility.getInstance().convertJSONObjectToString(transaction)

        Log.e("Onboard Transaction", transactionJson)
        return executeTransaction(transactionJson)
    }

    // this method is used to an http request and execute a transaction over the ledger
    fun executeTransaction(transactionJson: String): Observable<String> {

        return HttpClient.getInstance().sendTransaction(transactionJson)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }

    fun getKeyType(): KeyType {
        return keyType
    }

    fun setKeyType(keyType: KeyType) {
        ActiveLedgerSDK.keyType = keyType
    }

    //method used to retrieve the transaction data using id
    fun getTransactionData(id: String): Observable<String> {
        return HttpClient.getInstance().getTransactionData(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun subscribeToEvent(protocol: String, ip: String, port: String, url: String, listener: ServerEventListener?) {
        SSEUtil.getInstance().subscribeToEvent(protocol, ip, port, url, listener)
    }

    fun tearDown() {
        SSEUtil.getInstance().closeEvents()
    }

    // this method can be used to sign a message using private key
    fun signMessage(message: ByteArray, keyPair: KeyPair, type: KeyType, identifier: String): String? {
        try {
            return OnboardIdentity.signMessage(message, keyPair, keyType, identifier)
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchProviderException) {
            e.printStackTrace()
        } catch (e: SignatureException) {
            e.printStackTrace()
        }

        return null
    }

    // by given a file name this function reads the file from application directory and returns content as String.
    @Throws(IOException::class)
    fun readFileAsString(fileName: String): String {
        return Utility.getInstance().readFileAsString(fileName)
    }

    companion object {

        lateinit var keyType: KeyType
        var KEYNAME = "AwesomeKey"
        private var instance: ActiveLedgerSDK? = null

        @Synchronized
        fun getInstance(): ActiveLedgerSDK {
            if (instance == null)
                instance = ActiveLedgerSDK()
            return instance as ActiveLedgerSDK
        }


    }

}
