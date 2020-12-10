package blockchain.user;

import java.util.Random;
import blockchain.*;
import blockchain.utils.*;

// BLIND MINING STRATERGY IS USED AS THERE ARE NO PHYSICAL MINERS.

public class Miner extends User implements Runnable {

    private Block currentMiningBlock = null;
    private static final int BLIND_REPETITIONS = 100;
    private static final int SLEEP_WHEN_NO_WORK_MS = 1000;
    private Random mineRandom;
    private final Random selectRandom;

    private Miner(long id, Blockchain blockchain) {
        super(id, blockchain);
        selectRandom = new Random();
    }

    public static Miner with(long id, Blockchain blockchain) {
        return new Miner(id, blockchain);
    }

    @Override
    public void run() {
        while (true) {
            int randNum = selectRandom.nextInt();
            if (randNum % id == 0) {
                doTransaction();
            } else {
                try {
                    doMining();
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    // Perform minning of the block if not minned
    private void doMining() throws InterruptedException {
        boolean successful = false;

        while (!successful) {
            updateCurrentMiningBlock();
            if (currentMiningBlock == null) {
                Thread.sleep(SLEEP_WHEN_NO_WORK_MS);
                continue;
            }
            successful = blindMining();
            if (successful) {
                blockchain.submitBlock(currentMiningBlock, this);
            }
        }
    }

    // Initializing magic numbers and computing hash
    private boolean blindMining() {
        String requiredPrefix = blockchain.getRequiredPrefixForHash();
        for (int i = 0; i < BLIND_REPETITIONS; i++) {
            currentMiningBlock.setMagicNum(mineRandom.nextInt());
            String computedHash = StringUtils.applySha256(currentMiningBlock.toString());
            if (computedHash.startsWith(requiredPrefix)) {
                currentMiningBlock.setHash(computedHash);
                return true;
            }
        }
        return false;
    }

    // Set current minning block
    private void updateCurrentMiningBlock() {
        Block block = blockchain.getUnprocessedBlock();
        if (currentMiningBlock == null || !currentMiningBlock.equals(block)) {
            currentMiningBlock = block;
            mineRandom = new Random();

            if (currentMiningBlock != null) {
                currentMiningBlock.setMiner(this);
            }
        }
    }
}