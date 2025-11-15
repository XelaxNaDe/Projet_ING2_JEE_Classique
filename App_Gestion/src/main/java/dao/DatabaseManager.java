package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    // Assurez-vous que le nom de la BDD ('votre_nom_de_bdd') est correct
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/gestionRH";
    
    // Mettez le bon nom d'utilisateur (souvent "root" en local)
    private static final String JDBC_USER = "root"; 
    
    // Mettez une chaîne vide s'il n'y a pas de mot de passe
    private static final String JDBC_PASSWORD = ""; 

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
            } catch (ClassNotFoundException e) {
                // Il est préférable de "lancer" l'erreur pour que la servlet le sache
                throw new SQLException("Driver MySQL non trouvé", e);
            }
        }
        return connection;
    }
}