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
package com.example.activeledgersdk.key

import com.example.activeledgersdk.utility.KeyType
import com.example.activeledgersdk.utility.Utility

import org.spongycastle.crypto.DataLengthException

import java.io.IOException
import java.math.BigInteger
import java.security.InvalidAlgorithmParameterException
import java.security.Key
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Security
import java.security.interfaces.RSAPrivateCrtKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.ECGenParameterSpec

class KeyGenApi {

    // generates RSC/EC keypair
    fun generateKeyPair(keyType: KeyType, saveKeysToFile: Boolean, identifier: String): KeyPair? {

        var keyPair: KeyPair? = null
        try {

            Security.addProvider(org.spongycastle.jce.provider.BouncyCastleProvider())


            if (keyType == KeyType.RSA) {
                keyPair = createRSAKeyPair()
            } else {
                keyPair = createSecp256k1KeyPair()
            }


            val priv = keyPair.private
            val pub = keyPair.public

            println("Format pri key:" + priv.format)
            println("Format pub key:" + pub.format)


            try {
                Utility.getInstance().writePem(Utility.getInstance().getPublicKeyFileName(identifier), "PUBLIC KEY", pub)

                if (saveKeysToFile) {

                    Utility.getInstance().writePem(Utility.getInstance().getPrivateKeyFileName(identifier), "PRIVATE KEY", priv)

                }

            } catch (e: IOException) {
                e.printStackTrace()
            }

        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: DataLengthException) {
            e.printStackTrace()
        }

        return keyPair
    }

    fun createSecp256k1KeyPair(): KeyPair {

        var keyPairGenerator: KeyPairGenerator? = null
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "SC")

            val ecGenParameterSpec = ECGenParameterSpec("secp256k1")
            keyPairGenerator!!.initialize(ecGenParameterSpec, SecureRandom())
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchProviderException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        }

        return keyPairGenerator!!.generateKeyPair()

    }

    private fun createRSAKeyPair(): KeyPair {

        var keyPairGenerator: KeyPairGenerator? = null
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA", "SC")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchProviderException) {
            e.printStackTrace()
        }

        keyPairGenerator!!.initialize(2048)
        return keyPairGenerator.generateKeyPair()
    }

    companion object {

        // checks the validity of RSA keypair
        internal fun isValidRSAPair(pair: KeyPair): Boolean {
            val key = pair.private
            if (key is RSAPrivateCrtKey) {
                val e = key.publicExponent
                val pub = pair.public as RSAPublicKey
                return e == pub.publicExponent && key.modulus == pub.modulus
            } else {
                throw IllegalArgumentException("Not a CRT RSA key.")
            }
        }
    }


}

