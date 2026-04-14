import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.io.*;

class Account {
    int id;
    String name;
    double balance;
    ArrayList<String> history = new ArrayList<>();

    Account(int id, String name, double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        history.add("Account created with ₹" + balance);
    }
}

public class BankingSystem {

    static ArrayList<Account> accounts = new ArrayList<>();
    static final String FILE = "bank.json";

    //refresh tables
    static void refresh(DefaultTableModel m) {
        m.setRowCount(0);
        for (Account a : accounts) {
            m.addRow(new Object[]{a.id, a.name, a.balance});
        }
    }

    //save json
    static void save() {
        try (PrintWriter pw = new PrintWriter(FILE)) {

            pw.println("[");

            for (int i = 0; i < accounts.size(); i++) {
                Account a = accounts.get(i);

                pw.print("  {\"id\":" + a.id +
                        ",\"name\":\"" + a.name +
                        "\",\"balance\":" + a.balance + "}");

                if (i < accounts.size() - 1) pw.println(",");
            }

            pw.println("\n]");

        } catch (Exception e) {
            System.out.println("Error saving JSON");
        }
    }

    //load json
    static void load() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE));
            String line;

            while ((line = br.readLine()) != null) {

                line = line.trim();

                if (line.startsWith("{")) {
                    // Extract values properly
                    String idStr = line.split("\"id\":")[1].split(",")[0];
                    String nameStr = line.split("\"name\":\"")[1].split("\"")[0];
                    String balStr = line.split("\"balance\":")[1].replaceAll("[^0-9.]", "");

                    int id = Integer.parseInt(idStr);
                    double bal = Double.parseDouble(balStr);

                    accounts.add(new Account(id, nameStr, bal));
                }
            }

            br.close();

        } catch (Exception e) {
            System.out.println("Error loading JSON: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        load();

        JFrame f = new JFrame("MiniBank System");
        f.setSize(800, 650);
        f.setLayout(new FlowLayout());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextField idF = new JTextField(5);
        JTextField nameF = new JTextField(10);
        JTextField amtF = new JTextField(7);
        JTextField toIdField = new JTextField(5);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Name", "Balance"}, 0);
        JTable table = new JTable(model);

        JButton create = new JButton("Create");
        JButton delete = new JButton("Delete");
        JButton deposit = new JButton("Deposit");
        JButton withdraw = new JButton("Withdraw");
        JButton transfer = new JButton("Transfer");
        JButton interest = new JButton("Add Interest");
        JButton show = new JButton("Show All");

        f.add(new JLabel("ID")); f.add(idF);
        f.add(new JLabel("Name")); f.add(nameF);
        f.add(new JLabel("Amount")); f.add(amtF);
        f.add(new JLabel("To ID")); f.add(toIdField);

        f.add(create); f.add(delete);
        f.add(deposit); f.add(withdraw);
        f.add(transfer); f.add(interest);
        f.add(show);

        f.add(new JScrollPane(table));

        //button functions

        create.addActionListener(e -> {
            int id = Integer.parseInt(idF.getText());
            String name = nameF.getText();
            double bal = Double.parseDouble(amtF.getText());

            Account a = new Account(id, name, bal);
            accounts.add(a);
            save();
            refresh(model);
        });

        delete.addActionListener(e -> {
            int id = Integer.parseInt(idF.getText());
            accounts.removeIf(a -> a.id == id);
            save();
            refresh(model);
        });

        deposit.addActionListener(e -> {
            int id = Integer.parseInt(idF.getText());
            double amt = Double.parseDouble(amtF.getText());

            for (Account a : accounts) {
                if (a.id == id) {
                    a.balance += amt;
                    a.history.add("Deposited ₹" + amt);
                }
            }
            save();
            refresh(model);
        });

        withdraw.addActionListener(e -> {
            int id = Integer.parseInt(idF.getText());
            double amt = Double.parseDouble(amtF.getText());

            for (Account a : accounts) {
                if (a.id == id && a.balance >= amt) {
                    a.balance -= amt;
                    a.history.add("Withdraw ₹" + amt);
                }
            }
            save();
            refresh(model);
        });

        transfer.addActionListener(e -> {
            try {
                int from = Integer.parseInt(idF.getText());
                int to = Integer.parseInt(toIdField.getText());
                double amt = Double.parseDouble(amtF.getText());

                Account A = null, B = null;

                for (Account a : accounts) {
                    if (a.id == from) A = a;
                    if (a.id == to) B = a;
                }

                if (A != null && B != null && A.balance >= amt) {
                    A.balance -= amt;
                    B.balance += amt;

                    A.history.add("Transferred ₹" + amt + " to " + to);
                    B.history.add("Received ₹" + amt + " from " + from);
                } else {
                    JOptionPane.showMessageDialog(f, "Invalid Transfer");
                }

                save();
                refresh(model);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(f, "Invalid Input");
            }
        });

        interest.addActionListener(e -> {
            for (Account a : accounts) {
                double interestAmt = a.balance * 0.05;
                a.balance += interestAmt;
                a.history.add("Interest added ₹" + interestAmt);
            }
            save();
            refresh(model);
        });

        show.addActionListener(e -> refresh(model));

        refresh(model);
        f.setVisible(true);
    }
}