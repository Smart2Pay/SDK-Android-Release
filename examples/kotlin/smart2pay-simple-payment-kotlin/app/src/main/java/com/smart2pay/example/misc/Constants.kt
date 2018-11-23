package com.smart2pay.example.misc

import com.smart2pay.example.BuildConfig

object Constants {
    const val apiUrl = "https://s2pmerchantserver.azurewebsites.net"
    const val apiToken = "Basic UzJwTWVyY2hhbnRTZXJ2ZXI6UzJwTWVyY2hhbnRTZXJ2ZXJQYXNzd29yZA=="
    val isDebug = BuildConfig.DEBUG

    fun dummyCreditCardData() : HashMap<String, Any> {
        val body = HashMap<String, Any>()
        val cardAuthentication = HashMap<String, Any>()
        val customer = HashMap<String, Any>()
        val billingAddress = HashMap<String, Any>()
        val card = HashMap<String, Any>()

        customer["FirstName"] = "John"
        customer["LastName"] = "Doe"
        customer["Email"] = "android@sdktest.com"
        customer["SocialSecurityNumber"] = "00003456789"

        billingAddress["Country"] = "BR"

        card["HolderName"] = "John Doe"
        card["Number"] = "4111111111111111"
        card["ExpirationMonth"] = "02"
        card["ExpirationYear"] = "2019"
        card["SecurityCode"] = "321"

        cardAuthentication["Customer"] = customer
        cardAuthentication["BillingAddress"] = billingAddress
        cardAuthentication["Card"] = card

        body["CardAuthentication"] = cardAuthentication

        return body
    }
}