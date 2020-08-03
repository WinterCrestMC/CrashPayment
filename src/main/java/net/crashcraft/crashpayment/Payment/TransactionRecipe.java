package net.crashcraft.crashpayment.Payment;

import java.util.UUID;

public class TransactionRecipe {
    private UUID owner;

    private String comment;
    private double amount;

    private String transactionError;

    public TransactionRecipe(UUID owner, double amount, String comment) {
        this.owner = owner;
        this.comment = comment;
        this.amount = amount;
    }

    public TransactionRecipe(UUID owner, double amount, String comment, String transactionError) {
        this.owner = owner;
        this.comment = comment;
        this.amount = amount;
        this.transactionError = transactionError;
    }

    public boolean transactionSuccess(){
        return transactionError == null;
    }

    public TransactionResponse getTransactionStatus(){
        return transactionError == null ? TransactionResponse.SUCCESS : TransactionResponse.ERROR;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getComment() {
        return comment;
    }

    public double getAmount() {
        return amount;
    }

    public String getTransactionError() {
        return transactionError;
    }
}
