package blockchain.user;

import blockchain.Blockchain;
import blockchain.Transaction;
import blockchain.utils.SignatureUtils;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Random;

public class User implements Runnable {
    protected String name;
    protected Blockchain blockchain;
    protected long id;
    protected KeyPair keyPair = null;
    protected Random transRandom;
    protected static final int MAX_SLEEP_TIME = 5000;
    protected static final int MIN_SLEEP_TIME = 1;

    // Generating key pairs
    protected User(long id, Blockchain blockchain) {
        this.blockchain = blockchain;
        this.id = id;
        name = names[(int) id];
        transRandom = new Random();
        while (keyPair == null) {
            keyPair = SignatureUtils.generateKeyPair();
        }
    }

    public static User with(long id, Blockchain blockchain) {
        return new User(id, blockchain);
    }


    @Override
    public void run() {
        Random random = new Random();

        while (true) {
            try {
                int sleepTime = random.nextInt(MAX_SLEEP_TIME);
                while (sleepTime < MIN_SLEEP_TIME) {
                    sleepTime = random.nextInt(MAX_SLEEP_TIME);
                }
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                return;
            }
            doTransaction();
        }
    }

    // Performing Transaction and setting up signature for transaction
    protected boolean doTransaction() {
        User to = UserFactory.getUser(transRandom.nextInt((int) UserFactory.getNoOfUsers() + 1));
        int amount = transRandom.nextInt(100 + 1);
        long transactionId = blockchain.getTransactionId();

        Transaction transaction = new Transaction(transactionId, this, to, amount, keyPair.getPublic());
        String signature = SignatureUtils.generateSignature(transaction.toString(), keyPair.getPrivate());
        transaction.setSignature(signature);

        return blockchain.addTransaction(transaction);
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public String getName() {
        return name;
    }

    // Random naming
    protected static String[] names = { "James", "Mary", "John", "Linda", "Robert", "Michael", "Sarah", "William",
            "Laya", "David", "Richard", "Lisa", "Joseph", "Thomas", "Jessica", "Charles", "Nancy", "Jyothi", "Karthik",
            "Christopher", "Jennifer", "Keerthi" };
}
