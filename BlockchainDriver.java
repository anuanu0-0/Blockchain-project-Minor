package blockchain;

import java.io.IOException;
import blockchain.utils.*;

public class BlockchainDriver {
    String fileName;
    Blockchain blockchain;

    private BlockchainDriver() {
        fileName = null;
        blockchain = null;
    }

    public static BlockchainDriver newDriver() {
        BlockchainDriver driver = new BlockchainDriver();
        driver.fileName = driver.toString();
        return driver;
    }

    public Blockchain getBlockchain() {
        if (blockchain != null && blockchain.isValid()) { return blockchain; }

        try {
            blockchain = (Blockchain) SerializationUtils.deserialize(fileName);
        } catch(Exception ignored) {

        } finally {
            if (blockchain == null || !blockchain.isValid()) {
                blockchain = Blockchain.generateBlockchain(this);
            }
        }

        return blockchain;
    }

    public boolean saveBlockchain() {
        try {
            SerializationUtils.serialize(blockchain, fileName);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }
}