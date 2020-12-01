package blockchain.user;

import blockchain.Blockchain;
import java.util.ArrayList;

public class UserFactory {
    private static Blockchain blockchain;
    private static long runningUserId;
    private static ArrayList<User> users;
    private static UserFactory thisObj = null;

    private UserFactory(Blockchain blockchain) {
        UserFactory.blockchain = blockchain;
        runningUserId = 1;
        users = new ArrayList<>();
    }

    public static UserFactory with(Blockchain blockchain) {
        if (thisObj == null) {
            thisObj = new UserFactory(blockchain);
            return thisObj;
        }
        return thisObj;
    }

    public User newUser() {
        User user = User.with(runningUserId++, blockchain);
        users.add(user);
        return user;
    }

    public Miner newMiner() {
        Miner miner = Miner.with(runningUserId++, blockchain);
        users.add(miner);
        return miner;
    }

    public static User getUser(long id) {
        return users.get((int) id - 1);
    }

    public static long getNoOfUsers() {
        return users.size();
    }
}
