package com.smart2pay.example.misc

import com.smart2pay.example.BuildConfig

object Constants {
    const val apiUrl = "https://s2pmerchantserver.azurewebsites.net"
    const val apiToken = "Basic UzJwTWVyY2hhbnRTZXJ2ZXI6UzJwTWVyY2hhbnRTZXJ2ZXJQYXNzd29yZA=="
    val isDebug = BuildConfig.DEBUG

    fun creditCardData(
        firstName: String = "Michael",
        lastName: String = "Dean",
        email: String = "android@sdktest.com",
        socialNumber: String = "00003456789",
        countryCode: String = "BR",
        cardHolderName: String = "Michael Dean",
        cardNumber: String = "4111111111111111",
        cardExpMonth: String = "02",
        cardExpYear: String = "2022",
        cardSecurityCode: String = "321"
    ) : HashMap<String, Any> {
        val body = HashMap<String, Any>()
        val cardAuthentication = HashMap<String, Any>()
        val customer = HashMap<String, Any>()
        val billingAddress = HashMap<String, Any>()
        val card = HashMap<String, Any>()

        customer["FirstName"] = firstName
        customer["LastName"] = lastName
        customer["Email"] = email
        customer["SocialSecurityNumber"] = socialNumber

        billingAddress["Country"] = countryCode

        card["HolderName"] = cardHolderName
        card["Number"] = cardNumber
        card["ExpirationMonth"] = cardExpMonth
        card["ExpirationYear"] = cardExpYear
        card["SecurityCode"] = cardSecurityCode

        cardAuthentication["Customer"] = customer
        cardAuthentication["BillingAddress"] = billingAddress
        cardAuthentication["Card"] = card

        body["CardAuthentication"] = cardAuthentication

        return body
    }
}