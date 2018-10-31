package com.smart2pay.example;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.smart2pay.example.requests.RequestManager;
import com.smart2pay.example.requests.requests.PaymentsRequest;
import com.smart2pay.example.requests.requests.PaymentsVerifyRequest;
import com.smart2pay.sdk.PaymentManager;
import com.smart2pay.sdk.models.Payment;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements PaymentManager.PaymentManagerEventListener {

    final String TAG = "SMART2PAY";
    PaymentManager paymentManager = new PaymentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button alipayButton = findViewById(R.id.alipay_button);
        Button wechatButton = findViewById(R.id.wechat_button);
        alipayButton.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                pay(Payment.PaymentProvider.ALIPAY);
            }
        });

        wechatButton.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                pay(Payment.PaymentProvider.WECHAT);
            }
        });
    }

    public void pay(@NotNull Payment.PaymentProvider paymentProvider) {
        Payment payment = new Payment();
        payment.setAmount(10);
        payment.setCurrency("CNY");
        payment.setType(paymentProvider);
        payment.setActivity((Activity)this);
        RequestManager.Companion.initialize((Context)payment.getActivity());
        this.createOrder(payment);
    }

    private void createOrder(final Payment payment) {
        HashMap orderParameters = new HashMap();
        Map var3 = (Map)orderParameters;
        String var4 = "amount";
        String var5 = String.valueOf(payment.getAmount());
        var3.put(var4, var5);
        var3 = (Map)orderParameters;
        var4 = "currency";
        var5 = payment.getCurrency();
        var3.put(var4, var5);
        var3 = (Map)orderParameters;
        var4 = "methodID";
        var5 = String.valueOf(this.paymentManager.getMethodId(payment.getType()));
        var3.put(var4, var5);
        PaymentsRequest paymentsRequest = new PaymentsRequest(RequestManager.Companion.getInstance());
        paymentsRequest.setRequestBody(orderParameters);
        paymentsRequest.setCallback((PaymentsRequest.Callback)(new PaymentsRequest.Callback() {
            public void onSuccess(int paymentId, @NotNull String paymentsResponse) {
                payment.setId(paymentId);
                payment.setInstructions(paymentsResponse);
                paymentManager.pay(payment);
            }

            public void onFailure() {
            }
        }));
        paymentsRequest.enqueue();
    }

    private void verifyPayment(Payment payment, HashMap body) {
        PaymentsVerifyRequest paymentsVerifyRequest = new PaymentsVerifyRequest(RequestManager.Companion.getInstance());
        paymentsVerifyRequest.setRequestBody(payment.getId(), body);
        paymentsVerifyRequest.setCallback((new com.smart2pay.example.requests.requests.PaymentsVerifyRequest.Callback() {
            public void onSuccess() {
            }

            public void onFailure() {
            }
        }));
        paymentsVerifyRequest.enqueue();
    }


    // PaymentManagerEventListener callbacks

    @Override
    public void onPaymentSuccess(@NonNull Payment payment, @NonNull HashMap<String, Object> body) {
        Log.d(TAG, "Payment successful from " + payment.getType());
        verifyPayment(payment, body);
    }

    @Override
    public void onPaymentFailure(@NonNull Payment payment) {
        Log.d(TAG, "Payment failed from " + payment.getType());
    }
}
