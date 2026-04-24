package com.psychlog;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class PasswordVault {
    private Map<String, String> passwords;
    private EncryptionManager encryptionManager;
    private String vaultFilePath;

    public PasswordVault(String dataFolderPath, String masterPassword) {
        this.encryptionManager = new EncryptionManager(masterPassword);
        this.vaultFilePath = dataFolderPath + File.separator + "vault.dat";
        this.passwords = new HashMap<>();
        loadVault();
    }

    public void savePassword(String siteName, String password) {
        passwords.put(siteName, password);
        saveVault();
    }

    public String getPassword(String siteName) {
        return passwords.getOrDefault(siteName, null);
    }

    public void deletePassword(String siteName) {
        passwords.remove(siteName);
        saveVault();
    }

    public Map<String, String> getAllPasswords() {
        return new HashMap<>(passwords);
    }

    private void saveVault() {
        try {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : passwords.entrySet()) {
                sb.append(entry.getKey()).append(":::")
                        .append(entry.getValue()).append("\n");
            }
            String encrypted = encryptionManager.encrypt(sb.toString());
            Files.writeString(Paths.get(vaultFilePath), encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadVault() {
        try {
            Path path = Paths.get(vaultFilePath);
            if (!Files.exists(path)) return;
            String encrypted = Files.readString(path);
            String decrypted = encryptionManager.decrypt(encrypted);
            String[] lines = decrypted.split("\n");
            for (String line : lines) {
                if (line.contains(":::")) {
                    String[] parts = line.split(":::");
                    if (parts.length == 2) {
                        passwords.put(parts[0], parts[1]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}