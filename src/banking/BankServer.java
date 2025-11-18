package banking;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class BankServer {
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            BankInterface bank = new BankServerImpl();
            Naming.rebind("rmi://localhost/BankService", bank);
            System.out.println("Server đang chạy...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
