package Service;

import Utils.PaymentFramework;

public class CashPayment extends PaymentFramework {

    public CashPayment(String customerName, double amount, double discountPercent) {
        super(customerName, amount, discountPercent, "CASH");
    }

    @Override
    protected boolean validatePayment() {
        return true;
    }

    @Override
    protected void finalizeTransaction() {
        transactionSuccess = true;
    }
}