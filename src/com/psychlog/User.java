package com.psychlog;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class User {
    private String username;
    private String passwordHash;
    private String dataFolderPath;
    private boolean isGuest;

    public User(String username, String password, String dataFolderPath) {
        this.username = username;
        this.passwordHash = password != null ? hashPassword(password) : "";
        this.dataFolderPath = dataFolderPath;
        this.isGuest = false;
    }

    public User(String username, String password, String dataFolderPath, boolean isGuest) {
        this.username = username;
        this.passwordHash = password != null ? hashPassword(password) : "";
        this.dataFolderPath = dataFolderPath;
        this.isGuest = isGuest;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    public boolean verifyPassword(String password) {
        return this.passwordHash.equals(hashPassword(password));
    }

    public boolean isGuest() { return isGuest; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getDataFolderPath() { return dataFolderPath; }
    public void setDataFolderPath(String path) { this.dataFolderPath = path; }
}