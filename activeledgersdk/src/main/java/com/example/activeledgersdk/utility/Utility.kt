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
package com.example.activeledgersdk.utility

import android.content.Context
import android.util.Log

import com.example.activeledgersdk.R
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.spongycastle.util.encoders.Base64
import org.spongycastle.util.io.pem.PemObject
import org.spongycastle.util.io.pem.PemWriter

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.security.Key
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec


class Utility {

    // returns the ledger node endpoint
    val httpurl: String
        get() = PreferenceManager.getInstance().getStringValueFromKey(context.getString(R.string.protocol)) + "://" +
                PreferenceManager.getInstance().getStringValueFromKey(context.getString(R.string.ip)) + ":" +
                PreferenceManager.getInstance().getStringValueFromKey(context.getString(R.string.port))

    // extracts the private key from Application preference
    //		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(sigBytes);
    //			return  keyFact.generatePrivate(x509KeySpec);
    val privateKeyFromPreference: PrivateKey?
        get() {
            val privKeyStr = PreferenceManager.getInstance().getStringValueFromKey(Utility.getInstance().getContext().getString(R.string.private_key))
            val sigBytes = Base64.decode(privKeyStr!!)
            var keyFact: KeyFactory? = null
            try {
                keyFact = KeyFactory.getInstance("RSA", "SC")
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: NoSuchProviderException) {
                e.printStackTrace()
            }

            try {
                val privKeySpec = PKCS8EncodedKeySpec(sigBytes)
                return keyFact!!.generatePrivate(privKeySpec)
            } catch (e: InvalidKeySpecException) {
                e.printStackTrace()
            }

            return null
        }

    // extracts the public key from Application preference
    val publicKeyFromPreference: PublicKey?
        get() {
            val pubKeyStr = PreferenceManager.getInstance().getStringValueFromKey(Utility.getInstance().getContext().getString(R.string.public_key))
            val sigBytes = Base64.decode(pubKeyStr!!)
            val x509KeySpec = X509EncodedKeySpec(sigBytes)
            var keyFact: KeyFactory? = null
            try {
                keyFact = KeyFactory.getInstance("RSA", "SC")
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: NoSuchProviderException) {
                e.printStackTrace()
            }

            try {
                return keyFact!!.generatePublic(x509KeySpec)
            } catch (e: InvalidKeySpecException) {
                e.printStackTrace()
            }

            return null
        }

    fun getContext(): Context {
        return context
    }

    fun setContext(context: Context) {
        Utility.context = context
    }

    // this function initialises the sdk and should be called before using SDK
    fun initSDK(context: Context, protocol: String, url: String, port: String) {

        setContext(context)
        PreferenceManager.getInstance().init()
        PreferenceManager.getInstance().saveString(context.getString(R.string.protocol), protocol)
        PreferenceManager.getInstance().saveString(context.getString(R.string.ip), url)
        PreferenceManager.getInstance().saveString(context.getString(R.string.port), port)

    }

    override fun toString(): String {
        return "PemFile [pemObject=$pemObject]"
    }

    // returns the file path where the keys are stored
    fun getFilePath(filename: String): String {
        return getContext().filesDir.path.toString() + "/" + filename
    }

    @Throws(JsonProcessingException::class)
    fun convertObjectToString(`object`: Any): String {

        val mapper = ObjectMapper()
        return mapper.writeValueAsString(`object`)
    }

    @Throws(JsonProcessingException::class)
    fun convertByteArrayToString(byteData: ByteArray): String {

        return android.util.Base64.encodeToString(byteData, 16)
    }

    fun convertStringToByteArray(data: String): ByteArray {
        return data.toByteArray()
    }

    fun convertJSONObjectToString(jsonObject: JSONObject): String {
        return jsonObject.toString().replace("\\/", "/")
    }

