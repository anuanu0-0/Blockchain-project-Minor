package blockchain;

import blockchain.user.User;

// Interface extends Key
import java.security.PublicKey;

public class Transaction {
    private long transactionId;
    private User from;
    private User to;
    private int amount;
    private long creationTime;
    private String signature;
    private PublicKey publicKey;

    // Assignes Transaction details
    public Transaction(long transactionId, User from, User to, int amount, PublicKey publicKey) {
        this.transactionId = transactionId;
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.publicKey = publicKey;
        this.creationTime = System.currentTimeMillis();
    }

    // Return Transaction details to the caller

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public long getId() { return transactionId; }

    public User getFrom() { return from; }

    public User getTo() { return to; }

    public int getAmount() { return amount; }

    public long getCreationTime() { return creationTime; }

    public String getSignature() { return signature; }

    public PublicKey getPublicKey() { return publicKey; }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", from=" + from +
                ", to=" + to +
                ", amount=" + amount +
                ", creationTime=" + creationTime +
                ", publicKey=" + publicKey +
                '}';
    }
}