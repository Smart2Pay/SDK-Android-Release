package com.smart2pay.example

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.smart2pay.example.misc.Constants
import com.smart2pay.example.models.Order
import com.smart2pay.example.requests.RequestManager
import com.smart2pay.example.requests.requests.AuthorizationApiKeyRequest
import com.smart2pay.example.requests.requests.PaymentsRequest
import com.smart2pay.example.requests.requests.PaymentsVerifyRequest
import com.smart2pay.sdk.PaymentManager
import com.smart2pay.sdk.models.Payment
import com.smart2pay.sdk.requests.requests.CardAuthenticationRequest

class MainActivity : AppCompatActivity(), PaymentManager.PaymentManagerEventListener {
    val TAG = "SMART2PAY"
    val paymentManager = PaymentManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        RequestManager.initialize(this@MainActivity)

        val alipayButton = this.findViewById<Button>(R.id.alipay_button)
        val wechatButton = this.findViewById<Button>(R.id.wechat_button)
        val creditCardButton = this.findViewById<Button>(R.id.creditcard_button)

        alipayButton!!.setOnClickListener {
            pay(Payment.PaymentProvider.ALIPAY)
        }

        wechatButton!!.setOnClickListener {
            pay(Payment.PaymentProvider.WECHAT)
        }

        creditCardButton!!.setOnClickListener {
            getApiKeyForCreditCardCheck()
        }
    }

    fun pay(paymentProvider: Payment.PaymentProvider) {
        val order = Order()
        order.amount = 10
        order.currency = "EUR"
        order.type = paymentProvider
        RequestManager.initialize(this@MainActivity)
        placeOrder(order)
    }

    private fun placeOrder(order: Order) {
        val orderParameters = HashMap<String, Any>()
        orderParameters["amount"] = order.amount.toString()
        orderParameters["currency"] = order.currency
        orderParameters["methodID"] = paymentManager.getMethodId(order.type).toString()

        val paymentsRequest = PaymentsRequest(RequestManager.instance)
        paymentsRequest.setRequestBody(orderParameters)
        paymentsRequest.callback =
                object : PaymentsRequest.Callback {
                    override fun onSuccess(paymentId: Int, paymentsResponse: String) {
                        val payment = Payment()
                        payment.id = paymentId
                        payment.amount = order.amount
                        payment.currency = order.currency
                        payment.type = order.type
                        payment.activity = this@MainActivity
                        payment.instructions = paymentsResponse
                        paymentManager.pay(payment)
                    }

                    override fun onFailure() {

                    }
                }
        paymentsRequest.enqueue()
    }

    private fun verifyPayment(payment: Payment, body: HashMap<String, Any>) {
        val paymentsVerifyRequest = PaymentsVerifyRequest(RequestManager.instance)
        paymentsVerifyRequest.setRequestBody(payment.id, body)
        paymentsVerifyRequest.callback =
                object : PaymentsVerifyRequest.Callback {
                    override fun onSuccess() {
                        // Payment verification was successful!
                    }

                    override fun onFailure() {
                        // Payment verification was a failure!
                    }
                }
        paymentsVerifyRequest.enqueue()
    }

    // Credit Card handling

    private fun getApiKeyForCreditCardCheck() {
        val authorizationApiKeyRequest = AuthorizationApiKeyRequest(RequestManager.instance)
        authorizationApiKeyRequest.callback =
                object : AuthorizationApiKeyRequest.Callback {
                    override fun onSuccess(apiKey: String) {
                        // Authorization was successful!
                        Log.d("ApiTokenForCreditCard", apiKey)
                        getCreditCardToken(apiKey)
                    }

                    override fun onFailure() {
                        // Authorization was a failure!
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

        val cardAuthenticationRequest = CardAuthenticationRequest("Basic $apiKey", true)
        cardAuthenticationRequest.setRequestBody(Constants.dummyCreditCardData())
        cardAuthenticationRequest.callback =
                object : CardAuthenticationRequest.Callback {
                    override fun onSuccess(creditCardToken: String) {
                        // Verification was successful!
                        // Send it to your server and initiate a transactions via REST API: https://docs.smart2pay.com/category/direct-card-processing/one-click-payment/
                        Log.d("TokenForCreditCard", creditCardToken)
                    }

                    override fun onFailure() {
                        // Verification was a failure!
                    }
                }
        cardAuthenticationRequest.enqueue()
    }

    // PaymentManagerEventListener callbacks

    override fun onPaymentFailure(payment: Payment) {
        Log.d(TAG, "Payment failed from ${payment.type}")
        displayPaymentResult(payment.type.toString() + " payment failed.")
    }

    override fun onPaymentSuccess(payment: Payment, body: HashMap<String, Any>) {
        Log.d(TAG, "Payment successful from ${payment.type}")
        verifyPayment(payment, body)
    }

    private fun displayPaymentResult(result: String) {
        val mainHandler = Handler(Looper.getMainLooper())

        val myRunnable = Runnable {
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setMessage(result)
                .setPositiveButton(R.string.ok_button_title, DialogInterface.OnClickListener { dialog, id ->
                    //OK
                })

            val ad = builder.create()
            ad.show()
        }
        mainHandler.post(myRunnable)
    }
}
