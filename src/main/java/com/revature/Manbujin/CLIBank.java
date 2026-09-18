package com.revature.Manbujin;

import com.revature.Manbujin.API.*;

import java.util.*;

public class CLIBank {
    private static final String usage =
    """
    usage: Manbujin [-h] | [batch <command> -u <username> -p <password>]
    """;

    private static final String prompt = "Manbujin> ";

    private static final String logo =
    """
    __  __             _            _ _      \s
   |  \\/  | __ _ _ __ | |__  _   _ (_|_)_ __ \s
   | |\\/| |/ _` | '_ \\| '_ \\| | | || | | '_ \\\s
   | |  | | (_| | | | | |_) | |_| || | | | | |
   |_|  |_|\\__,_|_| |_|_.__/ \\__,_|/ |_|_| |_|
                                 |__/        \s
   """;

    protected static final String helpTxt =
    """
    Bank of CLI shell commands:
    
    help/?: Show this help
    accounts: List your accounts
    transactions [optional: <your account>] [optional: n]: List recent transactions
    create <PIN> <checking|savings>: create a new account
    delete <your account> <PIN>: delete an account
    deposit <your account> <amount>: Add money
    withdraw <your account> <amount> <PIN>: Remove money
    transfer <your account> <recipient account number> <amount>: Transfer money
    exit/quit: End your session
    """;

    private static API api;

    protected static void printError(String str) {
        System.err.println(str);
        System.out.println(prompt);
    }

    protected static void register() {
        String username, password;
        Scanner sc = new Scanner(System.in);
        boolean registered; //Mo

        do {
            System.out.println("Register for a new account with the Bank of CLI.");
            System.out.print("Username: ");
            username = sc.nextLine();
            System.out.print("Password: ");
            password = sc.nextLine();

            registered = api.register(username, password);

            if (!registered) {
                System.out.println(api.getResult());
                System.out.println();
            }

        } while (!registered);

        System.out.println("Successfully registered. Please log in.");
    }

    protected static boolean login() {
        Scanner sc = new Scanner(System.in);

        System.out.println("Bank of CLI Login");
        System.out.print("Username: ");
        String username = sc.nextLine();
        System.out.print("Password: ");
        String password = sc.nextLine();

        /* Mutates the API object to authorize all future API calls. */
        return api.login(username, password);
    }

    protected static void exec(List<String> args) {
        String cmd = args.getFirst();

        if (cmd.equalsIgnoreCase("exit") || cmd.equalsIgnoreCase("quit")) {
            System.out.println("Goodbye!");
            System.exit(0); /* Normal, planned exit. */
        } else if(cmd.equalsIgnoreCase("create")) {
            if(args.size() < 3) {
                printError("Create: not enough arguments");
                return;
            } else if (args.size() > 3) {
                printError("Create: too many arguments");
                return;
            }
            api.creatAcct(args.get(1), args.get(2));
            System.out.println(api.getResult());
        } else if(cmd.equals("delete")) {
            if(args.size() < 3) {
                printError("Delete: not enough arguments");
                return;
            } else if (args.size() > 3) {
                printError("Delete: too many arguments");
                return;
            }
            api.deleteAcct(args.get(1), args.get(2));
            System.out.println(api.getResult());
        } else if (cmd.equalsIgnoreCase("accounts")) {
            api.getAccts();
            System.out.print(api.getResult());
        } else if (cmd.equalsIgnoreCase("deposit")) {
            try {
                api.deposit(args.get(1), args.get(2));
                System.out.println(api.getResult());
            } catch (Exception e) {
                if (e instanceof IndexOutOfBoundsException)
                    printError("Syntax error: too few arguments.");
            }
        } else if (cmd.equalsIgnoreCase("withdraw")) {
            if(args.size() < 4) {
                printError("Withdraw: not enough arguments");
            } else if(args.size() > 4) {
                printError("Withdraw: too many arguments.");
            } else {
                api.withdraw(args.get(1), args.get(2), args.get(3));
                System.out.println(api.getResult());
            }
        } else if (cmd.equalsIgnoreCase("transactions")) {
            if (args.size() == 1) {
                api.getTransactions();
                System.out.println(api.getResult());
            } else if (args.size() == 2) {
                api.getTransactions(Integer.parseInt(args.get(1)));
                System.out.println(api.getResult());
            } else if (args.size() == 3) {
                int stagedRows = Integer.parseInt(args.get(1));
                if (stagedRows <= 1000) {
                    int rows = Integer.parseInt(args.get(2));
                    if (rows < 0) rows = 0;
                    api.getAcctTransactions(args.get(1), rows);
                    System.out.print(api.getResult());
                }
            } else {
                printError("Syntax error");
            }
        } else if(cmd.equalsIgnoreCase("transfer")) {
            if(args.size() < 4) {
                printError("Not enough arguments.");
            } else if(args.size() > 4) {
                printError("Too many arguments.");
            } else {
                api.transfer(args.get(1), args.get(2), args.get(3));
                System.out.println(api.getResult());
            }
        } else if(cmd.equalsIgnoreCase("help") || cmd.equals("?")) {
            System.out.println(helpTxt);
        } else {
            printError("Unrecognized command");
        }
    }

    protected static void interactive() {
        Scanner sc = new Scanner(System.in);
        System.out.print(logo);

        boolean status = false;
        do {
            System.out.print("Welcome to the Bank of CLI. Are you a new user or returning user? (N/R) ");
            String response = sc.nextLine();
            if (response.equalsIgnoreCase("n")) {
                register();
                status = login();
            } else if (response.equalsIgnoreCase("r")) {
                status = login();
            }
            if(!status && api.getResult() != null) {
                System.out.println(api.getResult());
                System.out.println();
            }

        } while (!status);

        while(true) {
            System.out.print(prompt);
            String line = sc.nextLine();
            exec(Arrays.asList(line.split(" ")));
        }
    }

    public static void main(String[] args) {
        List<String> arguments = Arrays.asList(args);

        api = new API();

        if(arguments.isEmpty()) {
            interactive();
        }

        if(arguments.contains("-h")) {
            System.out.print(CLIBank.usage);
            return;
        }

        if(arguments.getFirst().equalsIgnoreCase("batch")) {
            try {
                api.login(arguments.get(3), arguments.get(5));
            } catch(IndexOutOfBoundsException e) {
                printError("Syntax error. Exiting.");
                return;
            }

            exec(arguments.subList(1, arguments.size()));
        }

    }
}
