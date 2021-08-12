package com.smart2pay.example

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.nuvei.sdk.web_view.WebViewActivity
import com.nuvei.threeds2sdk.ChallengeActivity
import com.smart2pay.example.misc.Constants
import com.smart2pay.example.requests.RequestManager
import com.smart2pay.example.requests.requests.AuthorizationApiKeyRequest
import com.smart2pay.example.requests.requests.PayAfter3dRequest
import com.smart2pay.sdk.PaymentManager
import com.smart2pay.sdk.models.ApiError
import com.smart2pay.sdk.models.Environment
import com.smart2pay.sdk.models.S2PAuth3dOutput
import com.smart2pay.sdk.requests.requests.CardAuthenticationRequest
import org.json.JSONObject

class Auth3dActivity: AppCompatActivity() {
    private val TAG = "Auth3dActivity"

    private val paymentManager = PaymentManager()
    private var environment: Environment = Environment.TESTING

    private var apiKey: String? = null
    private var creditCardToken: String? = null
    private var cvv: String? = null
    private var amount: String? = null
    private var currency: String? = null
    private var auth3dOutput: S2PAuth3dOutput? = null

    private var amountInput: EditText? = null
    private var currencyInput: EditText? = null
    private var cardHolderNameInput: EditText? = null
    private var cardNumberInput: EditText? = null
    private var cardExpMonthInput: EditText? = null
    private var cardExpYearInput: EditText? = null
    private var cardCvvInput: EditText? = null

    private var environmentButton: Button? = null
    private var auth3dButton: Button? = null
    private var payButton: Button? = null

