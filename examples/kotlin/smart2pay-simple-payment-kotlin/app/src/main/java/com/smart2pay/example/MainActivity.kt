package com.smart2pay.example

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
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
        paymentManager.pay(payment)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)
    }


    // Callback
    override fun onPaymentFailure(payment: Payment) {
        Log.d(TAG, "Payment successful from ${payment.type}")
    }

    override fun onPaymentSuccess(payment: Payment) {
        Log.d(TAG, "Payment failed from ${payment.type}")
    }
}