    // extract the onboard key if and name from response
    @Throws(JSONException::class)
    fun extractID(response: String) {
        val Jobject = JSONObject(response)
        val name = Jobject.optString("\$streams")
        Log.e("stream", name)

        val JobjectName = JSONObject(name)
        val jsonArray = JobjectName.getJSONArray("new")
        val idObj = jsonArray.getJSONObject(0)


        PreferenceManager.getInstance().saveString(context.getString(R.string.onboard_id), idObj.optString("id"))
        PreferenceManager.getInstance().saveString(context.getString(R.string.onboard_name), idObj.optString("name"))


        Log.e("id", PreferenceManager.getInstance().getStringValueFromKey(context.getString(R.string.onboard_id)))
        Log.e("name", PreferenceManager.getInstance().getStringValueFromKey(context.getString(R.string.onboard_name)))
    }

    // function reads the keys as a string from a text file
    @Throws(IOException::class)
    fun readFileAsString(fileName: String): String {
        Log.e("File Reading", "")
        val filePath = Utility.getInstance().getFilePath(fileName)
        Log.e("File Reading", "file path = $filePath")
        val f = File(filePath)
        val `is` = FileInputStream(f)
        val buf = BufferedReader(InputStreamReader(`is`))
        var line: String? = buf.readLine()
        val sb = StringBuilder()
        while (line != null) {
            sb.append(line).append("\n")
            line = buf.readLine()
        }
        return sb.toString()
    }

    // function writes the keys in PEM format to a text file
    @Throws(FileNotFoundException::class, IOException::class)
    fun writePem(filename: String, description: String, key: Key) {
        Log.e("File Writing", "")
        val filePath = Utility.getInstance().getFilePath(filename)
        Log.e("File Writing", "file path = $filePath")

        val f = File(filePath)
        val pemWriter = PemWriter(FileWriter(f))
        try {
            pemObject = PemObject(description, key.encoded)
            pemWriter.writeObject(pemObject!!)
        } finally {
            pemWriter.close()
        }

    }



    // function generates the private keys from a text file
    @Throws(InvalidKeySpecException::class, IOException::class, NoSuchProviderException::class, NoSuchAlgorithmException::class)
    fun generatePrivateKeyFromFile(filename: String, type: KeyType): PrivateKey {

        var factory: KeyFactory? = null

        if (type === KeyType.RSA) {
            factory = KeyFactory.getInstance("RSA", "BC")
        } else {
            factory = KeyFactory.getInstance("EC", "BC")
        }

        val filePath = Utility.getInstance().getFilePath(filename)
        Log.e("File Reading", "file path = $filePath")
        val f = File(filePath)
        val pemFile = PemFile(f)
        val content = pemFile.pemObject!!.content
        val privKeySpec = PKCS8EncodedKeySpec(content)
        return factory!!.generatePrivate(privKeySpec)


    }

    // function generates the public keys from a text file
    @Throws(InvalidKeySpecException::class, IOException::class, NoSuchProviderException::class, NoSuchAlgorithmException::class)
    fun generatePublicKeyFromFile(filename: String, type: KeyType): PublicKey {

        var factory: KeyFactory? = null

        if (type === KeyType.RSA) {
            factory = KeyFactory.getInstance("RSA", "BC")
        } else {
            factory = KeyFactory.getInstance("EC", "BC")
        }

        val filePath = Utility.getInstance().getFilePath(filename)
        Log.e("File Reading", "file path = $filePath")
        val f = File(filePath)
        val pemFile = PemFile(f)
        val content = pemFile.pemObject!!.content
        val pubKeySpec = X509EncodedKeySpec(content)
        return factory!!.generatePublic(pubKeySpec)
    }

    fun getPrivateKeyFileName(identifier: String): String {
        var identifier = identifier
        identifier = removePeriodFromIdentifier(identifier)
        return "$identifier-priv-key.pem"
    }

    fun getPublicKeyFileName(identifier: String): String {
        var identifier = identifier
        identifier = removePeriodFromIdentifier(identifier)
        return "$identifier-pub-key.pem"
    }

    fun removePeriodFromIdentifier(identifier: String): String {
        return identifier.replace(".", "")
    }


    companion object {

        //    public static String PUBLICKEY_FILE = "pub-key.pem";
        lateinit var context: Context
        private var instance: Utility? = null
        private var pemObject: PemObject? = null


        @Synchronized
        fun getInstance(): Utility {
            if (instance == null) {
                instance = Utility()
            }
            return instance as Utility
        }


    }

}
