package com.smart2pay.example;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.smart2pay.sdk.PaymentManager;
import com.smart2pay.sdk.models.Payment;

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

    public void pay(Payment.PaymentProvider paymentProvider) {
        Payment payment = new Payment();
        payment.setAmount(10);
        payment.setCurrency("CNY");
        payment.setType(paymentProvider);
        payment.setActivity(this);

        PaymentManager paymentManager = new PaymentManager();
        paymentManager.pay(payment);
    }

    // PaymentManagerEventListener callbacks

    @Override
    public void onPaymentSuccess(@NonNull Payment payment) {
        Log.d(TAG, "Payment successful from " + payment.getType());
    }

    @Override
    public void onPaymentFailure(@NonNull Payment payment) {
        Log.d(TAG, "Payment failed from " + payment.getType());
    }
}
