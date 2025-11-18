package banking;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BankInterface extends Remote {
    boolean deposit(String acc, double amount) throws RemoteException;
    boolean withdraw(String acc, double amount) throws RemoteException;
    boolean transfer(String fromAcc, String toAcc, double amount, String content)
            throws RemoteException;
    double getBalance(String acc) throws RemoteException;
    String getHistory(String acc) throws RemoteException;
    void registerCallback(String acc, ClientCallback callback) throws RemoteException;
}
