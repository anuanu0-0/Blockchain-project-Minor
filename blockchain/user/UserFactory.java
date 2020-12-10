package blockchain.user;

import blockchain.Blockchain;
import java.util.ArrayList;

public class UserFactory {
    private static Blockchain blockchain;
    private static long runningUserId;
    private static ArrayList<User> users;
    private static UserFactory thisObj = null;

    // Initialize objects and data members
    private UserFactory(Blockchain blockchain) {
        UserFactory.blockchain = blockchain;
        runningUserId = 1;
        users = new ArrayList<>();
    }

    // Create & return new object if null
    public static UserFactory with(Blockchain blockchain) {
        if (thisObj == null) {
            thisObj = new UserFactory(blockchain);
            return thisObj;
        }
        return thisObj;
    }

    //  Create add and return user object to ArrayList
    public User newUser() {
        User user = User.with(runningUserId++, blockchain);
        users.add(user);
        return user;
    }
    //  Create add and return miner object to ArrayList
    public Miner newMiner() {
        Miner miner = Miner.with(runningUserId++, blockchain);
        users.add(miner);
        return miner;
    }

    // Return User id
    public static User getUser(long id) {
        return users.get((int) id - 1);
    }

    // Return size of arrayListdoTransaction
    public static long getNoOfUsers() {
        return users.size();
    }
}
