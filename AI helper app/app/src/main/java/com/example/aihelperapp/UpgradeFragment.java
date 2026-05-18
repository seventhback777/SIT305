package com.example.aihelperapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UpgradeFragment extends Fragment {

    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;

    private PaymentsClient paymentsClient;
    private String pendingPrice;
    private String pendingTier;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upgrade, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        paymentsClient = Wallet.getPaymentsClient(requireActivity(),
                new Wallet.WalletOptions.Builder()
                        .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                        .build());

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        Button btnBuyStarter = view.findViewById(R.id.btnBuyStarter);
        Button btnBuyIntermediate = view.findViewById(R.id.btnBuyIntermediate);
        Button btnBuyAdvanced = view.findViewById(R.id.btnBuyAdvanced);

        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        btnBuyStarter.setOnClickListener(v -> startPayment("4.99", "Starter"));
        btnBuyIntermediate.setOnClickListener(v -> startPayment("9.99", "Intermediate"));
        btnBuyAdvanced.setOnClickListener(v -> startPayment("19.99", "Advanced"));
    }

    private void startPayment(String price, String tier) {
        pendingPrice = price;
        pendingTier = tier;

        IsReadyToPayRequest isReadyToPayRequest = buildIsReadyToPayRequest();
        if (isReadyToPayRequest == null) return;

        paymentsClient.isReadyToPay(isReadyToPayRequest).addOnCompleteListener(task -> {
            if (task.isSuccessful() && Boolean.TRUE.equals(task.getResult())) {
                launchGooglePay(price);
            } else {
                Toast.makeText(requireContext(),
                        "Google Pay is not available on this device.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void launchGooglePay(String price) {
        PaymentDataRequest request = buildPaymentDataRequest(price);
        if (request == null) return;
        Task<PaymentData> task = paymentsClient.loadPaymentData(request);
        AutoResolveHelper.resolveTask(task, requireActivity(), LOAD_PAYMENT_DATA_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode != LOAD_PAYMENT_DATA_REQUEST_CODE) return;

        switch (resultCode) {
            case Activity.RESULT_OK:
                saveTier(pendingTier);
                showSuccessDialog(pendingTier);
                break;

            case Activity.RESULT_CANCELED:
                Toast.makeText(requireContext(), "Payment cancelled.", Toast.LENGTH_SHORT).show();
                break;

            case AutoResolveHelper.RESULT_ERROR:
                Status status = AutoResolveHelper.getStatusFromIntent(data);
                String msg = status != null ? status.getStatusMessage() : "Unknown error";
                Toast.makeText(requireContext(), "Payment error: " + msg, Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void saveTier(String tier) {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("learning_prefs", 0);
        prefs.edit().putString("subscription_tier", tier).apply();
    }

    private void showSuccessDialog(String tier) {
        String emoji = tier.equals("Advanced") ? "🚀" : tier.equals("Intermediate") ? "⭐" : "✅";
        new AlertDialog.Builder(requireContext())
                .setTitle(emoji + " You're now on " + tier + "!")
                .setMessage("Your " + tier + " plan is now active.\n\n"
                        + getPlanBenefits(tier)
                        + "\n\nThank you for subscribing to AI Learning Assistant!")
                .setPositiveButton("Let's Go!", (d, w) ->
                        Navigation.findNavController(requireView()).popBackStack())
                .setCancelable(false)
                .show();
    }

    private String getPlanBenefits(String tier) {
        switch (tier) {
            case "Starter":       return "• 50 quizzes per month\n• Improved quiz generation";
            case "Intermediate":  return "• Unlimited quizzes\n• AI progress insights\n• Priority support";
            case "Advanced":      return "• Everything in Intermediate\n• Custom AI tutor\n• Team access";
            default:              return "";
        }
    }

    // ── Google Pay JSON builders ───────────────────────────────────────────

    private JSONObject baseRequest() throws JSONException {
        return new JSONObject()
                .put("apiVersion", 2)
                .put("apiVersionMinor", 0);
    }

    private JSONObject tokenizationSpec() throws JSONException {
        return new JSONObject()
                .put("type", "PAYMENT_GATEWAY")
                .put("parameters", new JSONObject()
                        .put("gateway", "example")
                        .put("gatewayMerchantId", "exampleGatewayMerchantId"));
    }

    private JSONObject baseCardMethod() throws JSONException {
        return new JSONObject()
                .put("type", "CARD")
                .put("parameters", new JSONObject()
                        .put("allowedAuthMethods", new JSONArray()
                                .put("PAN_ONLY").put("CRYPTOGRAM_3DS"))
                        .put("allowedCardNetworks", new JSONArray()
                                .put("AMEX").put("DISCOVER").put("MASTERCARD").put("VISA")));
    }

    private JSONObject cardPaymentMethod() throws JSONException {
        return baseCardMethod().put("tokenizationSpecification", tokenizationSpec());
    }

    private IsReadyToPayRequest buildIsReadyToPayRequest() {
        try {
            JSONObject json = baseRequest()
                    .put("allowedPaymentMethods", new JSONArray().put(baseCardMethod()));
            return IsReadyToPayRequest.fromJson(json.toString());
        } catch (JSONException e) {
            return null;
        }
    }

    private PaymentDataRequest buildPaymentDataRequest(String price) {
        try {
            JSONObject json = baseRequest()
                    .put("allowedPaymentMethods", new JSONArray().put(cardPaymentMethod()))
                    .put("transactionInfo", new JSONObject()
                            .put("totalPrice", price)
                            .put("totalPriceStatus", "FINAL")
                            .put("countryCode", "AU")
                            .put("currencyCode", "AUD"))
                    .put("merchantInfo", new JSONObject()
                            .put("merchantName", "AI Learning Assistant"));
            return PaymentDataRequest.fromJson(json.toString());
        } catch (JSONException e) {
            return null;
        }
    }
}
