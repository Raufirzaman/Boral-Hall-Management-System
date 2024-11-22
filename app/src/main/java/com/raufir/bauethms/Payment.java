package com.raufir.bauethms;
public class Payment {
    private String date;
    private String amount;

    public Payment(String date, String amount) {
        this.date = date;
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public String getAmount() {
        return amount;
    }
}

