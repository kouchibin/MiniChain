import java.util.Scanner;

public class CLI extends Thread{
	private MiniChain chain;
	private Scanner scanner;
	
	public CLI(MiniChain chain) {
		this.chain = chain;
		scanner = new Scanner(System.in);
	}
	
	private String[] getCmd() {
		if (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] parameters = line.split(" ");
			return parameters;
		}
		return null;
	}
	
	private void executeCmd(String[] parameters) {
		if (parameters == null) {
			System.out.println("Parsed command is empty.");
			return;
		}
		switch (parameters[0]) {
		case "printchain":
			System.out.println(chain);
			break;
		case "addtransaction":
			String msg = "";
			for (int i = 1; i < parameters.length; i++) {
				msg += parameters[i];
				msg += " ";
			}
			msg = msg.substring(1, msg.length()-2);
			chain.newTransaction(msg);
			break;
		default:
			System.out.println("Unknown command.");
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
