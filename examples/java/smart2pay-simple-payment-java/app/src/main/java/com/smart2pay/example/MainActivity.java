package com.smart2pay.example;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.smart2pay.example.models.Order;
import com.smart2pay.example.requests.RequestManager;
import com.smart2pay.example.requests.requests.PaymentsRequest;
import com.smart2pay.example.requests.requests.PaymentsVerifyRequest;
import com.smart2pay.sdk.PaymentManager;
import com.smart2pay.sdk.models.Payment;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

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
        Order order = new Order();
        order.setAmount(10);
        order.setCurrency("CNY");
        order.setType(paymentProvider);
        RequestManager.Companion.initialize(this);
        this.placeOrder(order);
    }

    private void placeOrder(final Order order) {
        HashMap<String, Object> orderParameters = new HashMap<>();
        orderParameters.put("amount", String.valueOf(order.getAmount()));
        orderParameters.put("currency", order.getCurrency());
        orderParameters.put("methodID", String.valueOf(this.paymentManager.getMethodId(order.getType())));
        PaymentsRequest paymentsRequest = new PaymentsRequest(RequestManager.Companion.getInstance());
        paymentsRequest.setRequestBody(orderParameters);
        paymentsRequest.setCallback((new PaymentsRequest.Callback() {
            public void onSuccess(int paymentId, @NotNull String paymentsResponse) {
                Payment payment = new Payment();
                payment.setAmount(order.getAmount());
                payment.setCurrency(order.getCurrency());
                payment.setType(order.getType());
                payment.setId(paymentId);
                payment.setInstructions(paymentsResponse);
                payment.setActivity(MainActivity.this);
                paymentManager.pay(payment);
            }

            public void onFailure() {
            }
        }));
        paymentsRequest.enqueue();
    }

    private void verifyPayment(Payment payment, HashMap<String, Object> body) {
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
