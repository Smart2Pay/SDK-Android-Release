package com.smart2pay.example;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.smart2pay.example.builders.CCAuthenticateRequestBodyBuilder;
import com.smart2pay.example.models.Order;
import com.smart2pay.example.requests.RequestManager;
import com.smart2pay.example.requests.requests.AuthorizationApiKeyRequest;
import com.smart2pay.example.requests.requests.PaymentsRequest;
import com.smart2pay.example.requests.requests.PaymentsVerifyRequest;
import com.smart2pay.sdk.PaymentManager;
import com.smart2pay.sdk.models.Payment;
import com.smart2pay.sdk.requests.requests.CardAuthenticationRequest;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements PaymentManager.PaymentManagerEventListener {

    final String TAG = "SMART2PAY";
    PaymentManager paymentManager = new PaymentManager();
    TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RequestManager.Companion.initialize(this);

        resultTextView = findViewById(R.id.tv_results);

        Button alipayButton = findViewById(R.id.alipay_button);
        Button weChatButton = findViewById(R.id.wechat_button);
        Button creditCardButton = findViewById(R.id.creditcard_button);


        alipayButton.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                displayDebugInfoReset();
                displayDebugInfo(getResources().getString(R.string.text_results));
                pay(Payment.PaymentProvider.ALIPAY);
            }
        });


        weChatButton.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                displayDebugInfoReset();
                displayDebugInfo(getResources().getString(R.string.text_results));
                pay(Payment.PaymentProvider.WECHAT);
            }
        });

        creditCardButton.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                displayDebugInfoReset();
                displayDebugInfo(getResources().getString(R.string.text_results));
                getApiKeyForCreditCardCheck();
            }
        });
    }

    public void pay(@NotNull Payment.PaymentProvider paymentProvider) {
        Order order = new Order();
        order.setAmount(Integer.parseInt(((EditText)findViewById(R.id.e_amount)).getText().toString()));
        order.setCurrency(((EditText)findViewById(R.id.e_currency)).getText().toString());
        order.setType(paymentProvider);
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
        final HashMap<String, Object> bodyCopy = body;

        displayDebugInfo(bodyCopy.toString());

        paymentsVerifyRequest.setRequestBody(payment.getId(), body);
        paymentsVerifyRequest.setCallback((new com.smart2pay.example.requests.requests.PaymentsVerifyRequest.Callback() {
            public void onSuccess() {
                Log.d(TAG, "Payment successfully verified.");
                displayDebugInfo("Payment successfully verified.");
            }

            public void onFailure() {
                Log.d(TAG, "Payment verification failed.");
                displayDebugInfo("Payment verification failed.");
            }
        }));
        paymentsVerifyRequest.enqueue();
    }

    // Credit Card handling

    private void getApiKeyForCreditCardCheck() {
        AuthorizationApiKeyRequest authorizationApiKeyRequest = new AuthorizationApiKeyRequest(RequestManager.Companion.getInstance());
        authorizationApiKeyRequest.setCallback((new AuthorizationApiKeyRequest.Callback() {
            public void onSuccess(final String apiKey) {
                // Authorization was successful!
                Log.d("APITokenForCreditCard", apiKey);
                displayDebugInfo("Temporary API key:" + apiKey);
                getCreditCardToken(apiKey);
            }

            public void onFailure() {
                // Authorization was a failure!
                Log.d(TAG, "Failed to get temporary API key.");
                displayDebugInfo("Failed to get temporary API key.");
            }
        }));
        authorizationApiKeyRequest.enqueue();
    }

    private void getCreditCardToken(String apiKey) {
//        {
//            "CardAuthentication": {
//                "Customer": {
//                    "FirstName": "John",
//                    "LastName": "Doe",
//                    "Email": "testing2@test.com",
//                    "SocialSecurityNumber": "00003456789"
//                },
//                "BillingAddress": {
//                    "Country": "BR"
//                },
//                "Card": {
//                    "HolderName": "John Doe",
//                    "Number": "4111111111111111",
//                    "ExpirationMonth": "02",
//                    "ExpirationYear": "2019",
//                    "SecurityCode": "312"
//                }
//            }
//        }

        CardAuthenticationRequest cardAuthenticationRequest = new CardAuthenticationRequest("Basic " + apiKey, true);

        HashMap<String,Object> card = new HashMap<String, Object>();
        card.put("HolderName", ((EditText)findViewById(R.id.e_cardholder_name)).getText().toString());
        card.put("Number", ((EditText)findViewById(R.id.e_cc_number)).getText().toString());
        card.put("ExpirationMonth", ((EditText)findViewById(R.id.e_exp_month)).getText().toString());
        card.put("ExpirationYear", ((EditText)findViewById(R.id.e_exp_year)).getText().toString());
        card.put("SecurityCode", ((EditText)findViewById(R.id.e_cvv)).getText().toString());

        cardAuthenticationRequest.setRequestBody(CCAuthenticateRequestBodyBuilder.getBody(card));
        cardAuthenticationRequest.setCallback((new CardAuthenticationRequest.Callback() {
            public void onSuccess(@NonNull final String creditCardToken) {
                // Authorization was successful!
                // Send it to your server and initiate a transactions via REST API: https://docs.smart2pay.com/category/direct-card-processing/one-click-payment/
                Log.d("TokenForCreditCard", creditCardToken);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayDebugInfo("Credit Card Token:" + creditCardToken);
                    }
                });
            }

            public void onFailure() {
                Log.w(TAG,"Card Authentication request failed.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayDebugInfo("Card Authentication request failed.");
                    }
                });
            }
        }));
        cardAuthenticationRequest.enqueue();
    }

    // PaymentManagerEventListener callbacks

    @Override
    public void onPaymentSuccess(@NonNull Payment payment, @NonNull HashMap<String, Object> body) {
        Log.d(TAG, payment.getType() + "payment successful.");
        displayDebugInfo(payment.getType() + "payment successful.");
        //displayPaymentResult(payment.getType() + " payment successful.");
        if(payment.getType()==Payment.PaymentProvider.ALIPAY) {
            Log.d(TAG, "Starting payment verification...");
            verifyPayment(payment, body);
        }
    }

    @Override
    public void onPaymentFailure(@NonNull Payment payment) {
        Log.d(TAG, payment.getType() + "payment failed.");
        displayDebugInfo(payment.getType() + "payment failed.");
        //displayPaymentResult(payment.getType() + " payment failed.");
    }

    private void displayResultialogue(final String result){
        Handler mainHandler = new Handler(Looper.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(result)
                        .setPositiveButton(R.string.ok_button_title, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //OK
                            }
                        });

                AlertDialog ad = builder.create();
                ad.show();
            }
        };
        mainHandler.post(myRunnable);

    }

    private void displayDebugInfo(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultTextView.setText(resultTextView.getText() + "\n" + message);
            }
        });
    }

    private void displayDebugInfoReset(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultTextView.setText(R.string.text_debug_info);
            }
        });
    }

}
