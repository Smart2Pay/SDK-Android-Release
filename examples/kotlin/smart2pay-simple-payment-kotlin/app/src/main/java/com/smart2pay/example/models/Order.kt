package com.smart2pay.example.models

import com.smart2pay.sdk.models.Payment

class Order {
    var type = Payment.PaymentProvider.NONE
    var amount: Int = 0
    var currency: String = ""
}