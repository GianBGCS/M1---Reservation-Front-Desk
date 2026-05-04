package Service;

import Util.PaymentFramework;

public class CashPayment extends PaymentFramework {

    public CashPayment(String customerName, double amount, double discountPercent) {
        super(customerName, amount, discountPercent, "CASH");
    }

    @Override
    protected boolean validatePayment() {
        // In a real system you would check cash authenticity, etc.
        return true;
    }

    @Override
    protected void finalizeTransaction() {
        transactionSuccess = true;
    }
}