package banking;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class BankServerImpl extends UnicastRemoteObject implements BankInterface {

    Map<String, Double> accounts = new HashMap<>();
    Map<String, ClientCallback> callbacks = new HashMap<>();
    Map<String, StringBuilder> history = new HashMap<>();

    public BankServerImpl() throws RemoteException {
        accounts.put("1001", 1000.0);
        accounts.put("1002", 500.0);
        accounts.put("1003", 2000.0);
    }

    private void addHistory(String acc, String log) {
        history.putIfAbsent(acc, new StringBuilder());
        history.get(acc).append(log).append("\n");
    }

    @Override
    public boolean deposit(String acc, double amount) throws RemoteException {
        if (amount <= 0) {
            throw new RemoteException("Số tiền phải lớn hơn 0!");
        }

        accounts.putIfAbsent(acc, 0.0);
        accounts.put(acc, accounts.get(acc) + amount);
        addHistory(acc, "Nạp tiền: +" + String.format("%,.0f", amount) + " VND | Thời gian: " + java.time.LocalDateTime.now());

        // Callback người nạp
        if (callbacks.containsKey(acc)) {
            callbacks.get(acc).notifyMessage(
                    "Nạp tiền thành công: +" + String.format("%,.0f", amount) + " VND | Số dư: " + String.format("%,.0f", accounts.get(acc)) + " VND"
            );
        }

        return true;
    }

    @Override
    public boolean withdraw(String acc, double amount) throws RemoteException {
        if (amount <= 0) {
            throw new RemoteException("Số tiền phải lớn hơn 0!");
        }

        accounts.putIfAbsent(acc, 0.0);
        double currentBalance = accounts.get(acc);

        // Kiểm tra số dư TRƯỚC KHI thực hiện giao dịch
        if (currentBalance < amount) {
            throw new RemoteException(
                    "Số dư không đủ! Số dư hiện tại: " + String.format("%,.0f", currentBalance) +
                            " VND, số tiền muốn rút: " + String.format("%,.0f", amount) + " VND"
            );
        }

        accounts.put(acc, currentBalance - amount);
        addHistory(acc, "Rút tiền: -" + String.format("%,.0f", amount) + " VND | Thời gian: " + java.time.LocalDateTime.now());

        // Callback người rút
        if (callbacks.containsKey(acc)) {
            callbacks.get(acc).notifyMessage(
                    "Rút tiền thành công: -" + String.format("%,.0f", amount) + " VND | Số dư: " + String.format("%,.0f", accounts.get(acc)) + " VND"
            );
        }

        return true;
    }

    @Override
    public boolean transfer(String fromAcc, String toAcc, double amount, String content) throws RemoteException {
        if (amount <= 0) {
            throw new RemoteException("Số tiền phải lớn hơn 0!");
        }

        if (fromAcc.equals(toAcc)) {
            throw new RemoteException("Không thể chuyển tiền cho chính mình!");
        }

        accounts.putIfAbsent(fromAcc, 0.0);
        accounts.putIfAbsent(toAcc, 0.0);

        double fromBalance = accounts.get(fromAcc);

        // Kiểm tra số dư TRƯỚC KHI thực hiện giao dịch
        if (fromBalance < amount) {
            throw new RemoteException(
                    "Số dư không đủ! Số dư hiện tại: " + String.format("%,.0f", fromBalance) +
                            " VND, số tiền muốn chuyển: " + String.format("%,.0f", amount) + " VND"
            );
        }

        // Thực hiện giao dịch
        accounts.put(fromAcc, fromBalance - amount);
        accounts.put(toAcc, accounts.get(toAcc) + amount);

        // Lưu lịch sử
        addHistory(fromAcc,
                "Chuyển tiền: -" + String.format("%,.0f", amount) + " VND tới " + toAcc +
                        " | Nội dung: " + (content.isEmpty() ? "(Không có)" : content) +
                        " | Thời gian: " + java.time.LocalDateTime.now());

        addHistory(toAcc,
                "Nhận tiền: +" + String.format("%,.0f", amount) + " VND từ " + fromAcc +
                        " | Nội dung: " + (content.isEmpty() ? "(Không có)" : content) +
                        " | Thời gian: " + java.time.LocalDateTime.now());

        // Callback người nhận
        if (callbacks.containsKey(toAcc)) {
            callbacks.get(toAcc).notifyMessage(
                    "Nhận " + String.format("%,.0f", amount) + " VND từ " + fromAcc +
                            " | ND: " + (content.isEmpty() ? "(Không có)" : content) +
                            " | Số dư: " + String.format("%,.0f", accounts.get(toAcc)) + " VND"
            );
        }

        // Callback người gửi
        if (callbacks.containsKey(fromAcc)) {
            callbacks.get(fromAcc).notifyMessage(
                    "Chuyển " + String.format("%,.0f", amount) + " VND tới " + toAcc +
                            " | ND: " + (content.isEmpty() ? "(Không có)" : content) +
                            " | Số dư: " + String.format("%,.0f", accounts.get(fromAcc)) + " VND"
            );
        }

        return true;
    }

    @Override
    public double getBalance(String acc) throws RemoteException {
        if (!accounts.containsKey(acc)) {
            accounts.put(acc, 0.0); // tạo tài khoản mới
        }
        return accounts.get(acc);
    }

    @Override
    public String getHistory(String acc) throws RemoteException {
        history.putIfAbsent(acc, new StringBuilder("Chưa có giao dịch nào"));
        String historyStr = history.get(acc).toString();
        return historyStr.isEmpty() ? "Chưa có giao dịch nào" : historyStr;
    }

    @Override
    public void registerCallback(String acc, ClientCallback callback) throws RemoteException {
        callbacks.put(acc, callback);
        System.out.println("Đã đăng ký callback cho tài khoản: " + acc);
    }
}