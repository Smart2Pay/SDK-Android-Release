package com.smart2pay.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.smart2pay.example.requests.RequestManager
import com.smart2pay.example.requests.requests.PaymentsRequest
import com.smart2pay.example.requests.requests.PaymentsVerifyRequest
import com.smart2pay.sdk.PaymentManager
import com.smart2pay.sdk.models.Payment

class MainActivity : AppCompatActivity(), PaymentManager.PaymentManagerEventListener {
    val TAG = "SMART2PAY"
    val paymentManager = PaymentManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val alipayButton = this.findViewById<Button>(R.id.alipay_button)
        val wechatButton = this.findViewById<Button>(R.id.wechat_button)

        alipayButton!!.setOnClickListener {
            pay(Payment.PaymentProvider.ALIPAY)
        }

        wechatButton!!.setOnClickListener {
            pay(Payment.PaymentProvider.WECHAT)
        }
    }

    fun pay(paymentProvider: Payment.PaymentProvider) {
        val payment = Payment()
        payment.amount = 10
        payment.currency = "CNY"
        payment.type = paymentProvider
        payment.activity = this@MainActivity
        RequestManager.initialize(payment.activity)
        createOrder(payment)
    }

    private fun createOrder(payment: Payment) {
        val orderParameters = HashMap<String, Any>()
        orderParameters["amount"] = payment.amount.toString()
        orderParameters["currency"] = payment.currency
        orderParameters["methodID"] = paymentManager.getMethodId(payment.type).toString()

        val paymentsRequest = PaymentsRequest(RequestManager.instance)
        paymentsRequest.setRequestBody(orderParameters)
        paymentsRequest.callback =
                object : PaymentsRequest.Callback {
                    override fun onSuccess(paymentId: Int, paymentsResponse: String) {
                        payment.id = paymentId
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

    // PaymentManagerEventListener callbacks

    override fun onPaymentFailure(payment: Payment) {
        Log.d(TAG, "Payment successful from ${payment.type}")

    }

    override fun onPaymentSuccess(payment: Payment, body: HashMap<String, Any>) {
        Log.d(TAG, "Payment failed from ${payment.type}")
        verifyPayment(payment, body)
    }
}
