package edu.univ.erp.service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class BackupService {

    private static final String DB_USER = System.getenv().getOrDefault("SIS_DB_USER", "root");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("SIS_DB_PASSWORD", "yeahboi123");
    private static final String DB_HOST = System.getenv().getOrDefault("SIS_DB_HOST", "127.0.0.1");
    private static final String DB_PORT = System.getenv().getOrDefault("SIS_DB_PORT", "3306");

    public void backupDatabase(String dbName, File outputFile) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "mysqldump",
                "-h", DB_HOST,
                "-P", DB_PORT,
                "-u", DB_USER,
                "-p" + DB_PASSWORD,
                "--databases", dbName,
                "-r", outputFile.getAbsolutePath());

        pb.redirectErrorStream(true);
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException("Backup failed with exit code: " + exitCode);
        }
    }

    public void restoreDatabase(String dbName, File inputFile) throws IOException, InterruptedException {
        // Note: For restore, we pipe the file content to mysql command
        // mysql -h host -u user -p dbname < file.sql

        ProcessBuilder pb = new ProcessBuilder(
                "mysql",
                "-h", DB_HOST,
                "-P", DB_PORT,
                "-u", DB_USER,
                "-p" + DB_PASSWORD,
                dbName);

        pb.redirectInput(inputFile);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException("Restore failed with exit code: " + exitCode);
        }
    }
}
