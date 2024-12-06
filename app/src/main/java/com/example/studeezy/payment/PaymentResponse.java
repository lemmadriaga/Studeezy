package com.example.studeezy.payment;

public class PaymentResponse {

    private PaymentData data;

    public PaymentData getData() {
        return data;
    }

    public void setData(PaymentData data) {
        this.data = data;
    }

    public static class PaymentData {
        private String id;
        private String type;
        private PaymentAttributes attributes;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public PaymentAttributes getAttributes() {
            return attributes;
        }

        public void setAttributes(PaymentAttributes attributes) {
            this.attributes = attributes;
        }
    }

    public static class PaymentAttributes {
        private int amount;
        private String currency;
        private String description;
        private String status;
        private String checkout_url;

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getCheckoutUrl() {
            return checkout_url;
        }

        public void setCheckoutUrl(String checkout_url) {
            this.checkout_url = checkout_url;
        }
    }
}
