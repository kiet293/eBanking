package banking;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientCallback extends Remote {
    void notifyMessage(String msg) throws RemoteException;
    void notifyHistory(String msg) throws RemoteException;

}
