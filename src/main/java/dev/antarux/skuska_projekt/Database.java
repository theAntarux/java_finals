package dev.antarux.skuska_projekt;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private final static String ip = "167.86.67.94";
    private final static String port = "3306";
    private final static String database = "java_skuska";
    private final static String username = "DB_ACCESS";

    // Returns password very safely, basically slovak safety level
    private String getPassword() {
        String password = "";

        try {
            FileReader file = new FileReader(".env");
            LineNumberReader reader = new LineNumberReader(file);
            String line;

            while ((line = reader.readLine()) != null) {
                password = line.trim();
            }

            reader.close();
            file.close();
        } catch (IOException err){
            System.err.println(err);
        }

        if (password.isEmpty()) {
            System.err.println("Password is empty");
        }

        return password;
    }

    /*
        Returns active database connection for CRUD operations
        App should ideally use restful API endpoints for crud operations tho
     */
    public Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://" + ip + ":" + port + "/" + database;
        return DriverManager.getConnection(url, username, this.getPassword());
    }
}
