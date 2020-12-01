package blockchain;

import blockchain.user.Miner;
import blockchain.user.User;

import static blockchain.utils.SignatureUtils.verifySignature;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Takes control of validity, checks currency status(or transaction status) and
// unprocessed blocks(if any), and created blocks for the blockchain

public class Blockchain implements Serializable {

    private static class UnprocessedBlock {
        private final ReentrantReadWriteLock readWriteLock;
        private Block block;

        UnprocessedBlock() {
            block = null;
            readWriteLock = new ReentrantReadWriteLock();
        }

        Block getBlock() {
            return block;
        }

        void setBlock(Block block) {
            this.block = block;
        }

        ReentrantReadWriteLock.ReadLock getReadLock() {
            return readWriteLock.readLock();
        }

        ReentrantReadWriteLock.WriteLock getWriteLock() {
            return readWriteLock.writeLock();
        }
    }

    private final List<Block> chain;
    private final UnprocessedBlock unprocessedBlock;
    private final Queue<Transaction> transactionQueue;

    private BlockchainDriver creator;

    private long runningBlockId;
    private String runningPrevBlockHash;

    private int noOfStartZerosForHash;
    private String requiredPrefixForHash;

    private static final int BLOCK_CREATION_FREQUENCY_PER_MINUTE = 100;
    private static final int FIXED_MINING_TIME_MS = (int) ((60 * 1e3) / BLOCK_CREATION_FREQUENCY_PER_MINUTE);
    // 15% deviation from fixed time is acceptable
    private static final int ACCEPTABLE_DEVIATION_IN_MINING_TIME_MS = ((FIXED_MINING_TIME_MS * 15) / 100);

    private long currentMiningBlockStartTimeMs;
    private long currentMiningBlockEndTimeMs;

    private final AtomicLong transactionIdCounter = new AtomicLong(1);
    private long largestTransactionIdTillPrevBlock = 0L;
    private final ReentrantReadWriteLock largestTransactionIdTillPrevBlockLock = new ReentrantReadWriteLock();

    private static final int mineReward = 100;
    private static final int initialUserBalance = 100;

    private Blockchain() {
        runningBlockId = 1;
        runningPrevBlockHash = "0";
        chain = new LinkedList<>();
        unprocessedBlock = new UnprocessedBlock();
        transactionQueue = new ConcurrentLinkedQueue<>();
        noOfStartZerosForHash = 0;
        requiredPrefixForHash = "";
    }

    public static Blockchain generateBlockchain(Object caller) {
        if(!(caller instanceof BlockchainDriver)) throw new IllegalCallerException();
        Blockchain blockchain = new Blockchain();
        blockchain.creator = (BlockchainDriver) caller;

        blockchain.unprocessedBlock.setBlock(blockchain.createBlock());
        blockchain.currentMiningBlockStartTimeMs = System.currentTimeMillis();
        return blockchain;
    }

    public boolean addTransaction(Transaction transaction) {
        if (!validateTransaction(transaction)) { return false; }
        transactionQueue.add(transaction);

        unprocessedBlock.getWriteLock().lock();
        if (unprocessedBlock.getBlock() == null) {
            unprocessedBlock.setBlock(createBlock());
            currentMiningBlockStartTimeMs = System.currentTimeMillis();
        }
        unprocessedBlock.getWriteLock().unlock();
        return true;
    }

    private Block createBlock() {
        largestTransactionIdTillPrevBlockLock.writeLock().lock();
        largestTransactionIdTillPrevBlock = transactionQueue.stream()
                                                .map(Transaction::getId)
                                                .max(Long::compare).orElse(0L);

        List<Transaction> transactions = new LinkedList<>();
        for (int i = 0; i < transactionQueue.size(); i++) {
            transactions.add(transactionQueue.remove());
        }

        Block block = Block.with(runningBlockId++, transactions, runningPrevBlockHash, mineReward);
        runningPrevBlockHash = null;
        largestTransactionIdTillPrevBlockLock.writeLock().unlock();
        return block;
    }

    public synchronized boolean submitBlock(Block block, Object caller) {
        if (!(caller instanceof Miner)) {
            throw new IllegalCallerException();
        }

        if (!areIdenticalBlocks(unprocessedBlock.getBlock(), block)) { return false; }
        if (!block.getHash().startsWith(requiredPrefixForHash)) { return false; }
        if (!block.isConsistent()) { return false; }

        currentMiningBlockEndTimeMs = System.currentTimeMillis();

        block.setTimeTookForMiningMs(currentMiningBlockEndTimeMs - currentMiningBlockStartTimeMs);

        unprocessedBlock.getWriteLock().lock();

        chain.add(block);
        updateMiningConstraints();
        runningPrevBlockHash = block.getHash();

        if (transactionQueue.isEmpty()) {
            unprocessedBlock.setBlock(null);
        } else {
            unprocessedBlock.setBlock(createBlock());
            currentMiningBlockStartTimeMs = System.currentTimeMillis();
        }

        unprocessedBlock.getWriteLock().unlock();

        creator.saveBlockchain();
        return true;
    }

