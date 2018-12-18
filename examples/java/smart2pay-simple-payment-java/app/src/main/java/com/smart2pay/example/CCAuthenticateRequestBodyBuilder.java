package com.smart2pay.example;


import java.util.HashMap;

public final class CCAuthenticateRequestBodyBuilder {

    public static HashMap <String, Object> getBody(HashMap <String, Object> card){

            HashMap <String, Object>  body = new HashMap<String, Object>();
            HashMap <String, Object>  cardAuthentication = new HashMap<String, Object>();
            HashMap <String, Object>  customer = new HashMap<String, Object>();
            HashMap <String, Object>  billingAddress = new HashMap<String, Object>();

            customer.put("FirstName", "John");
            customer.put("LastName", "Doe");
            customer.put("Email", "android@sdktest.com");
            customer.put("SocialSecurityNumber", "00003456789");

            billingAddress.put("Country", "BR");

            cardAuthentication.put("Customer", customer);
            cardAuthentication.put("BillingAddress", billingAddress);
            cardAuthentication.put("Card", card);

            body.put("CardAuthentication", cardAuthentication);
            return body;
    }
}
