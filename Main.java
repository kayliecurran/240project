import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Scanner;

public class Main {
    private static HashMap<String, User> users = new HashMap<>();
    private static User mainUser;

    public static void createNewUser() throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        String newUsername = null;
        System.out.println("Create a new account");
        System.out.print("Please create a username: ");
        Scanner in = new Scanner(System.in);
        newUsername = in.nextLine();
        for (String id : users.keySet()) {
            if (id.equals(newUsername)) {
                while (id.equals(newUsername)) {
                    System.out.print("This username is taken. Please enter a different username: ");
                    Scanner newIn = new Scanner(System.in);
                    newUsername = newIn.nextLine();
                }
            }
        }
        File file = new File("./users/" + newUsername + ".txt");
        file.createNewFile();

        PrintWriter output = new PrintWriter("./users/" + newUsername + ".txt");
        output.println(newUsername);
        System.out.print("Please create a password: ");
        Scanner in2 = new Scanner(System.in);
        String password = in2.nextLine();
        //next 3 lines creates salt
        //password hashing - "the salt" is what is used to transform the password so each salt would need to be stored with password, so added a new variable to the user object to include this
        byte[] salt = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);

        System.out.println(salt);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = f.generateSecret(spec).getEncoded();
        Base64.Encoder enc = Base64.getEncoder();
        String theSalt = enc.encodeToString(salt);
        password = enc.encodeToString(hash);
        output.println(password);
        output.println(salt);

        System.out.println("First name:");
        Scanner in3 = new Scanner(System.in);
        String fname = in3.nextLine();
        output.println(fname);
        System.out.println("Last name:");
        Scanner in4 = new Scanner(System.in);
        String lname = in4.nextLine();
        output.println(lname);
        mainUser = new User(newUsername, password, theSalt, fname, lname, 0.0, 0.0);
        users.put(newUsername,/*new User(newUsername,password,theSalt,fname,lname,0.0,0.0)*/mainUser);
        System.out.println("Your account has been successfully created.");
        System.out.println("");
        output.println(0.00);
        output.println(0.00);
        output.println(0.00);
        output.print(0.00);
        output.close();
    }

    public static void addToHashFromFile() throws FileNotFoundException {
        User user = null;
        String filename;
        File dir = new File("./users");
        File[] directoryListing = dir.listFiles();
        for (File aFile : directoryListing) {
            filename = aFile.getName();
            filename = filename.substring(0, filename.length() - 4);
            FileReader reader = new FileReader(aFile);
            Scanner in = new Scanner(reader);
            while (in.hasNext()) {
                user = new User(in);
                if (in.hasNext()) {
                    in.nextLine();
                }
            }
            users.put(filename, user);
        }
    }

    public static boolean login() throws NoSuchAlgorithmException, InvalidKeySpecException, FileNotFoundException {
        String filename;
        String fileUsername = null;
        String matchFile = null;
        System.out.println("Username: ");
        Scanner in = new Scanner(System.in);
        String loginUsername = in.nextLine();
        File dir = new File("./users");
        File[] directoryListing = dir.listFiles();
        for (File aFile : directoryListing) {
            filename = aFile.getName();
            fileUsername = filename.substring(0, filename.length() - 4);
            if (fileUsername.equals(loginUsername)) {
                matchFile = aFile.getName();
            }
        }
        if (matchFile == null) {
            System.out.println("Invalid Username.");
            return false;
        } else {
            System.out.println("Password: ");
            Scanner in2 = new Scanner(System.in);
            String pass = in2.nextLine();
            for (String userName : users.keySet()) {
                if (userName.equals(fileUsername)) {
                    mainUser = users.get(userName);
                }
            }
            String salt = mainUser.getSalt();
            byte[] theSalt = salt.getBytes(StandardCharsets.UTF_8);
            System.out.println(theSalt);
            KeySpec spec = new PBEKeySpec(pass.toCharArray(), theSalt, 65536, 128);
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = f.generateSecret(spec).getEncoded();
            Base64.Encoder enc = Base64.getEncoder();
            String newPass = enc.encodeToString(hash);
            System.out.println(newPass);
            String userPass = mainUser.getPassword();
            System.out.println(userPass);
            if (pass.equals(userPass)) {
                return true;
            } else {
                return false;
            }
        }
    }


    public static void balance() {
        System.out.println("Which account balance would you like to see?");
        System.out.println("1. Checking");
        System.out.println("2. Savings");
        System.out.println("3. Money Market");
        System.out.println("4. Certificate Deposit");
        System.out.println("5. All");
        Scanner in = new Scanner(System.in);
        int choice = in.nextInt();
        if (choice == 1) {
            mainUser.balanceChecking();
        } else if (choice == 2) {
            mainUser.balanceSavings();
        } else if (choice == 3) {
            mainUser.balanceMoneyMarket();
        } else if (choice == 4) {
            mainUser.balanceCertificateDeposit();
        } else if (choice == 5) {
            mainUser.balance();
        } else {
            System.out.println("You did not select a valid account");
            System.out.println("");
        }
    }

    public static void deposit() throws IOException {
        System.out.println("Which account would you like to deposit in to?");
        System.out.println("1. Checking");
        System.out.println("2. Savings");
        System.out.println("3. Money Market");
        System.out.println("4. Certificate Deposit");
        Scanner in = new Scanner(System.in);
        int choice = in.nextInt();

        System.out.println("How much would you like to deposit?");
        double depositAmount = in.nextDouble();

        if (choice == 1) {
            mainUser.depositChecking(depositAmount);
        } else if (choice == 2) {
            mainUser.depositSavings(depositAmount);
        } else if (choice == 3) {
            mainUser.depositMoneyMarket(depositAmount);
        } else if (choice == 4) {
            mainUser.depositCertificateDeposit(depositAmount);
        } else {
            System.out.println("You did not select a valid account");
            System.out.println("");
        }


    }

    public static void withdraw() throws IOException {
        System.out.println("Which account would you like to withdraw from?");
        System.out.println("1. Checking");
        System.out.println("2. Savings");
        System.out.println("3. Money Market");
        System.out.println("4. Certificate Deposit");
        Scanner in = new Scanner(System.in);
        int choice = in.nextInt();

        System.out.println("How much would you like to withdraw?");
        double withdrawAmount = in.nextDouble();

        if (choice == 1) {
            mainUser.withdrawChecking(withdrawAmount);
        } else if (choice == 2) {
            mainUser.withdrawSavings(withdrawAmount);
        } else if (choice == 3) {
            mainUser.withdrawMoneyMarket(withdrawAmount);
        } else if (choice == 4) {
            mainUser.withdrawCertificateDeposit(withdrawAmount);
        }

        File fnew = new File("./users/" + mainUser.getUsername() + ".txt");
        FileWriter f = new FileWriter(fnew, false);
        f.write(mainUser.getUsername() + "\n");
        f.write(mainUser.getPassword() + "\n");
        f.write(mainUser.getSalt() + "\n");
        f.write(mainUser.getFname() + "\n");
        f.write(mainUser.getLname() + "\n");
        f.write(String.valueOf(mainUser.getChecking()) + "\n");
        f.write(String.valueOf(mainUser.getSavings()) + "\n");
        f.write(String.valueOf(mainUser.getMM()) + "\n");
        f.write(String.valueOf(mainUser.getCD()) + "\n");
        f.close();


    }

    public static void transfer() throws IOException {
        Scanner in = new Scanner(System.in);
        System.out.println("Which account would you like to transfer money from?");
        System.out.println("1. Checking");
        System.out.println("2. Savings");
        System.out.println("3. Money Market");
        System.out.println("4. Certificate Deposit");
        int account1 = in.nextInt();

        Scanner in2 = new Scanner(System.in);
        System.out.println("Which account would you like to transfer money to?");
        System.out.println("1. Checking");
        System.out.println("2. Savings");
        System.out.println("3. Money Market");
        System.out.println("4. Certificate Deposit");
        int account2 = in.nextInt();

        System.out.println("How much money would you like to transfer?");
        Scanner in3 = new Scanner(System.in);
        double transferAmount = in.nextDouble();
        if (account1 == 1 && account2 == 2) {
            mainUser.transferCheckingSaving(transferAmount);
        } else if (account1 == 1 && account2 == 3) {
            mainUser.transferCheckingMM(transferAmount);
        } else if (account1 == 1 && account2 == 4) {
            mainUser.transferCheckingCD(transferAmount);
        } else if (account1 == 2 && account2 == 1) {
            mainUser.transferSavingsChecking(transferAmount);
        } else if (account1 == 2 && account2 == 3) {
            mainUser.transferSavingsMM(transferAmount);
        } else if (account1 == 2 && account2 == 4) {
            mainUser.transferSavingsCD(transferAmount);
        } else if (account1 == 3 && account2 == 1) {
            mainUser.transferMMChecking(transferAmount);
        } else if (account1 == 3 && account2 == 2) {
            mainUser.transferMMSavings(transferAmount);
        } else if (account1 == 3 && account2 == 4) {
            mainUser.transferMMCD(transferAmount);
        } else if (account1 == 4 && account2 == 1) {
            mainUser.transferCDChecking(transferAmount);
        } else if (account1 == 4 && account2 == 2) {
            mainUser.transferCDSavings(transferAmount);
        } else if (account1 == 4 && account2 == 3) {
            mainUser.transferCDMM(transferAmount);
        } else {
            System.out.println("Unable to transfer, please try again");
        }

        File fnew = new File("./users/" + mainUser.getUsername() + ".txt");
        FileWriter f = new FileWriter(fnew, false);
        f.write(mainUser.getUsername() + "\n");
        f.write(mainUser.getPassword() + "\n");
        f.write(mainUser.getSalt() + "\n");
        f.write(mainUser.getFname() + "\n");
        f.write(mainUser.getLname() + "\n");
        f.write(String.valueOf(mainUser.getChecking()) + "\n");
        f.write(String.valueOf(mainUser.getSavings()) + "\n");
        f.write(String.valueOf(mainUser.getMM()) + "\n");
        f.write(String.valueOf(mainUser.getCD()) + "\n");
        f.close();
    }

    public static void loan() {
        Scanner in = new Scanner(System.in);
        System.out.println("How much would you like to request a loan for?");
        double loanAmount = in.nextDouble();

        Scanner in2 = new Scanner(System.in);
        System.out.println("What is the reason you are requesting a loan?");
        String loanReason = in2.nextLine();

        Scanner in3 = new Scanner(System.in);
        System.out.println("What is your current salary?");
        double salary = in3.nextDouble();

        System.out.println("Thank you for your request. It will be reviewed and you will be notified when it has been approved");
        System.out.println("");
    }

    public static void main(String[] args) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        //Line below initializes all the windows and displays the login window.