    public Block getUnprocessedBlock() {
        try {
            return (Block) unprocessedBlock.getBlock().clone();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isValid() {
        long id = 1;
        String prevBlockHash = "0";

        for (Block block : chain) {
            if (block.getId() != id) return false;
            if (!block.getPrevBlockHash().equals(prevBlockHash)) return false;
            if (!block.isConsistent()) return false;
            String presentHash = block.getHash();

            id += 1;
            prevBlockHash = presentHash;
        }

        return true;
    }

    public long getLength() { return chain.size(); }

    public Block getBlock(int index) { return chain.get(index); }

    public String getRequiredPrefixForHash() { return requiredPrefixForHash; }

    public static int getFixedMiningTimeMs() { return FIXED_MINING_TIME_MS; }

    public static int getAcceptableDeviationInMiningTimeMs() { return ACCEPTABLE_DEVIATION_IN_MINING_TIME_MS; }

    public long getTransactionId() {
        return transactionIdCounter.getAndIncrement();
    }

    private static boolean areIdenticalBlocks(Block b1, Block b2) {
        if (b1 == null || b2 == null) { return false; }
        if (b1 == b2) { return true; }

        if (!b1.getClass().equals(b2.getClass())) { return false; }
        if (b1.getId() != b2.getId()) { return false; }
        if (b1.getTimestamp() != b2.getTimestamp()) { return false; }
        if (!b1.getPrevBlockHash().equals(b2.getPrevBlockHash())) { return false; }
        if (!b1.getTransactionsToStringCached().equals(b2.getTransactionsToStringCached())) { return false; }

        return true;
    }

    private boolean validateTransaction(Transaction transaction) {
        largestTransactionIdTillPrevBlockLock.readLock().lock();
        if (transaction.getId() < largestTransactionIdTillPrevBlock) { return false; }
        largestTransactionIdTillPrevBlockLock.readLock().unlock();
        
        if (transaction.getFrom() == transaction.getTo()) { return false; }
        if (!transaction.getFrom().getPublicKey().equals(transaction.getPublicKey())) { return false; }
        if (!verifySignature(transaction.toString(), transaction.getSignature(), transaction.getPublicKey())) { return false; }
        if (transaction.getAmount() > getBalance(transaction.getFrom(), initialUserBalance)) { return false; }
        return true;
    }

    public int getBalance(User user, int seed) {
        class ToBeProcessedBlocks {
            int nBlocks = 0;
            int nTransactions = 0;
        }
        ToBeProcessedBlocks toBeProcessedBlocks = new ToBeProcessedBlocks();

        unprocessedBlock.getReadLock().lock();
        toBeProcessedBlocks.nBlocks += chain.size();
        toBeProcessedBlocks.nBlocks += unprocessedBlock.getBlock() == null ? 0 : 1;
        toBeProcessedBlocks.nTransactions += transactionQueue.size();
        unprocessedBlock.getReadLock().unlock();

        AtomicInteger balance = new AtomicInteger(seed);

        for (int i = 0; i < toBeProcessedBlocks.nBlocks-1; i++) {
            processBlockForBalance(user, balance, chain.get(i));
        }

        if (chain.size() >= toBeProcessedBlocks.nBlocks+1) {
            processBlockForBalance(user, balance, chain.get(toBeProcessedBlocks.nBlocks-1));
            Block block = chain.get(toBeProcessedBlocks.nBlocks);
            for (int i=0; i < toBeProcessedBlocks.nTransactions; i++) {
                processTransactionForBalance(user, balance, block.getTransactions().get(i));
            }
        }
        else {
            unprocessedBlock.getReadLock().lock();
            if (chain.size() == toBeProcessedBlocks.nBlocks) {
                processBlockForBalance(user, balance, chain.get(toBeProcessedBlocks.nBlocks-1));
                Block block = unprocessedBlock.getBlock();
                for (int i=0; i < toBeProcessedBlocks.nTransactions; i++) {
                    processTransactionForBalance(user, balance, block.getTransactions().get(i));
                }
            } else {
                processBlockForBalance(user, balance, unprocessedBlock.getBlock());
                Iterator iterator = transactionQueue.iterator();
                for (int i=0; i < toBeProcessedBlocks.nTransactions; i++) {
                    processTransactionForBalance(user, balance, (Transaction) iterator.next());
                }
            }
            unprocessedBlock.getReadLock().unlock();
        }

        return balance.get();
    }

    private void processBlockForBalance(User user, AtomicInteger currBalance,Block block) {
        if (user == block.getMiner()) {
            currBalance.set(currBalance.get() + block.getMineReward());
        }
        block.getTransactions().forEach(
                transaction -> processTransactionForBalance(user, currBalance, transaction)
        );
    }

    private void processTransactionForBalance(User user, AtomicInteger currBalance,Transaction transaction) {
        if (transaction.getFrom() == user) {
            currBalance.set(currBalance.get() - transaction.getAmount());
        } else if (transaction.getTo() == user) {
            currBalance.set(currBalance.get() + transaction.getAmount());
        }
    }

    private void updateMiningConstraints() {
        long timeTookForMining = currentMiningBlockEndTimeMs - currentMiningBlockStartTimeMs;

        if (timeTookForMining >= (FIXED_MINING_TIME_MS - ACCEPTABLE_DEVIATION_IN_MINING_TIME_MS)
            && timeTookForMining <= (FIXED_MINING_TIME_MS + ACCEPTABLE_DEVIATION_IN_MINING_TIME_MS)) {
            return;
        }

        if(timeTookForMining < (FIXED_MINING_TIME_MS - ACCEPTABLE_DEVIATION_IN_MINING_TIME_MS)) {
            noOfStartZerosForHash++;
            requiredPrefixForHash = requiredPrefixForHash.concat("0");
            return;
        }

        noOfStartZerosForHash = Math.max(0, --noOfStartZerosForHash);
        requiredPrefixForHash = "0".repeat(noOfStartZerosForHash);
    }
}
