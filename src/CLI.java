import java.io.IOException;
import java.util.Scanner;

public class CLI extends Thread{
	private MiniChain chain;
	private Scanner scanner;
	private Wallets wallets;
	
	public CLI(MiniChain chain) {
	    this.chain = chain;
		scanner = new Scanner(System.in);
		wallets = new Wallets();
	}
	
	public void setChain(MiniChain chain) {
        this.chain = chain;
    }
	
	@Override
    public void run() {
        try {
            executionLoop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	private void executionLoop() throws Exception {
	    while (true) {
            System.out.print("> ");
            String[] parameters = getCmd();
            executeCmd(parameters);
        }
	}

	private String[] getCmd() {
		if (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] parameters = line.split(" ");
			return parameters;
		}
		return null;
	}
	
	private void executeCmd(String[] parameters) throws Exception {
        if (parameters == null) {
            System.out.println("ERROR: Parsed command empty.");
            return;
        }
        switch (parameters[0]) {
        case "printchain":
            printChain();
            break;
        case "createblockchain":
            createBlockchain(parameters);
            break;
        case "getbalance":
            getBalance(parameters);
            break;
        case "createwallet":
            createWallet(parameters);
            break;
        case "send":
            send(parameters);
            break;
        default:
            printHelp();
            break;
        }
    }
	
	public void printChain() {
	    System.out.println(chain);
	}
	
	public void createBlockchain(String[] parameters) {
	    if (parameters.length != 3 || !parameters[1].equals("-addr")) 
            printHelp();
        chain.createBlockchain(parameters[2]);
	}
	
	private void getBalance(String[] parameters) {
        if (parameters.length != 3 || !parameters[1].equals("-addr")) {
            printHelp();
            return;
        }
        System.out.println("Balance:" + chain.getBalance(parameters[2]));
    }
	
	private void createWallet(String[] parameters) throws Exception {
        if (parameters.length != 1) {
            printHelp();
            return;
        }
        Wallet w = wallets.newWallet();
        System.out.println("Your new address:" + w.address);
    }
	
	public void send(String[] parameters) throws Exception {
        String from = "";
        String to = "";
        int amount = 0;
        if (parameters.length != 7) {
            printHelp();
            return;
        }
            
        for (int i = 1; i < parameters.length; i++) {
            if (parameters[i].equals("-from"))
                from = parameters[i+1];
            else if (parameters[i].equals("-to"))
                to = parameters[i+1];
            else if (parameters[i].equals("-amount"))
                amount = Integer.parseInt(parameters[i+1]);
        }
        
        Wallet wallet = wallets.get(from);
        chain.addSendingTransaction(from, to, amount, wallet);
    }
	
	public void printHelp() {
	    System.out.println("createblockchain -addr \"address\".");
	}

}
