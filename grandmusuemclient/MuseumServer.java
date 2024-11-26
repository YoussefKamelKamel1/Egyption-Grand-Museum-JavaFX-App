package com.example.grandmusuemclient;
import org.mindrot.jbcrypt.BCrypt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MuseumServer {

    private static final int PORT = 12345;
    private static Connection dbConnection;
    private static Map<Integer, ClientHandler> onlineClients = new ConcurrentHashMap<>();
    private static Map<Integer, String> userStatuses = new ConcurrentHashMap<>();
    private static List<ServerUI> uis = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        try {
            dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/museum", "root", "123456");

            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Museum server is running on port " + PORT);

            // Set all users to offline when the server starts
            setAllUsersOffline();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException | SQLException e) {
            System.err.println("Server initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // revise
    public static void registerUI(ServerUI ui) {
        uis.add(ui);
        updateUI();
    }
    // revise
    public static void unregisterUI(ServerUI ui) {
        uis.remove(ui);
    }
    // revise
    private static void updateUI() {
        uis.forEach(ServerUI::refreshUI);
    }

    private static void setAllUsersOffline() throws SQLException {
        String sql = "UPDATE users SET status = 'Offline'";
        try (Statement stmt = dbConnection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private int userId;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String command;
                while ((command = in.readLine()) != null) {
                    handleCommand(command);
                }
            } catch (IOException | SQLException e) {
                System.err.println("Connection error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                             handleDisconnection();
            }
        }

        private void handleDisconnection() {
            try {
                if (userId != -1) {
                    onlineClients.remove(userId);
                    userStatuses.remove(userId);
                    updateUserStatus(userId, "Offline");
                    broadcastMessage("USER_OFFLINE " + userId);
                    updateUI();
                }
                socket.close();
            } catch (IOException | SQLException e) {
                System.err.println("Error closing resources for user " + userId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void handleCommand(String command) throws SQLException {
            System.out.println("Received command: " + command);
            String[] parts = command.split(" ", 2);
            String action = parts[0];
            String data = parts.length > 1 ? parts[1] : "";

            switch (action) {
                case "SIGNUP":
                    handleRegister(data);
                    break;
                case "LOGIN":
                    handleLogin(data);
                    break;
                case "CHAT":
                    handleChat(data);
                    break;
                case "STATUS":
                    handleStatus(data);
                    break;
                case "BOOK":
                    handleBooking(data);
                    break;
                case "LOGOUT":
                    handleLogout();
                    break;
                case "GET_ONLINE_USERS":
                    sendOnlineUsersToUI();
                    break;
                case "GET_OFFLINE_USERS":
                    sendOfflineUsersToUI();
                    break;
                case "UPDATE_PROFILE":
                    handleUpdateProfile(data);
                    break;
                case "DELETE_ACCOUNT":
                    handleDeleteAccount();
                    break;
                case "GET_USER_INFO":
                    handleGetUserInfo();
                    break;
                default:
                    out.println("UNKNOWN_COMMAND");
            }
        }

        private void handleRegister(String data) {
            System.out.println("Registering user with data: " + data);
            String[] details = data.split(",");
            if (details.length != 5) {
                out.println("REGISTER_FAILED: Invalid input");
                return;
            }

            String username = details[0];
            String email = details[1];
            String password = details[2];
            String gender = details[3];
            String nationality = details[4];


            try {
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                String sql = "INSERT INTO users (username, email, password_hash, gender,  nationality) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, email);
                    pstmt.setString(3, hashedPassword);
                    pstmt.setString(4,gender );
                    pstmt.setString(5, nationality);

                    pstmt.executeUpdate();
                    out.println("SUCCESS");
                    System.out.println("User registered successfully: " + email);
                }
            } catch (SQLException e) {
                System.err.println("Registration failed: " + e.getMessage());
                e.printStackTrace();
                out.println("REGISTER_FAILED: " + e.getMessage());
            }
        }

        private void handleLogin(String data) {
            System.out.println("Login attempt with data: " + data);
            String[] details = data.split(",");

            if (details.length != 2) {
                out.println("LOGIN_FAILED: Invalid input");
                return;
            }

            String email = details[0];
            String password = details[1];

            try {
                String sql = "SELECT user_id, password_hash FROM users WHERE email = ?";
                try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
                    pstmt.setString(1, email);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            int userId = rs.getInt("user_id");
                            String hashedPassword = rs.getString("password_hash");

                            if (BCrypt.checkpw(password, hashedPassword)) {
                                this.userId = userId;
                                onlineClients.put(userId, this);

                                updateUserStatus(userId, "Available");
                                out.println("SUCCESS");
                                broadcastMessage("USER_ONLINE " + userId);
                                updateUI();
                                System.out.println("User logged in: " + email);
                            } else {
                                out.println("LOGIN_FAILED: Invalid password");
                            }
                        } else {
                            out.println("LOGIN_FAILED: User not found");
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Login failed: " + e.getMessage());
                e.printStackTrace();
                out.println("LOGIN_FAILED: An error occurred.");
            }
        }

        private void handleChat(String message) throws SQLException {
            System.out.println("Chat message: " + message);

            if (message == null || message.trim().isEmpty()) {
                out.println("CHAT_FAILED: Empty message");
                return;
            }

            // Fetch user details from the database
            User user = getUserById(this.userId);
            if (user == null) {
                out.println("CHAT_FAILED: User not found");
                return;
            }

            // Format the message to include username, nationality, and status
            String formattedMessage = String.format("MESSAGE,%s,%s,%s,%s",
                    user.getUsername(),
                    user.getNationality(),
                    user.getStatus(),
                    message // Full message content
            );

            // Broadcast the formatted message to all online users
            broadcastMessage(formattedMessage);
        }




        private void handleUpdateProfile(String data) throws SQLException {
            String[] details = data.split(",");
            String newUsername = null, newNationality = null, oldPassword = null, newPassword = null;

            // Parse the update request to extract which fields are being updated
            for (String detail : details) {
                if (detail.startsWith("username=")) {
                    newUsername = detail.split("=")[1];
                } else if (detail.startsWith("nationality=")) {
                    newNationality = detail.split("=")[1];
                } else if (detail.startsWith("oldPassword=")) {
                    oldPassword = detail.split("=")[1];
                } else if (detail.startsWith("newPassword=")) {
                    newPassword = detail.split("=")[1];
                }
            }

            // Verify old password if the password is being updated
            if (oldPassword != null && newPassword != null) {
                String query = "SELECT password_hash FROM users WHERE user_id = ?";
                try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
                    stmt.setInt(1, userId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            String passwordHash = rs.getString("password_hash");
                            if (!BCrypt.checkpw(oldPassword, passwordHash)) {
                                out.println("INVALID_PASSWORD");
                                return;
                            }
                        }
                    }
                }
            }

            // Update the user information based on the fields that were provided
            StringBuilder updateQuery = new StringBuilder("UPDATE users SET ");
            List<Object> params = new ArrayList<>();

            if (newUsername != null) {
                updateQuery.append("username = ?, ");
                params.add(newUsername);
            }

            if (newNationality != null) {
                updateQuery.append("nationality = ?, ");
                params.add(newNationality);
            }

            if (newPassword != null) {
                updateQuery.append("password_hash = ?, ");
                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                params.add(hashedPassword);
            }

            // Remove last comma and append WHERE clause
            updateQuery.setLength(updateQuery.length() - 2); // Remove trailing comma
            updateQuery.append(" WHERE user_id = ?");

            // Execute the update query
            try (PreparedStatement pstmt = dbConnection.prepareStatement(updateQuery.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    pstmt.setObject(i + 1, params.get(i));
                }
                pstmt.setInt(params.size() + 1, userId); // Set userId at the end
                pstmt.executeUpdate();
                System.out.println("updated sussefully");
                out.println("UPDATE_SUCCESS");
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("UPDATE_FAILED");
            }
        }




        private void handleDeleteAccount() throws SQLException {
            String sql = "DELETE FROM users WHERE user_id = ?";
            try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.executeUpdate();
                out.println("DELETE_SUCCESS");
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("DELETE_FAILED");
            }
        }


        private void handleGetUserInfo() throws SQLException {
            String query = "SELECT username, email, nationality FROM users WHERE user_id = ?";
            try (PreparedStatement statement = dbConnection.prepareStatement(query)) {
                statement.setInt(1, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String username = resultSet.getString("username");
                        String email = resultSet.getString("email");
                        String nationality = resultSet.getString("nationality");
                        out.println(username + "," + email + "," + nationality); // Return user info
                    } else {
                        out.println("USER_NOT_FOUND");
                    }
                }
            }
        }





        private User getUserById(int userId) throws SQLException {
            System.out.println("start serching for name");
            if (dbConnection == null || dbConnection.isClosed()) {
                throw new SQLException("Database connection is not available.");
            }

            String query = "SELECT username, nationality, status FROM users WHERE user_id = ?";
            try (PreparedStatement statement = dbConnection.prepareStatement(query)) {
                statement.setInt(1, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String username = resultSet.getString("username");
                        String nationality = resultSet.getString("nationality");
                        String status = resultSet.getString("status");

                        return new User(userId, username, null, null, nationality, null, status);
                    } else {
                        System.out.println("User not found in sending message");
                        return null;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new SQLException("Error fetching user details: " + e.getMessage());
            }
        }



        private void handleStatus(String data) {
            System.out.println("Status update: " + data);
            try {
                userStatuses.put(userId, data);
                updateUserStatus(userId, data);
                out.println("STATUS_UPDATE_SUCCESS");
                broadcastMessage("STATUS_UPDATE " + userId + " " + data);
                updateUI();
                System.out.println("Status updated for user " + userId + " to " + data);
            } catch (SQLException e) {
                System.err.println("Status update failed: " + e.getMessage());
                e.printStackTrace();
                out.println("STATUS_UPDATE_FAILED: " + e.getMessage());
            }
        }



        // not used
        private void handleBooking(String data) {
            System.out.println("Booking request with data: " + data);
            out.println("BOOKING_SUCCESS");
        }

        private void handleLogout() {
            System.out.println("Logout for user: " + userId);
            try {
                onlineClients.remove(userId);
                userStatuses.remove(userId);
                updateUserStatus(userId, "Offline");
                broadcastMessage("USER_OFFLINE " + userId);
                updateUI();
                out.println("LOGOUT_SUCCESS");
            } catch (SQLException e) {
                System.err.println("Logout failed: " + e.getMessage());
                e.printStackTrace();
                out.println("LOGOUT_FAILED: " + e.getMessage());
            } finally {
//                handleDisconnection();
            }
        }

        private void updateUserStatus(int userId, String status) throws SQLException {
            String sql = "UPDATE users SET status = ? WHERE user_id = ?";
            try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
                pstmt.setString(1, status);
                pstmt.setInt(2, userId);
                pstmt.executeUpdate();
            }
        }

        private void broadcastMessage(String message) {
            System.out.println("broadcastMessage"+message);
            for (Map.Entry<Integer, ClientHandler> entry : onlineClients.entrySet()) {
                ClientHandler client = entry.getValue();
                // Send the message to all clients except the sender

                if (client != this) {
                    client.out.println(message);
                }
            }
        }



        private void sendOnlineUsersToUI() throws SQLException {
            System.out.println("Sending online users to UI");
            String sql = "SELECT username FROM users WHERE status = 'Available'";
            try (Statement stmt = dbConnection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                StringBuilder response = new StringBuilder("ONLINE_USERS_LIST:");
                while (rs.next()) {
                    response.append(rs.getString("username")).append(",");
                }
                if (response.length() > 0 && response.charAt(response.length() - 1) == ',') {
                    response.setLength(response.length() - 1);  // Remove trailing comma
                }
                out.println(response.toString());
            }
        }

        private void sendOfflineUsersToUI() throws SQLException {
            System.out.println("Sending offline users to UI");
            String sql = "SELECT username FROM users WHERE status = 'Offline'";
            try (Statement stmt = dbConnection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                StringBuilder response = new StringBuilder("OFFLINE_USERS_LIST:");
                while (rs.next()) {
                    response.append(rs.getString("username")).append(",");
                }
                if (response.length() > 0 && response.charAt(response.length() - 1) == ',') {
                    response.setLength(response.length() - 1);  // Remove trailing comma
                }
                out.println(response.toString());
            }
        }

    }
}