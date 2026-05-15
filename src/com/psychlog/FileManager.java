package com.psychlog;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class FileManager {
    private EncryptionManager encryptionManager;
    private String dataFolderPath;

    public FileManager(String dataFolderPath, String password) {
        this.dataFolderPath = dataFolderPath;
        this.encryptionManager = new EncryptionManager(password);
        createFolderIfNotExists();
    }

    private void createFolderIfNotExists() {
        File folder = new File(dataFolderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public void saveUser(User user) throws IOException {
        String data = user.getUsername() + "," +
                user.getPasswordHash() + "," +
                user.getDataFolderPath();
        String encrypted = encryptionManager.encrypt(data);
        Path filePath = Paths.get(dataFolderPath, "user.dat");
        Files.writeString(filePath, encrypted);
    }

    public User loadUserFromFile(String username) throws IOException {
        Path filePath = Paths.get(dataFolderPath, "user.dat");
        if (!Files.exists(filePath)) return null;
        String encrypted = Files.readString(filePath);
        String decrypted = encryptionManager.decrypt(encrypted);
        String[] parts = decrypted.split(",");
        if (parts[0].equals(username)) {
            return new User(parts[0], "", parts[2]) {
                @Override
                public boolean verifyPassword(String password) {
                    return getPasswordHash().equals(parts[1]);
                }
                @Override
                public String getPasswordHash() { return parts[1]; }
            };
        }
        return null;
    }

    public void saveJournalEntry(JournalEntry entry) throws IOException {
        String data = entry.getId() + "|" +
                entry.getUsername() + "|" +
                entry.getContent() + "|" +
                entry.getTimestamp().toString() + "|" +
                entry.getMood();
        String encrypted = encryptionManager.encrypt(data);
        Path filePath = Paths.get(dataFolderPath,
                "entry_" + entry.getId() + ".dat");
        Files.writeString(filePath, encrypted);
    }

    public List<JournalEntry> loadAllEntries(String username)
            throws IOException {
        List<JournalEntry> entries = new ArrayList<>();
        File folder = new File(dataFolderPath);
        File[] files = folder.listFiles((dir, name) ->
                name.startsWith("entry_") && name.endsWith(".dat"));
        if (files == null) return entries;
        for (File file : files) {
            String encrypted = Files.readString(file.toPath());
            String decrypted = encryptionManager.decrypt(encrypted);
            String[] parts = decrypted.split("\\|");
            if (parts.length >= 5 && parts[1].equals(username)) {
                JournalEntry entry = new JournalEntry(
                        parts[0], parts[1], parts[2],
                        LocalDateTime.parse(parts[3]), parts[4]);
                entries.add(entry);
            }
        }
        return entries;
    }

    public static User loadUser(String username) {
        try {
            Path indexPath = Paths.get(
                    System.getProperty("user.home"), "PsychLog", "index.dat");
            if (!Files.exists(indexPath)) return null;

            List<String> lines = Files.readAllLines(indexPath);
            for (String line : lines) {
                String[] parts = line.split("\\|");
                if (parts.length >= 3 && parts[0].equals(username)) {
                    String folderPath = parts[1];
                    String passwordHash = parts[2];
                    return new User(username, "", folderPath) {
                        @Override
                        public boolean verifyPassword(String password) {
                            try {
                                MessageDigest md = MessageDigest
                                        .getInstance("SHA-256");
                                byte[] hash = md.digest(password.getBytes());
                                String hashed = Base64.getEncoder()
                                        .encodeToString(hash);
                                return hashed.equals(passwordHash);
                            } catch (Exception e) {
                                return false;
                            }
                        }
                        @Override
                        public String getPasswordHash() { return passwordHash; }
                    };
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveUserIndex(User user) {
        try {
            Path indexDir = Paths.get(
                    System.getProperty("user.home"), "PsychLog");
            Files.createDirectories(indexDir);
            Path indexPath = indexDir.resolve("index.dat");

            String entry = user.getUsername() + "|" +
                    user.getDataFolderPath() + "|" +
                    user.getPasswordHash();

            List<String> lines = new ArrayList<>();
            if (Files.exists(indexPath)) {
                lines = new ArrayList<>(Files.readAllLines(indexPath));
                lines.removeIf(l -> l.startsWith(user.getUsername() + "|"));
            }
            lines.add(entry);
            Files.write(indexPath, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDataFolderPath() { return dataFolderPath; }
}