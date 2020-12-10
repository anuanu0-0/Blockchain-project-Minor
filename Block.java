package blockchain;

import java.io.Serializable;
import java.util.List;

import blockchain.user.Miner;
import blockchain.utils.*;

// Implementing Serialization & Overriding methods
public class Block implements Serializable, Cloneable {
    private final long id;
    private final long timestamp;
    private String prevBlockHash;
    private List<Transaction> transactions;
    private String hash;
    private int magicNum;
    private long timeTookForMiningMs;
    private Miner miner;
    private int mineReward;
    private String transactionsToStringCached;

    // Initializing block
    private Block(final long id, final List<Transaction> transactions, final String prevBlockHash) {
        this.id = id;
        this.transactions = transactions;
        this.prevBlockHash = prevBlockHash;
        timestamp = System.currentTimeMillis();
    }
    //
    public static Block with(final long id, final List<Transaction> transactions, final String prevBlockHash,
            final int mineReward) {
        Block block = new Block(id, transactions, prevBlockHash);
        block.transactionsToStringCached = block.transactions.stream().map(Transaction::toString).reduce("", String::concat);
        block.mineReward = mineReward;
        return block;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(prevBlockHash);
        str.append(id);
        str.append(transactionsToStringCached);
        str.append(timestamp);
        str.append(magicNum);
        str.append(miner);
        str.append(mineReward);
        return str.toString();
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error("Something messed up!");
        }
    }

    // Overriden has functions and conditionals
    @Override
    public int hashCode() {
        return (int) id;
    }

    // Check for validity / correctness of block
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (obj.getClass() != getClass()) {
            return false;
        }

        if (this.id != ((Block) obj).id) {
            return false;
        }

        if (this.timestamp != ((Block) obj).timestamp) {
            return false;
        }

        if (miner != ((Block) obj).miner) {
            return false;
        }

        if (mineReward != ((Block) obj).mineReward) {
            return false;
        }

        if (magicNum != ((Block) obj).magicNum) {
            return false;
        }

        if (timeTookForMiningMs != ((Block) obj).timeTookForMiningMs) {
            return false;
        }

        if (!this.prevBlockHash.equals(((Block) obj).prevBlockHash)) {
            return false;
        }

        if (!this.hash.equals(((Block) obj).hash)) {
            return false;
        }

        if (!this.transactionsToStringCached.equals(((Block) obj).transactionsToStringCached)) {
            return false;
        }

        return true;
    }
    // Hash functions
    public boolean isConsistent() {
        return hash.equals(StringUtils.applySha256(toString()));
    }

    public long getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getPrevBlockHash() {
        return prevBlockHash;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getMagicNum() {
        return magicNum;
    }

    public void setMagicNum(int magicNum) {
        this.magicNum = magicNum;
    }

    public long getTimeTookForMiningMs() {
        return this.timeTookForMiningMs;
    }

    public void setTimeTookForMiningMs(long timeTookForMiningMs) {
        this.timeTookForMiningMs = timeTookForMiningMs;
    }

    public Miner getMiner() {
        return miner;
    }

    public void setMiner(Miner miner) {
        this.miner = miner;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public String getTransactionsToStringCached() {
        return transactionsToStringCached;
    }

    public int getMineReward() {
        return mineReward;
    }

    public void setMineReward(int mineReward) {
        this.mineReward = mineReward;
    }
}