    private var logTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth3d)
        RequestManager.initialize(this@Auth3dActivity)

        amountInput = findViewById(R.id.input_amount)
        currencyInput = findViewById(R.id.input_currency)
        cardHolderNameInput = findViewById(R.id.input_card_holder_name)
        cardNumberInput = findViewById(R.id.input_card_number)
        cardExpMonthInput = findViewById(R.id.input_card_exp_month)
        cardExpYearInput = findViewById(R.id.input_card_exp_year)
        cardCvvInput = findViewById(R.id.input_cvv)

        logTextView = findViewById(R.id.text_log)

        amountInput?.setText("153")
        currencyInput?.setText("EUR")
        cardCvvInput?.setText("121")
        cardExpMonthInput?.setText("11")
        cardExpYearInput?.setText("2022")

        environmentButton = findViewById<Button>(R.id.button_environment)?.apply { setOnClickListener { chooseEnvironment() } }
        findViewById<Button>(R.id.button_fill)?.setOnClickListener { fill() }
        findViewById<Button>(R.id.button_tokenize)?.apply { setOnClickListener { tokenize() } }
        auth3dButton = findViewById<Button>(R.id.button_auth3d)?.apply {
            setOnClickListener { auth3d() }
            isEnabled = false
        }
        payButton = findViewById<Button>(R.id.button_pay)?.apply {
            setOnClickListener { pay() }
            isEnabled = false
        }

        setEnvironment(Environment.TESTING)
    }

    private fun chooseEnvironment() {
        val data = arrayOf("Testing", "Production")
        AlertDialog.Builder(this)
            .setTitle("Environment")
            .setItems(data) { _, which ->
                when (which) {
                    0 -> setEnvironment(Environment.TESTING)
                    1 -> setEnvironment(Environment.PRODUCTION)
                }
            }
            .show()
    }

    private fun setEnvironment(environment: Environment) {
        this.environment = environment
        paymentManager.setup(environment)
        environmentButton?.text = when (environment) {
            Environment.TESTING -> "Env: Testing"
            Environment.PRODUCTION -> "Env: Production"
        }
    }

    private fun fill() {
        val data = arrayOf("3Dv2", "Frictionless", "3Dv1")
        AlertDialog.Builder(this)
            .setTitle("Flow")
            .setItems(data) { _, which ->
                when (which) {
                    0 -> {
                        // Force Web Challenge
                        cardHolderNameInput?.setText("CL-BRW1")
                        cardNumberInput?.setText("2221008123677736")
                    }
                    1 -> {
                        // Frictionless
                        cardHolderNameInput?.setText("FL-APP1")
                        cardNumberInput?.setText("5111426646345761")
                    }
                    2 -> {
                        // 3Dv1
                        cardHolderNameInput?.setText("CL-APP1")
                        cardNumberInput?.setText("4407106439671112")
                    }
                }
            }
            .show()
    }

    private fun tokenize() {
        auth3dButton?.isEnabled = false
        payButton?.isEnabled = false

        cvv = cardCvvInput?.text?.toString()
        amount = amountInput?.text?.toString()
        currency = currencyInput?.text?.toString()

        auth3dOutput = null

        log("Request apiKey", true)
        val authorizationApiKeyRequest = AuthorizationApiKeyRequest(RequestManager.instance)
        authorizationApiKeyRequest.callback = object : AuthorizationApiKeyRequest.Callback {
            override fun onSuccess(apiKey: String) {
                this@Auth3dActivity.apiKey = apiKey
                log("Response apiKey: $apiKey")
                getCreditCardToken(apiKey)
            }

            override fun onFailure() {
                log("Response apiKey: failed")
            }
        }
        authorizationApiKeyRequest.enqueue()
    }

    private fun getCreditCardToken(apiKey: String) {
//        {
//            "CardAuthentication": {
//                "Customer": {
//                    "FirstName": "John",
//                    "LastName": "Doe",
//                    "Email": "testing2@test.com",
//                    "SocialSecurityNumber": "00003456789"
//                },
//                "BillingAddress": {
//                    "Country": "BR"
//                },
//                "Card": {
//                    "HolderName": "John Doe",
//                    "Number": "4111111111111111",
//                    "ExpirationMonth": "02",
//                    "ExpirationYear": "2019",
//                    "SecurityCode": "312"
//                }
//            }
//        }

        log(
            "paymentManager.authenticateCreditCard(" +
                    "\"firstName\": Michael, " +
                    "\"lastName\": Dean, " +
                    "\"email\": android@sdktest.com, " +
                    "\"socialNumber\": 00003456789, " +
                    "\"countryCode\": BR, " +
                    "\"cardHolderName\": ${cardHolderNameInput?.text?.toString() ?: ""}, " +
                    "\"cardNumber\": ${cardNumberInput?.text?.toString() ?: ""}, " +
                    "\"cardExpMonth\": ${cardExpMonthInput?.text?.toString() ?: ""}, " +
                    "\"cardExpYear\": ${cardExpYearInput?.text?.toString() ?: ""}, " +
                    "\"cardSecurityCode\": ${this.cvv ?: ""}, " +
                    "\"apiKey\": $apiKey)"
        )
        paymentManager.authenticateCreditCard(
            firstName = "Michael",
            lastName = "Dean",
            email = "android@sdktest.com",
            socialNumber = "00003456789",
            countryCode = "BR",
            cardHolderName = cardHolderNameInput?.text?.toString() ?: "",
            cardNumber = cardNumberInput?.text?.toString() ?: "",
            cardExpMonth = cardExpMonthInput?.text?.toString() ?: "",
            cardExpYear = cardExpYearInput?.text?.toString() ?: "",
            cardSecurityCode = this.cvv ?: "",
            apiKey = apiKey
        ) { creditCardToken, error ->
            this@Auth3dActivity.creditCardToken = creditCardToken

            creditCardToken?.let {
                log("Response creditCardToken: $it")
            }

            error?.let {
                log("Response errorCode = ${it.statusCode}, message = ${it.message}")
            }

            runOnUiThread {
                auth3dButton?.isEnabled = (creditCardToken?.isNotEmpty() == true)
            }
        }
    }

    private fun auth3d() {
        val creditCardToken = this.creditCardToken
        val cvv = this.cvv
        val amount = this.amount
        val currency = this.currency
        val apiKey = this.apiKey
        if (creditCardToken != null && cvv != null && amount != null && currency != null && apiKey != null) {
            log("paymentManager.authenticate3d(" +
                    "\"creditCardToken\": $creditCardToken, " +
                    "\"cvv\": $cvv, " +
                    "\"amount\": $amount, " +
                    "\"currency\": $currency, " +
                    "\"apiKey\": $apiKey)")
            paymentManager.authenticate3d(
                this,
                creditCardToken,
                cvv,
                amount,
                currency,
                apiKey
            ) { output ->
                log("paymentManager.authenticate3d().output:"
                        + "\n- \"userPaymentOptionId\":${output.userPaymentOptionId}"
                        + "\n- \"result\":${output.result}"
                        + "\n- \"cavv\":${output.cavv}"
                        + "\n- \"eci\":${output.eci}"
                        + "\n- \"xid\":${output.xid}"
                        + "\n- \"dsTransID\":${output.dsTransID}"
                        + "\n- \"ccCardNumber\":${output.ccCardNumber}"
                        + "\n- \"bin\":${output.bin}"
                        + "\n- \"last4Digits\":${output.last4Digits}"
                        + "\n- \"ccExpMonth\":${output.ccExpMonth}"
                        + "\n- \"ccExpYear\":${output.ccExpYear}"
                        + "\n- \"ccTempToken\":${output.ccTempToken}"
                        + "\n- \"transactionId\":${output.transactionId}"
                        + "\n- \"threeDReasonId\":${output.threeDReasonId}"
                        + "\n- \"threeDReason\":${output.threeDReason}"
                        + "\n- \"challengePreferenceReason\":${output.challengePreferenceReason}"
                        + "\n- \"isLiabilityOnIssuer\":${output.isLiabilityOnIssuer}"
                        + "\n- \"challengeCancelReasonId\":${output.challengeCancelReasonId}"
                        + "\n- \"challengeCancelReason\":${output.challengeCancelReason}"
                        + "\n- \"errorCode\":${output.errorCode}"
                        + "\n- \"errorDescription\":${output.errorDescription}"
                )

                this.auth3dOutput = output
                payButton?.isEnabled = (output.result == S2PAuth3dOutput.S2PAuth3dResult.APPROVED)
            }
        } else {
            log("Missing data for paymentManager.authenticate3d")
        }
//        if (apiKey != null && creditCardToken != null) {
//            startActivity(
//                Auth3dActivity.createIntent(
//                    this,
//                    apiKey!!,
//                    creditCardToken!!
//                )
//            )
//        }
    }

    private fun pay() {
        // [
        //            "Amount": amount,
        //            "Currency": currency,
        //            "CreditCardToken": creditCardToken,
        //            "SecurityCode": securityCode,
        //            "MethodId": "6",
        //            "3DSecureData": [
        //                "TransactionStatus": transactionStatus,
        //                "ECI": eci,
        //                "AuthenticationValue": authenticationValue,
        //                "DSTransId": dsTransId,
        //                "MessageVersion": messageVersion,
        //                "ThreeDSecureAuthenticationType1": "F",
        //            ]
        //        ]

        val amount = this.amount
        val currency = this.currency
        val creditCardToken = this.creditCardToken
        val securityCode = this.cvv
        val eci = auth3dOutput?.eci
        val authenticationValue = auth3dOutput?.cavv
        val dsTransID = auth3dOutput?.dsTransID

        val transactionStatus = if (auth3dOutput?.result == S2PAuth3dOutput.S2PAuth3dResult.APPROVED) "Y" else "N"

        if (
            amount != null &&
            currency != null &&
            creditCardToken != null &&
            securityCode != null &&
            eci != null &&
            authenticationValue != null &&
            dsTransID != null
        ) {
            val params = HashMap<String, Any>().apply {
                put("Amount", amount)
                put("Currency", currency)
                put("CreditCardToken", creditCardToken)
                put("SecurityCode", securityCode)
                put("MethodId", "6")
                put("3DSecureData", HashMap<String, Any>().apply {
                    put("TransactionStatus", transactionStatus)
                    put("ECI", eci)
                    put("AuthenticationValue", authenticationValue)
                    put("DSTransId", dsTransID)
                    put("MessageVersion", "2.1.0")
                    put("ThreeDSecureAuthenticationType1", "F")
                })
            }
            log("Request payment:\n$params")

            val paymentRequest = PayAfter3dRequest(RequestManager.instance)
            paymentRequest.setRequestBody(params)
            paymentRequest.callback =
                object : PayAfter3dRequest.Callback {
                    override fun onSuccess(json: JSONObject) {
                        log("Response payment:\n$json")
                    }

                    override fun onFailure(e: Throwable) {
                        log("Error payment:\n$e")
                    }
                }
            paymentRequest.enqueue()
        } else {
            log("Request payment: Wrong input")
        }
    }

    private fun log(text: String, clear: Boolean = false) {
        Log.d(TAG, text)
        runOnUiThread {
            logTextView?.let { log ->
                log.text = "${if (clear) "" else "${log.text}\n\n"}$text"
            }
        }
    }
}