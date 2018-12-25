import java.io.IOException;
import java.util.Scanner;

public class CLI extends Thread{
	private MiniChain chain;
	private Scanner scanner;
	
	public CLI(MiniChain chain) {
	    this.chain = chain;
		scanner = new Scanner(System.in);
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
	
	public void send(String[] parameters) {
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
	    chain.sendTransaction(from, to, amount);
	}
	
	private void executeCmd(String[] parameters) {
		if (parameters == null) {
			System.out.println("Parsed command is empty.");
			return;
		}
		
		if (chain.emptyChain() && !parameters[0].equals("createblockchain")) {
		    System.out.println("Currently there is no blockchain.\n"
                    + "Use <createblockchain -addr \"address\"> to create a new blockchain.");
		    return;
		}
		    
		
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
		    if (parameters.length != 3 || !parameters[1].equals("-addr")) {
                printHelp();
                break;
            }
		    System.out.println("Balance:" + chain.getBalance(parameters[2]));
		    break;
		case "send":
		    send(parameters);
		    break;
		default:
			printHelp();
			break;
		}
	}
	
	@Override
	public void run() {
		while (true) {
			System.out.print("> ");
			String[] parameters = getCmd();
			executeCmd(parameters);
		}
	}
	
}
