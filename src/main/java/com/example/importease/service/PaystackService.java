package com.example.importease.service;

import com.example.importease.model.PaymentTransaction;
import com.example.importease.model.Shipment;
import com.example.importease.model.ShipmentPaymentStatus;
import com.example.importease.model.Supplier;
import com.example.importease.repository.PaymentTransactionRepository;
import com.example.importease.repository.ShipmentRepository;
import com.example.importease.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaystackService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final SupplierRepository supplierRepository;
    private final ShipmentRepository shipmentRepository;

    @Value("${paystack.secret.key:}")
    private String paystackSecretKey;

    @Value("${paystack.base.url:https://api.paystack.co}")
    private String paystackBaseUrl;

    @Value("${app.base.url:https://importease-backend.onrender.com}")
    private String appBaseUrl;

    public PaystackService(PaymentTransactionRepository paymentTransactionRepository,
                           SupplierRepository supplierRepository,
                           ShipmentRepository shipmentRepository) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.supplierRepository = supplierRepository;
        this.shipmentRepository = shipmentRepository;
    }

    public Map<String, Object> initializePayment(String payerEmail, String supplierName, BigDecimal amount, String currency) {
        return initializePayment(payerEmail, supplierName, amount, currency, "STANDARD", null, null);
    }

    public Map<String, Object> initializePayment(String payerEmail, String supplierName, BigDecimal amount,
                                                  String currency, String paymentType, Long supplierId) {
        return initializePayment(payerEmail, supplierName, amount, currency, paymentType, supplierId, null);
    }

    public Map<String, Object> initializePayment(String payerEmail, String supplierName, BigDecimal amount,
                                                  String currency, String paymentType, Long supplierId,
                                                  String shipmentId) {
        if (paystackSecretKey == null || paystackSecretKey.isBlank()) {
            throw new IllegalStateException("Paystack secret key is not configured");
        }

        String reference = UUID.randomUUID().toString();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("email", payerEmail);
        payload.put("amount", amount.multiply(new BigDecimal("100")).intValueExact());
        payload.put("currency", currency);
        payload.put("reference", reference);
        payload.put("callback_url", appBaseUrl + "/api/payments/callback");
        payload.put("metadata", Map.of("supplierName", supplierName, "paymentType", paymentType,
                "supplierId", supplierId != null ? supplierId : "",
                "shipmentId", shipmentId != null ? shipmentId : ""));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(paystackSecretKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(paystackBaseUrl + "/transaction/initialize", request, Map.class);

        PaymentTransaction transaction = new PaymentTransaction(reference, payerEmail, supplierName, amount, currency);
        transaction.setPaymentType(paymentType);
        transaction.setPaystackResponse(String.valueOf(response.getBody()));
        paymentTransactionRepository.save(transaction);

        Map<String, Object> body = response.getBody();
        Map<String, Object> data = body != null ? (Map<String, Object>) body.get("data") : Map.of();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reference", reference);
        result.put("authorizationUrl", data.get("authorization_url"));
        result.put("accessCode", data.get("access_code"));
        result.put("message", body != null ? body.get("message") : "Payment initialized");
        return result;
    }

    public Map<String, Object> verifyPayment(String reference) {
        PaymentTransaction transaction = paymentTransactionRepository.findByReference(reference)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(paystackSecretKey);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.getForEntity(paystackBaseUrl + "/transaction/verify/" + reference, Map.class, request);

        Map<String, Object> body = response.getBody();
        Map<String, Object> data = body != null ? (Map<String, Object>) body.get("data") : Map.of();
        String status = data.get("status") != null ? String.valueOf(data.get("status")) : "failed";
        transaction.setStatus("success".equalsIgnoreCase(status) ? "SUCCESS" : "FAILED");
        transaction.setPaystackResponse(String.valueOf(body));

        if ("SUCCESS".equals(transaction.getStatus())) {
            BigDecimal commission = transaction.getAmount().multiply(new BigDecimal("0.04"));
            transaction.setCommissionAmount(commission);

            Map<String, Object> metadata = data.get("metadata") instanceof Map ? (Map<String, Object>) data.get("metadata") : Map.of();

            if ("SUBSCRIPTION".equals(transaction.getPaymentType())) {
                try {
                    Object supplierIdObj = metadata.get("supplierId");
                    if (supplierIdObj != null && !String.valueOf(supplierIdObj).isEmpty()) {
                        Long supplierId = Long.valueOf(String.valueOf(supplierIdObj));
                        Optional<Supplier> supplierOpt = supplierRepository.findById(supplierId);
                        if (supplierOpt.isPresent()) {
                            Supplier supplier = supplierOpt.get();
                            supplier.setSubscriptionTier("PAID");
                            supplier.setPaidUntil(LocalDateTime.now().plusDays(30));
                            supplierRepository.save(supplier);
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Subscription upgrade failed: " + ex.getMessage());
                }
            }

            try {
                Object shipmentIdObj = metadata.get("shipmentId");
                if (shipmentIdObj != null && !String.valueOf(shipmentIdObj).isEmpty()) {
                    UUID shipmentId = UUID.fromString(String.valueOf(shipmentIdObj));
                    Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentId);
                    if (shipmentOpt.isPresent()) {
                        Shipment shipment = shipmentOpt.get();
                        shipment.setPaymentStatus(ShipmentPaymentStatus.PAID);
                        shipment.setAmountPaid(transaction.getAmount());
                        shipmentRepository.save(shipment);
                    }
                }
            } catch (Exception ex) {
                System.err.println("Shipment payment update failed: " + ex.getMessage());
            }
        }

        paymentTransactionRepository.save(transaction);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reference", reference);
        result.put("status", transaction.getStatus());
        result.put("amount", transaction.getAmount());
        result.put("currency", transaction.getCurrency());
        result.put("supplierName", transaction.getSupplierName());
        result.put("commissionAmount", transaction.getCommissionAmount());

        if ("SUCCESS".equals(transaction.getStatus())) {
            try {
                String receiptMessage = "Payment successful for " + transaction.getSupplierName() + " — " + transaction.getAmount() + " " + transaction.getCurrency();
                System.out.println("PAYMENT_SUCCESS_REPORT: " + receiptMessage);
            } catch (Exception ex) {
                System.err.println("Payment success report failed: " + ex.getMessage());
            }
        }

        return result;
    }
}
