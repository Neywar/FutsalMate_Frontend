package com.example.futsalmate;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class EsewaPaymentActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esewa_payment);

        WebView webView = findViewById(R.id.webViewEsewa);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(EsewaPaymentActivity.this, "Payment page error: " + description, Toast.LENGTH_SHORT).show();
            }
        });

        String paymentUrl = getIntent().getStringExtra("payment_url");
        String amount = formatAmount(getIntent().getDoubleExtra("amount", 0));
        String taxAmount = formatAmount(getIntent().getDoubleExtra("tax_amount", 0));
        String totalAmount = formatAmount(getIntent().getDoubleExtra("total_amount", 0));
        String transactionUuid = getIntent().getStringExtra("transaction_uuid");
        String productCode = getIntent().getStringExtra("product_code");
        String productServiceCharge = formatAmount(getIntent().getDoubleExtra("product_service_charge", 0));
        String productDeliveryCharge = formatAmount(getIntent().getDoubleExtra("product_delivery_charge", 0));
        String successUrl = getIntent().getStringExtra("success_url");
        String failureUrl = getIntent().getStringExtra("failure_url");
        String signedFieldNames = getIntent().getStringExtra("signed_field_names");
        String signature = getIntent().getStringExtra("signature");

        if (paymentUrl == null || paymentUrl.trim().isEmpty()) {
            Toast.makeText(this, "Payment URL missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String postData = buildPostData(
                amount,
                taxAmount,
                totalAmount,
                transactionUuid,
                productCode,
                productServiceCharge,
                productDeliveryCharge,
                successUrl,
                failureUrl,
                signedFieldNames,
                signature
        );

        webView.postUrl(paymentUrl, postData.getBytes(StandardCharsets.UTF_8));
    }

    private String formatAmount(double value) {
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private String buildPostData(
            String amount,
            String taxAmount,
            String totalAmount,
            String transactionUuid,
            String productCode,
            String productServiceCharge,
            String productDeliveryCharge,
            String successUrl,
            String failureUrl,
            String signedFieldNames,
            String signature
    ) {
        return "amount=" + encode(amount)
                + "&tax_amount=" + encode(taxAmount)
                + "&total_amount=" + encode(totalAmount)
                + "&transaction_uuid=" + encode(transactionUuid)
                + "&product_code=" + encode(productCode)
                + "&product_service_charge=" + encode(productServiceCharge)
                + "&product_delivery_charge=" + encode(productDeliveryCharge)
                + "&success_url=" + encode(successUrl)
                + "&failure_url=" + encode(failureUrl)
                + "&signed_field_names=" + encode(signedFieldNames)
                + "&signature=" + encode(signature);
    }

    private String encode(String value) {
        if (value == null) {
            return "";
        }
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }
}
