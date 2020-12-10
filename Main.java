package blockchain;

import blockchain.user.UserFactory;

import java.util.concurrent.Executors;

public class Main {

    // UserFactory - User related methods
    // Executors - public class
    public static void main(String[] args) {
        var driver = BlockchainDriver.newDriver();
        var blockchain = driver.getBlockchain();
        var userFactory = UserFactory.with(blockchain);

        // Creates a thread pool that reuses a fixed number of
        // threads operating off a shared unbounded queue.
        var executor = Executors.newFixedThreadPool(20);
        for (int i = 0; i < 10; i++) {
            executor.submit(userFactory.newUser());
        }
        for (int i = 0; i < 10; i++) {
            executor.submit(userFactory.newMiner());
        }

        // Number of miners = 15
        for (int i = 0; i < 15; i++) {
            while (blockchain.getLength() < i + 1) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignore) {

                }
            }
            printBlock(blockchain.getBlock(i));
            System.out.print(i < 14 ? "\n" : "");
        }

        executor.shutdownNow();
    }

    private static void printBlock(Block block) {
        System.out.println("Block:");
        System.out.println("Created by: " + block.getMiner().getName());
        System.out.println(block.getMiner().getName() + " gets " + block.getMineReward() + " VC");
        System.out.println("Id: " + block.getId());
        System.out.println("Timestamp: " + block.getTimestamp());
        System.out.println("Magic number: " + block.getMagicNum());
        System.out.println("Hash of the previous block: \n" + block.getPrevBlockHash());
        System.out.println("Hash of the block: \n" + block.getHash());
        System.out.println("Block data: " + extractTransactions(block));
        System.out.printf("Block was generating for %d seconds\n", block.getTimeTookForMiningMs() / 1000);
        System.out.println(nValueStatus(block));
    }

    private static int nValue = 0;
    // Current Status of the block miner is executing
    private static String nValueStatus(Block block) {
        long timeTook = block.getTimeTookForMiningMs();
        int fixedMiningTime = Blockchain.getFixedMiningTimeMs();
        int acceptableDeviation = Blockchain.getAcceptableDeviationInMiningTimeMs();

        if (timeTook >= (fixedMiningTime - acceptableDeviation)
                && timeTook <= (fixedMiningTime + acceptableDeviation)) {
            return "N stays the same";
        }

        if (timeTook < (fixedMiningTime - acceptableDeviation)) {
            return "N was increased to " + ++nValue;
        }

        if (nValue == 0) {
            return "N stays the same";
        }
        nValue--;
        return "N was decreased by 1";
    }

    private static String extractTransactions(Block block) {
        if (block.getTransactions().isEmpty()) {
            return "\nNo transactions";
        } else {
            StringBuilder str = new StringBuilder();
            block.getTransactions()
                    .forEach(transaction -> str.append("\n").append(transaction.getFrom().getName()).append(" sent ")
                            .append(transaction.getAmount()).append(" VC to ").append(transaction.getTo().getName()));
            return str.toString();
        }
    }
}

/*
TO DO:
Create blocks and make them secured and connected. - LinkedList : Done
Making cryptocurrency mining for command line: generate magic numbers and store them in blocks. : Done
Generating random numbers in several threads; when a miner finds the magic number, a blockchain is created, and mining goes on : Done
Creating a user defined DS, storing data in the blocks: adding transaction messages to the blockchain.  : Done
Adding encryption with private and public keys to ensure a high level of privacy and security - MessageDigest.  : Done
Creating a cryptocurrency : starting with an amount of zero coins, and virtual miners will get a hundred for each block they mine. : Done
Correcting Project Structure : Done
Fixing errors (Logical) : Done Partially
Add to Github : Done

TOOLS : HASHING(+Cryptographic hashing), MULTITHREADING, (DE)SERIALIZATION, STREAM API,
 */