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
package com.example.activeledgersdk.onboard


import com.example.activeledgersdk.ActiveLedgerSDK
import com.example.activeledgersdk.utility.KeyType
import com.example.activeledgersdk.utility.Utility

import org.json.JSONException
import org.json.JSONObject

import java.io.IOException
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.KeyPair
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.Signature
import java.security.SignatureException
import java.security.spec.InvalidKeySpecException

class OnboardIdentity {

    // this method return the onboard transaction as JSON object
    fun onboard(keyPair: KeyPair, type: KeyType, identifier: String): JSONObject {

        val transaction = JSONObject()
        val `$sigs` = JSONObject()
        val identity = JSONObject()
        val `$i` = JSONObject()
        val `$tx` = JSONObject()

        try {

            var pubKey: String? = null
            try {
                pubKey = Utility.getInstance().readFileAsString(Utility.getInstance().getPublicKeyFileName(identifier))
                println("public:::" + pubKey!!.toString())
                val priKey = Utility.getInstance().readFileAsString(Utility.getInstance().getPrivateKeyFileName(identifier))
                println("private:::$priKey")
            } catch (e: IOException) {
                e.printStackTrace()
            }


            `$tx`.put("\$contract", "onboard")
            `$tx`.put("\$namespace", "default")

            identity.put("publicKey", pubKey)

            if (type == KeyType.RSA)
                identity.put("type", "rsa")
            else if (type == KeyType.EC)
                identity.put("type", "secp256k1")

            `$i`.put(ActiveLedgerSDK.KEYNAME, identity)
            `$tx`.put("\$i", `$i`)

            try {

                val signTransactionObject = Utility.getInstance().convertJSONObjectToString(`$tx`)
                `$sigs`.put(ActiveLedgerSDK.KEYNAME, signMessage(signTransactionObject.toByteArray(), keyPair, type, identifier))

            } catch (e: Exception) {
                throw IllegalArgumentException("Unable to sign object:" + e.message)
            }

            transaction.put("\$tx", `$tx`)
            transaction.put("\$selfsign", true)
            transaction.put("\$sigs", `$sigs`)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return transaction
    }

    companion object {

        private var instance: OnboardIdentity? = null

        @Synchronized
        fun getInstance(): OnboardIdentity {
            if (instance == null) {
                instance = OnboardIdentity()
            }
            return instance as OnboardIdentity
        }

        // use this method to sign a transaction using private key
        @Throws(InvalidKeyException::class, NoSuchAlgorithmException::class, NoSuchProviderException::class, SignatureException::class)
        fun signMessage(message: ByteArray, keyPair: KeyPair, type: KeyType, identifier: String): String {

            var sign: Signature? = null
            var factory: KeyFactory? = null

            if (type == KeyType.RSA) {
                sign = Signature.getInstance("SHA256withRSA", "BC")
                factory = KeyFactory.getInstance("RSA", "BC")
            } else {
                sign = Signature.getInstance("SHA256withECDSA", "BC")
                factory = KeyFactory.getInstance("EC", "BC")

            }

            try {
                sign!!.initSign(Utility.getInstance().generatePrivateKeyFromFile(Utility.getInstance().getPrivateKeyFileName(identifier), type))
            } catch (e: InvalidKeySpecException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            //		  sign.initSign(Utility.generatePrivateKeyFromFile(factory,Utility.PRIVATEKEY_FILE));
            //        sign.initSign(keyPair.getPrivate());

            sign!!.update(message)
            val signature = sign.sign()
            println("Sign byte::::::::$signature")

            val s = android.util.Base64.encodeToString(signature, 16)
            println("Signautre::::::::$s")


            return s
        }
    }


}
