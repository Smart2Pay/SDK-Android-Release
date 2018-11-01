package com.smart2pay.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.smart2pay.example.models.Order
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
        val order = Order()
        order.amount = 10
        order.currency = "CNY"
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

    // PaymentManagerEventListener callbacks

    override fun onPaymentFailure(payment: Payment) {
        Log.d(TAG, "Payment successful from ${payment.type}")

    }

    override fun onPaymentSuccess(payment: Payment, body: HashMap<String, Any>) {
        Log.d(TAG, "Payment failed from ${payment.type}")
        verifyPayment(payment, body)
    }
}