//        Window win = new Window();
        addToHashFromFile();
        JFrame frame = new JFrame("Welcome");
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.setSize(300, 200);
        JPanel panel = new JPanel();
        JLabel label = new JLabel();
        JButton button1 = new JButton("Login");
        button1.addActionListener(new ButtonListener());
        JButton button2 = new JButton("Create account");
        button2.addActionListener(new ButtonListener2());
        JButton button3 = new JButton("Exit");
        button3.addActionListener(new ButtonListener3());
        label.setText("Welcome to your mobile banking account!");
        panel.setSize(400, 100);
        panel.add(label);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel panel2 = new JPanel();
        panel2.setSize(400, 100);
        panel2.add(button1);
        panel2.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel panel3 = new JPanel();
        panel3.setSize(400, 100);
        panel3.add(button2);
        panel3.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel panel4 = new JPanel();
        panel4.setSize(400, 100);
        panel4.add(button3);
        panel4.setAlignmentX(Component.CENTER_ALIGNMENT);
        frame.add(panel);
        frame.add(panel2);
        frame.add(panel3);
        frame.add(panel4);
        frame.setVisible(true);
//        System.out.println("Welcome to your mobile bank account!");
        int choice = 0;
        while (choice != 3) {
            System.out.println("1. Login to existing account");
            System.out.println("2. Create an account");
            System.out.println("3. Exit");
            System.out.print(": ");
            Scanner in = new Scanner(System.in);
            choice = in.nextInt();
            if (choice == 1) {
                if (login() == true) {
                    int choice1 = 0;
                    while (choice1 != 6) {
                        System.out.println("Main Menu");
                        System.out.println("Manage your bank accounts");
                        System.out.println("1. See account balance");
                        System.out.println("2. Make a deposit");
                        System.out.println("3. Withdraw money");
                        System.out.println("4. Transfer money");
                        System.out.println("5. Request a loan");
                        System.out.println("6. Log out");
                        System.out.print(": ");
                        Scanner in2 = new Scanner(System.in);
                        choice1 = in.nextInt();
                        if (choice1 == 1) {
                            balance();
                        } else if (choice1 == 2) {
                            deposit();
                        } else if (choice1 == 3) {
                            withdraw();
                        } else if (choice1 == 4) {
                            transfer();
                        } else if (choice1 == 5) {
                            loan();
                        } else if (choice1 == 6) {
                            continue;
                        }
                    }
                } else {
                    continue;
                }
            } else if (choice == 2) {
                createNewUser();
                int choice2 = 0;
                while (choice2 != 6) {
                    System.out.println("Main Menu");
                    System.out.println("Manage your bank accounts");
                    System.out.println("1. See account balance");
                    System.out.println("2. Make a deposit");
                    System.out.println("3. Withdraw money");
                    System.out.println("4. Transfer money");
                    System.out.println("5. Request a loan");
                    System.out.println("6. Log out");
                    System.out.print(": ");
                    Scanner in2 = new Scanner(System.in);
                    choice2 = in.nextInt();
                    if (choice2 == 1) {
                        balance();
                    } else if (choice2 == 2) {
                        deposit();
                    } else if (choice2 == 3) {
                        withdraw();
                    } else if (choice2 == 4) {
                        transfer();
                    } else if (choice2 == 5) {
                        loan();
                    } else if (choice2 == 6) {
                        continue;
                    }
                }
            } else if (choice == 3) {
                System.exit(0);
            }
        }
    }


    public static User getMainUser() {
        return mainUser;
    }

    public static HashMap<String, User> getUsers() {
        return users;
    }
}

    class ButtonListener implements ActionListener {
        public static JTextField loginUserName;
        public static JTextField loginPassword;

        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Login");
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
            frame.setSize(300, 400);
            JPanel panel = new JPanel();
            JLabel label = new JLabel();
            label.setText("Login to account");
            panel.setSize(400, 50);
            panel.add(label);
            panel.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel2 = new JPanel();
            panel2.setSize(400, 100);
            JLabel label2 = new JLabel();
            label2.setText("Username:");
            panel2.add(label2);
            loginUserName = new JTextField(20);
            panel2.add(loginUserName);
            JLabel label3 = new JLabel();
            label3.setText("Password:");
            panel2.add(label3);
            loginPassword = new JTextField(20);
            panel2.add(loginPassword);
            panel2.setAlignmentX(Component.CENTER_ALIGNMENT);
            frame.add(panel);
            frame.add(panel2);
            JPanel panel3 = new JPanel();
            JButton button4 = new JButton("Login");
            button4.addActionListener(new ButtonListener23());
            panel3.add(button4);
            panel3.setAlignmentX(Component.CENTER_ALIGNMENT);
            frame.add(panel3);
            frame.setVisible(true);
        }

    }

    class ButtonListener2 implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Register User");
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
            frame.setSize(300, 400);
            JPanel panel = new JPanel();
            JLabel label = new JLabel();
            label.setText("Create an account");
            panel.setSize(400, 50);
            panel.add(label);
            panel.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel2 = new JPanel();
            panel2.setSize(400, 100);
            JLabel label2 = new JLabel();
            label2.setText("Username:");
            panel2.add(label2);
            JTextField createUserName = new JTextField(20);
            panel2.add(createUserName);
            JLabel label3 = new JLabel();
            label3.setText("Password:");
            panel2.add(label3);
            JTextField createPassword = new JTextField(20);
            panel2.add(createPassword);
            JLabel label4 = new JLabel();
            label4.setText("First Name:");
            panel2.add(label4);
            JTextField createFname = new JTextField(20);
            panel2.add(createFname);
            JLabel label5 = new JLabel();
            label5.setText("Last Name:");
            panel2.add(label5);
            JTextField createLname = new JTextField(20);
            panel2.add(createLname);
            panel2.setAlignmentX(Component.CENTER_ALIGNMENT);
            frame.add(panel);
            frame.add(panel2);
            JPanel panel3 = new JPanel();
            JButton button4 = new JButton("Create Account");
            button4.addActionListener(new ButtonListener());
            panel3.add(button4);
            panel3.setAlignmentX(Component.CENTER_ALIGNMENT);
            frame.add(panel3);
            frame.setVisible(true);
        }

    }

    class ButtonListener3 implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Login");
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
            frame.setSize(300, 400);
            JPanel panel = new JPanel();
        }

    }

    class ButtonListener4 implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Login");
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
            frame.setSize(300, 400);
            JPanel panel = new JPanel();
            JLabel label = new JLabel();
            JButton button1 = new JButton("Check Balance");
            button1.addActionListener(new ButtonListener5());
            JButton button2 = new JButton("Deposit");
            button2.addActionListener(new ButtonListener6());
            JButton button3 = new JButton("Withdraw");
            button3.addActionListener(new ButtonListener7());
            JButton button4 = new JButton("Transfer");
            button4.addActionListener(new ButtonListener8());
            JButton button5 = new JButton("Request a Loan");
            button5.addActionListener(new ButtonListener9());
            JButton button6 = new JButton("Exit");
            button5.addActionListener(new ButtonListener3());
            label.setText("Main Menu");
            panel.setSize(400, 100);
            panel.add(label);
            panel.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel2 = new JPanel();
            panel2.setSize(400, 100);
            panel2.add(button1);
            panel2.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel3 = new JPanel();
            panel3.setSize(400, 100);
            panel3.add(button2);
            panel3.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel4 = new JPanel();
            panel4.setSize(400, 100);
            panel4.add(button3);
            panel4.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel5 = new JPanel();
            panel5.setSize(400, 100);
            panel5.add(button4);
            panel5.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel6 = new JPanel();
            panel6.setSize(400, 100);
            panel6.add(button5);
            panel6.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel7 = new JPanel();
            panel7.setSize(400, 100);
            panel7.add(button6);
            panel7.setAlignmentX(Component.CENTER_ALIGNMENT);
            frame.add(panel);
            frame.add(panel2);
            frame.add(panel3);
            frame.add(panel4);
            frame.add(panel5);
            frame.add(panel6);
            frame.add(panel7);
            frame.setVisible(true);
        }

    }

    class ButtonListener5 implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Balance");
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
            frame.setSize(300, 400);
            JPanel panel = new JPanel();
            JLabel label = new JLabel();
            JButton button1 = new JButton("Checking Account");
            button1.addActionListener(new ButtonListener10());
            JButton button2 = new JButton("Savings Account");
            button2.addActionListener(new ButtonListener11());
            JButton button3 = new JButton("Money Market Account");
            button2.addActionListener(new ButtonListener12());
            JButton button4 = new JButton("Certificate of Deposit Account");
            button4.addActionListener(new ButtonListener13());
            JButton button5 = new JButton("All Accounts");
            button5.addActionListener(new ButtonListener14());
            label.setText("Select the account balance you want to see:");
            panel.setSize(400, 100);
            panel.add(label);
            panel.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel2 = new JPanel();
            panel2.setSize(400, 100);
            panel2.add(button1);
            panel2.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel3 = new JPanel();
            panel3.setSize(400, 100);
            panel3.add(button2);
            panel3.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel4 = new JPanel();
            panel4.setSize(400, 100);
            panel4.add(button3);
            panel4.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel5 = new JPanel();
            panel5.setSize(400, 100);
            panel5.add(button4);
            panel5.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel6 = new JPanel();
            panel6.setSize(400, 100);
            panel6.add(button5);
            panel6.setAlignmentX(Component.CENTER_ALIGNMENT);
            frame.add(panel);
            frame.add(panel2);
            frame.add(panel3);
            frame.add(panel4);
            frame.add(panel5);
            frame.add(panel6);
            frame.setVisible(true);
        }

    }

    class ButtonListener6 implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Deposit");
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
            frame.setSize(300, 400);
            JPanel panel = new JPanel();
            JLabel label = new JLabel();
            JButton button1 = new JButton("Checking Account");
            button1.addActionListener(new ButtonListener15());
            JButton button2 = new JButton("Savings Account");
            button2.addActionListener(new ButtonListener16());
            JButton button3 = new JButton("Money Market Account");
            button2.addActionListener(new ButtonListener17());
            JButton button4 = new JButton("Certificate of Deposit Account");
            button4.addActionListener(new ButtonListener18());
            label.setText("Select an account to deposit in to:");
            panel.setSize(400, 100);
            panel.add(label);
            panel.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel2 = new JPanel();
            panel2.setSize(400, 100);
            panel2.add(button1);
            panel2.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel3 = new JPanel();
            panel3.setSize(400, 100);
            panel3.add(button2);
            panel3.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel4 = new JPanel();
            panel4.setSize(400, 100);
            panel4.add(button3);
            panel4.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel5 = new JPanel();
            panel5.setSize(400, 100);
            panel5.add(button4);
            panel5.setAlignmentX(Component.CENTER_ALIGNMENT);
            frame.add(panel);
            frame.add(panel2);
            frame.add(panel3);
            frame.add(panel4);
            frame.add(panel5);
            frame.setVisible(true);
        }

    }


    class ButtonListener7 implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Withdraw");
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
            frame.setSize(300, 400);
            JPanel panel = new JPanel();
            JLabel label = new JLabel();
            JButton button1 = new JButton("Checking Account");
            button1.addActionListener(new ButtonListener19());
            JButton button2 = new JButton("Savings Account");
            button2.addActionListener(new ButtonListener20());
            JButton button3 = new JButton("Money Market Account");
            button2.addActionListener(new ButtonListener21());
            JButton button4 = new JButton("Certificate of Deposit Account");
            button4.addActionListener(new ButtonListener22());
            label.setText("Select an account to withdraw from:");
            panel.setSize(400, 100);
            panel.add(label);
            panel.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel2 = new JPanel();
            panel2.setSize(400, 100);
            panel2.add(button1);
            panel2.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel3 = new JPanel();
            panel3.setSize(400, 100);
            panel3.add(button2);
            panel3.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel4 = new JPanel();
            panel4.setSize(400, 100);
            panel4.add(button3);
            panel4.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel5 = new JPanel();
            panel5.setSize(400, 100);
            panel5.add(button4);
            panel5.setAlignmentX(Component.CENTER_ALIGNMENT);
            frame.add(panel);
            frame.add(panel2);
            frame.add(panel3);
            frame.add(panel4);
            frame.add(panel5);
            frame.setVisible(true);
        }

    }

    class ButtonListener8 implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Transfer");
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
            frame.setSize(300, 400);
            JPanel panel = new JPanel();
            JLabel label = new JLabel();
            JButton button1 = new JButton("Checking Account");
            button1.addActionListener(new ButtonListener5());
            JButton button2 = new JButton("Savings Account");
            button2.addActionListener(new ButtonListener5());
            JButton button3 = new JButton("Money Market Account");
            button2.addActionListener(new ButtonListener5());
            JButton button4 = new JButton("Certificate of Deposit Account");
            button4.addActionListener(new ButtonListener5());
            label.setText("Select an account to transfer from:");
            panel.setSize(400, 100);
            panel.add(label);
            panel.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel2 = new JPanel();
            panel2.setSize(400, 100);
            panel2.add(button1);
            panel2.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel3 = new JPanel();
            panel3.setSize(400, 100);
            panel3.add(button2);
            panel3.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel4 = new JPanel();
            panel4.setSize(400, 100);
            panel4.add(button3);
            panel4.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel panel5 = new JPanel();
            panel5.setSize(400, 100);
            panel5.add(button4);
            panel5.setAlignmentX(Component.CENTER_ALIGNMENT);
            frame.add(panel);
            frame.add(panel2);
            frame.add(panel3);
            frame.add(panel4);
            frame.add(panel5);
            frame.setVisible(true);
        }

    }

    class ButtonListener9 implements ActionListener {
        static JTextField amount;
        static JTextField reason;
        static JTextField salary;

        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Loan Request");
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
            frame.setSize(300, 400);
            JPanel panel = new JPanel();
            JLabel label = new JLabel();
            label.setText("How much would you like to request a loan for?");
            panel.setPreferredSize(new Dimension(100, 100));
            panel.add(label);
            amount = new JTextField(8);
            JPanel panel2 = new JPanel();
            panel2.setPreferredSize(new Dimension(100, 100));
            amount.setSize(50, 50);
            panel2.add(amount);

            JPanel panel3 = new JPanel();
            JLabel label2 = new JLabel();
            label2.setText("What is the reason you are requesting a loan?");
            panel3.setPreferredSize(new Dimension(100, 100));
            panel3.add(label2);
            reason = new JTextField(8);
            JPanel panel4 = new JPanel();
            panel4.setPreferredSize(new Dimension(100, 100));
            reason.setSize(50, 50);
            panel4.add(reason);

            JPanel panel5 = new JPanel();
            JLabel label3 = new JLabel();
            label3.setText("What is your current salary?");
            panel5.setPreferredSize(new Dimension(100, 100));
            panel5.add(label3);
            salary = new JTextField(8);
            JPanel panel6 = new JPanel();
            panel6.setPreferredSize(new Dimension(100, 100));
            salary.setSize(50, 50);
            panel6.add(salary);

            JPanel panel7 = new JPanel();
            panel7.setPreferredSize(new Dimension(100, 100));
            JButton button = new JButton("Submit Request");
            panel7.add(button);

            frame.add(panel);
            frame.add(panel2);
            frame.add(panel3);
            frame.add(panel4);
            frame.add(panel5);
            frame.add(panel6);
            frame.add(panel7);
            frame.setVisible(true);

        }

    }

    class ButtonListener10 implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            User user = Main.getMainUser();
            JFrame frame = new JFrame("Account Balance");
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
            frame.setSize(300, 400);
            JLabel label = new JLabel();
            label.setText("Checking account balance: " + user.getChecking());
            frame.add(label);
            frame.setVisible(true);
        }
    }

    class ButtonListener11 implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            User user = Main.getMainUser();
            JFrame frame = new JFrame("Account Balance");
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
            frame.setSize(300, 400);
            JLabel label = new JLabel();
            label.setText("Savings account balance: " + user.getSavings());
            frame.add(label);
            frame.setVisible(true);
        }
    }

    class ButtonListener12 implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            User user = Main.getMainUser();
            JFrame frame = new JFrame("Account Balance");
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
            frame.setSize(300, 400);
            JLabel label = new JLabel();
            label.setText("Money market account balance: " + user.getMM());
            frame.add(label);
            frame.setVisible(true);
        }
    }

    class ButtonListener13 implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            User user = Main.getMainUser();
            JFrame frame = new JFrame("Account Balance");
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
            frame.setSize(300, 400);
            JLabel label = new JLabel();
            label.setText("Certificate of Deposit account balance: " + user.getCD());
            frame.add(label);
            frame.setVisible(true);
        }
    }

    class ButtonListener14 implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            User user = Main.getMainUser();
            JFrame frame = new JFrame("Account Balance");
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
            frame.setSize(300, 400);
            JLabel label = new JLabel();
            JPanel panel = new JPanel();
            label.setText("Checking account balance: " + user.getChecking());
            panel.add(label);
            JLabel label2 = new JLabel();
            JPanel panel2 = new JPanel();
            label2.setText("Savings account balance: " + user.getSavings());
            panel2.add(label2);
            JLabel label3 = new JLabel();
            JPanel panel3 = new JPanel();
            label3.setText("Money Market account balance: " + user.getMM());
            panel3.add(label3);
            JLabel label4 = new JLabel();
            JPanel panel4 = new JPanel();
            label4.setText("Certificate of Deposit account balance: " + user.getCD());
            panel4.add(label4);

            frame.add(panel);
            frame.add(panel2);
            frame.add(panel3);
            frame.add(panel4);
            frame.setVisible(true);
        }
    }

    class ButtonListener15 implements ActionListener {
        static JTextField amount;

        @Override
        public void actionPerformed(ActionEvent e) {
            User user = Main.getMainUser();
            JFrame frame = new JFrame("Account Deposit");
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
            frame.setSize(300, 400);
            JPanel panel = new JPanel();
            JLabel label = new JLabel();
            label.setText("How much would you like to deposit?");
            panel.setPreferredSize(new Dimension(100, 100));
            panel.add(label);
            amount = new JTextField(8);
            JPanel panel2 = new JPanel();
            panel2.setPreferredSize(new Dimension(100, 100));
            amount.setSize(50, 50);
            panel2.add(amount);
            JPanel panel3 = new JPanel();
            panel3.setPreferredSize(new Dimension(100, 100));
            JButton button = new JButton("Deposit");
            panel3.add(button);

            frame.add(panel);
            frame.add(panel2);
            frame.add(panel3);
            frame.setVisible(true);
        }
    }

    class ButtonListener16 implements ActionListener {
        static JTextField amount;

        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Account Deposit");
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
            frame.setSize(300, 400);
            JPanel panel = new JPanel();
            JLabel label = new JLabel();
            label.setText("How much would you like to deposit?");
            panel.setPreferredSize(new Dimension(100, 100));
            panel.add(label);
            amount = new JTextField(8);
            JPanel panel2 = new JPanel();
            panel2.setPreferredSize(new Dimension(100, 100));
            amount.setSize(50, 50);
            panel2.add(amount);
            JPanel panel3 = new JPanel();
            panel3.setPreferredSize(new Dimension(100, 100));
            JButton button = new JButton("Deposit");
            panel3.add(button);

            frame.add(panel);
            frame.add(panel2);
            frame.add(panel3);
            frame.setVisible(true);
        }
    }

    class ButtonListener17 implements ActionListener {
        static JTextField amount;

        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Account Deposit");
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
            frame.setSize(300, 400);
            JPanel panel = new JPanel();
            JLabel label = new JLabel();
            label.setText("How much would you like to deposit?");
            panel.setPreferredSize(new Dimension(100, 100));
            panel.add(label);
            amount = new JTextField(8);
            JPanel panel2 = new JPanel();
            panel2.setPreferredSize(new Dimension(100, 100));
            amount.setSize(50, 50);
            panel2.add(amount);
            JPanel panel3 = new JPanel();
            panel3.setPreferredSize(new Dimension(100, 100));
            JButton button = new JButton("Deposit");
            panel3.add(button);

            frame.add(panel);
            frame.add(panel2);
            frame.add(panel3);
            frame.setVisible(true);
        }
    }

    class ButtonListener18 implements ActionListener {
        static JTextField amount;

        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Account Deposit");
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
            frame.setSize(300, 400);
            JPanel panel = new JPanel();
            JLabel label = new JLabel();
            label.setText("How much would you like to deposit?");
            panel.setPreferredSize(new Dimension(100, 100));
            panel.add(label);
            amount = new JTextField(8);
            JPanel panel2 = new JPanel();
            panel2.setPreferredSize(new Dimension(100, 100));
            amount.setSize(50, 50);
            panel2.add(amount);
            JPanel panel3 = new JPanel();
            panel3.setPreferredSize(new Dimension(100, 100));
            JButton button = new JButton("Deposit");
            panel3.add(button);

            frame.add(panel);
            frame.add(panel2);
            frame.add(panel3);
            frame.setVisible(true);
        }
    }

    class ButtonListener19 implements ActionListener {
        static JTextField amount;

        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Account Withdraw");
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
            frame.setSize(300, 400);
            JPanel panel = new JPanel();
            JLabel label = new JLabel();
            label.setText("How much would you like to withdraw?");
            panel.setPreferredSize(new Dimension(100, 100));
            panel.add(label);
            amount = new JTextField(8);
            JPanel panel2 = new JPanel();
            panel2.setPreferredSize(new Dimension(100, 100));
            amount.setSize(50, 50);
            panel2.add(amount);
            JPanel panel3 = new JPanel();
            panel3.setPreferredSize(new Dimension(100, 100));
            JButton button = new JButton("Withdraw");
            panel3.add(button);

            frame.add(panel);
            frame.add(panel2);
            frame.add(panel3);
            frame.setVisible(true);
        }
    }

    class ButtonListener20 implements ActionListener {
        static JTextField amount;

        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Account Withdraw");
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
            frame.setSize(300, 400);
            JPanel panel = new JPanel();
            JLabel label = new JLabel();
            label.setText("How much would you like to withdraw?");
            panel.setPreferredSize(new Dimension(100, 100));
            panel.add(label);
            amount = new JTextField(8);
            JPanel panel2 = new JPanel();
            panel2.setPreferredSize(new Dimension(100, 100));
            amount.setSize(50, 50);
            panel2.add(amount);
            JPanel panel3 = new JPanel();
            panel3.setPreferredSize(new Dimension(100, 100));
            JButton button = new JButton("Withdraw");
            panel3.add(button);

            frame.add(panel);
            frame.add(panel2);
            frame.add(panel3);
            frame.setVisible(true);
        }
    }

    class ButtonListener21 implements ActionListener {
        static JTextField amount;

        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Account Withdraw");
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
            frame.setSize(300, 400);
            JPanel panel = new JPanel();
            JLabel label = new JLabel();
            label.setText("How much would you like to withdraw?");
            panel.setPreferredSize(new Dimension(100, 100));
            panel.add(label);
            amount = new JTextField(8);
            JPanel panel2 = new JPanel();
            panel2.setPreferredSize(new Dimension(100, 100));
            amount.setSize(50, 50);
            panel2.add(amount);
            JPanel panel3 = new JPanel();
            panel3.setPreferredSize(new Dimension(100, 100));
            JButton button = new JButton("Withdraw");
            panel3.add(button);

            frame.add(panel);
            frame.add(panel2);
            frame.add(panel3);
            frame.setVisible(true);
        }
    }

    class ButtonListener22 implements ActionListener {
        static JTextField amount;

        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Account Withdraw");
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
            frame.setSize(300, 400);
            JPanel panel = new JPanel();
            JLabel label = new JLabel();
            label.setText("How much would you like to withdraw?");
            panel.setPreferredSize(new Dimension(100, 100));
            panel.add(label);
            amount = new JTextField(8);
            JPanel panel2 = new JPanel();
            panel2.setPreferredSize(new Dimension(100, 100));
            amount.setSize(50, 50);
            panel2.add(amount);
            JPanel panel3 = new JPanel();
            panel3.setPreferredSize(new Dimension(100, 100));
            JButton button = new JButton("Withdraw");
            panel3.add(button);

            frame.add(panel);
            frame.add(panel2);
            frame.add(panel3);
            frame.setVisible(true);
        }
    }

    class ButtonListener23 implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            User user = Main.getMainUser();
            String filename;
            String fileUsername = null;
            String matchFile = null;
            boolean login;
            String loginUsername = ButtonListener.loginUserName.getText();
            File dir = new File("./users");
            File[] directoryListing = dir.listFiles();
            for (File aFile : directoryListing) {
                filename = aFile.getName();
                fileUsername = filename.substring(0, filename.length() - 4);
                if (fileUsername.equals(loginUsername)) {
                    matchFile = aFile.getName();
                }
            }
            if (matchFile == null) {
                System.out.println("Invalid Username.");
                login = false;
            } else {
                Scanner in2 = new Scanner(System.in);
                String pass = in2.nextLine();
                HashMap<String, User> users = Main.getUsers();
                System.out.println(users);
                for (String userName : users.keySet()) {
                    if (userName.equals(fileUsername)) {
                        user = users.get(userName);
                    }
                }
                System.out.println(user);
                MessageDigest alg = null;
                try {
                    alg = MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException ex) {
                    throw new RuntimeException(ex);
                }
                alg.reset();
                alg.update(pass.getBytes());
                byte[] digest = alg.digest();
                StringBuffer hashedpasswd = new StringBuffer();
                String hx;
                for (int i = 0; i < digest.length; i++) {
                    hx = Integer.toHexString(0xFF & digest[i]);
                    if (hx.length() == 1) {
                        hx = "0" + hx;
                    }
                    hashedpasswd.append(hx);
                }


                String password = null;
                try {
                    password = Files.readAllLines(Paths.get("./users/" + loginUsername + ".txt")).get(1);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                if (hashedpasswd.toString().equals(password)) {
                    login = true;
                } else {
                    login = false;
                }

                    if (login = true) {
                        new ButtonListener4();
                    } else {
                        new ButtonListener();
                    }
                }
            }
        }




