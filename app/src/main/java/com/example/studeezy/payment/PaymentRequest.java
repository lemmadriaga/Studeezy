package com.example.studeezy.payment;

public class PaymentRequest {
    private PaymentData data;

    public PaymentRequest(PaymentData data) {
        this.data = data;
    }

    public PaymentData getData() {
        return data;
    }

    public void setData(PaymentData data) {
        this.data = data;
    }

    public static class PaymentData {
        private PaymentAttributes attributes;

        public PaymentAttributes getAttributes() {
            return attributes;
        }

        public void setAttributes(PaymentAttributes attributes) {
            this.attributes = attributes;
        }
    }

    public static class PaymentAttributes {
        private int amount;
        private String description;
        private String remarks;

        public PaymentAttributes(int amount, String description, String remarks) {
            this.amount = amount;
            this.description = description;
            this.remarks = remarks;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }
    }
}
