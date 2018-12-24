
public class Miner extends Thread {

    private final MiniChain chain;

    public Miner(MiniChain chain) {
        this.chain = chain;
    }

    private Transaction[] getTransactions() {
        return chain.getNewTransactions();
    }

    public void run() {
        while (true) {
            Transaction[] transactions = getTransactions();
            if (transactions == null) {
                // Nothing to do. Can be changed to block state in the future.
                continue;
            }
            String previousHash = chain.getLatesBlockHash();
            Block block = new Block(transactions, previousHash);
            Block minedBlock = PoW.mineBlock(block, chain.getDifficulty());
            System.out.println("Mined a new block:");
            System.out.println(minedBlock);
            chain.submit(minedBlock);
        }

    }
}
