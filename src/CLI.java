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
	
	private String[] getCmd() {
		if (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] parameters = line.split(" ");
			return parameters;
		}
		return null;
	}
	
	public void printHelp() {
	    System.out.println("createblockchain -addr \"address\".");
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
	    chain.sendTransaction(from, to, amount, wallet);
	}
	
	private void executeCmd(String[] parameters) throws Exception {
		if (parameters == null) {
			System.out.println("Parsed command is empty.");
			return;
		}
		
//		if (chain.emptyChain() && !parameters[0].equals("createblockchain")) {
//		    System.out.println("Currently there is no blockchain.\n"
//                    + "Use <createblockchain -addr \"address\"> to create a new blockchain.");
//		    return;
//		}
		    
		
		switch (parameters[0]) {
		case "printchain":
			System.out.println(chain);
			break;
		case "createblockchain":
		    if (parameters.length != 3 || !parameters[1].equals("-addr")) {
		        printHelp();
		        break;
		    }
		    chain.createBlockchain(parameters[2]);
		    break;
		case "addtransaction":
			String msg = "";
			// Handle space characters inside the double-quoted parameter 
			for (int i = 1; i < parameters.length; i++) {
				msg += parameters[i];
				msg += " ";
			}
			msg = msg.substring(1, msg.length() - 2);
			chain.newTransaction(Transaction.newCoinbaseTX("kouchibin", "abc"));
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

    @Override
	public void run() {
		while (true) {
			System.out.print("> ");
			String[] parameters = getCmd();
			try {
                executeCmd(parameters);
            } catch (Exception e) {
                e.printStackTrace();
            }
		}
	}
	
}
