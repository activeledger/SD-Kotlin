package com.example.activeledgersdk

import android.util.Log

import com.example.activeledgersdk.utility.PreferenceManager
import com.example.activeledgersdk.utility.Utility

import org.json.JSONException
import org.json.JSONObject

object Transactions {


    fun registerNamespaceTransactionObject(namespace: String, identityStream: String): JSONObject {

        val `$tx` = JSONObject()
        val `$i` = JSONObject()
        val IdentityStream = JSONObject()

        try {
            `$tx`.put("\$namespace", "default")
            `$tx`.put("\$contract", "namespace")
            IdentityStream.put("namespace", namespace)
            `$i`.put(identityStream, IdentityStream)

            `$tx`.put("\$i", `$i`)
        } catch (e1: JSONException) {
            e1.printStackTrace()
        }

        return `$tx`
    }


    fun registerNamespaceTransaction(namespace: String, identityStream: String, identifier: String): JSONObject {

        val transaction = JSONObject()

        val `$tx` = registerNamespaceTransactionObject(namespace, identityStream)
        try {

            transaction.put("\$tx", `$tx`)

            val `$sigs` = JSONObject()
            val signTransactionObject = Utility.getInstance().convertJSONObjectToString(`$tx`)
            val signature = ActiveLedgerSDK.getInstance().signMessage(signTransactionObject.toByteArray(), ActiveLedgerSDK.getInstance().keyPair!!, ActiveLedgerSDK.getInstance().getKeyType(), identifier)
            `$sigs`.put(identityStream, signature)
            transaction.put("\$sigs", `$sigs`)

            Log.e("Namespace Transaction", transaction.toString())

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return transaction
    }


    fun smartContractDeploymentTransactionObject(version: String, namespace: String, name: String, base64TSContract: String, identityStream: String): JSONObject {

        val `$tx` = JSONObject()
        val `$i` = JSONObject()
        val IdentityStream = JSONObject()

        try {
            `$tx`.put("\$namespace", "default")
            `$tx`.put("\$contract", "contract")
            IdentityStream.put("version", version)
            IdentityStream.put("namespace", namespace)
            IdentityStream.put("name", name)
            IdentityStream.put("contract", base64TSContract)
            `$i`.put(identityStream, IdentityStream)

            `$tx`.put("\$i", `$i`)
        } catch (e1: JSONException) {
            e1.printStackTrace()
        }

        return `$tx`

    }

    // this method can be used to create a transaction that uploads a contracr to the ledger
    fun createContractUploadTransaction(): JSONObject {
        val transaction = JSONObject()

        try {
            val `$tx` = JSONObject()
            `$tx`.put("\$namespace", "default")
            `$tx`.put("\$contract", "contract")
            val `$i` = JSONObject()
            val identity = JSONObject()
            identity.put("version", "<version>")
            identity.put("namespace", "<namespace>")
            identity.put("name", "<name>")
            identity.put("contract", "<base64 encoded smart contract>")
            `$i`.put(PreferenceManager.getInstance().getStringValueFromKey(Utility.getInstance().getContext().getString(R.string.onboard_id)), identity)
            `$tx`.put("\$i", `$i`)
            transaction.put("\$tx", `$tx`)

            val signature: String? = null
            //        signature = OnboardIdentity.signMessage(gson.toJson($tx).getBytes("UTF-8"),keyType);
            //        Log.e("new ---->",signature);

            val `$sigs` = JSONObject()
            `$sigs`.put(PreferenceManager.getInstance().getStringValueFromKey(Utility.getInstance().getContext().getString(R.string.onboard_id)), signature)


            transaction.put("\$sigs", `$sigs`)
        } catch (e: JSONException) {
            e.printStackTrace()
        }


        return transaction
    }


    fun createFundsTransferTransaction(): JSONObject {

        val transaction = JSONObject()
        try {


            val `$tx` = JSONObject()
            `$tx`.put("\$namespace", "default")
            `$tx`.put("\$contract", "fund")
            `$tx`.put("\$entry", "transfer")
            val `$i` = JSONObject()
            val identity = JSONObject()
            identity.put("symbol", "B$")
            identity.put("amount", 5)
            `$i`.put(PreferenceManager.getInstance().getStringValueFromKey(Utility.getInstance().getContext().getString(R.string.onboard_id)), identity)
            `$tx`.put("\$i", `$i`)
            val `$o` = JSONObject()
            val outputIdentity = JSONObject()
            outputIdentity.put("amount", 5)
            `$o`.put("output identity", outputIdentity)
            `$tx`.put("\$o", `$o`)
            transaction.put("\$tx", `$tx`)


            val signature: String? = null
            //        signature = OnboardIdentity.signMessage(gson.toJson($tx).getBytes("UTF-8"),keyType);
            //        Log.e("new ---->",signature);

            val `$sigs` = JSONObject()
            `$sigs`.put(PreferenceManager.getInstance().getStringValueFromKey(Utility.getInstance().getContext().getString(R.string.onboard_id)), signature)


            transaction.put("\$sigs", `$sigs`)
        } catch (e: JSONException) {
            e.printStackTrace()
        }


        return transaction
    }

    // method creates the basic transaction object this is included in a transaction
    fun createTXObject(namespace: String, contract: String, entry: String?, `$i`: JSONObject?, `$o`: JSONObject?, `$r`: JSONObject?): JSONObject {

        val `$tx` = JSONObject()

        try {
            `$tx`.put("\$namespace", namespace)

            `$tx`.put("\$contract", contract)
            if (entry != null)
                `$tx`.put("\$entry", entry)
            if (`$i` != null)
                `$tx`.put("\$i", `$i`)
            if (`$o` != null)
                `$tx`.put("\$o", `$o`)
            if (`$r` != null)
                `$tx`.put("\$r", `$r`)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return `$tx`
    }


    // this method creates the basic transaction that can save time while creating transactions
    fun createBaseTransaction(territorialityReference: String?, `$tx`: JSONObject, selfsign: Boolean?, `$sigs`: JSONObject?): JSONObject {

        val transaction = JSONObject()

        try {
            if (territorialityReference != null)
                transaction.put("\$territoriality", territorialityReference)

            transaction.put("\$tx", `$tx`)

            if (selfsign != null)
                transaction.put("\$selfsign", selfsign)
            if (`$sigs` != null)
                transaction.put("\$sigs", `$sigs`)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        Log.e("Base Transaction", transaction.toString())

        return transaction
    }


    fun smartContractDeploymentTransaction(version: String, namespace: String, name: String, base64TSContract: String, identityStream: String, identifier: String): JSONObject {

        val transaction = JSONObject()

        val `$tx` = smartContractDeploymentTransactionObject(version, namespace, name, base64TSContract, identityStream)
        try {

            transaction.put("\$tx", `$tx`)

            val `$sigs` = JSONObject()
            val signTransactionObject = Utility.getInstance().convertJSONObjectToString(`$tx`)
            val signature = ActiveLedgerSDK.getInstance().signMessage(signTransactionObject.toByteArray(), ActiveLedgerSDK.getInstance().keyPair!!, ActiveLedgerSDK.getInstance().getKeyType(), identifier)
            `$sigs`.put(identityStream, signature)
            transaction.put("\$sigs", `$sigs`)

            Log.e("Namespace Transaction", transaction.toString())

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return transaction
    }


    fun createAndSignTransaction(`$tx`: JSONObject): JSONObject {
        val transaction = JSONObject()
        val `$sigs` = JSONObject()
        val onboard_id = PreferenceManager.getInstance().getStringValueFromKey("onboard_id")

        val signTransactionObject = Utility.getInstance().convertJSONObjectToString(`$tx`)

        try {

            transaction.put("\$tx", `$tx`)
            transaction.put("\$selfsign", false)
            `$sigs`.put(onboard_id, ActiveLedgerSDK.getInstance().signMessage(signTransactionObject.toByteArray(), null!!, null!!, ""))
            transaction.put("\$sigs", `$sigs`)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        Log.e("Transaction", transaction.toString())

        return transaction
    }


    fun createLabeledTransaction(`$tx`: JSONObject): JSONObject {
        val transaction = JSONObject()
        val `$sigs` = JSONObject()
        var stream_id = ""
        val signTransactionObject = Utility.getInstance().convertJSONObjectToString(`$tx`)

        try {

            stream_id = `$tx`.getJSONObject("\$i").getJSONObject(`$tx`.getJSONObject("\$i").keys().next()).getString("\$stream")

            transaction.put("\$tx", `$tx`)
            transaction.put("\$selfsign", false)
            `$sigs`.put(stream_id, ActiveLedgerSDK.getInstance().signMessage(signTransactionObject.toByteArray(), null!!, null!!, ""))
            transaction.put("\$sigs", `$sigs`)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        Log.e("Transaction", transaction.toString())

        return transaction
    }


}
