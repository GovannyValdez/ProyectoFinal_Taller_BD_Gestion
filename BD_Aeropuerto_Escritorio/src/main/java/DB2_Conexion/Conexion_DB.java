/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DB2_Conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion_DB {
    
    private static Conexion_DB instance;
    private Connection connection;
    
    private static final String URL = "jdbc:db2://localhost:25000/BD_AEROP";
    private static final String USER = "db2admin";
    private static final String PASS = "Govanny27";
    
    // Constructor PRIVADO para evitar instanciación externa
    private Conexion_DB() {
        try {
            // Cargar driver solo una vez
            Class.forName("com.ibm.db2.jcc.DB2Driver");
            System.out.println("✔ Driver DB2 cargado correctamente");
            
            // Crear la conexión inicial
            connection = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("✔ Conexión a DB2 establecida exitosamente");
            
            // Configurar la conexión (opcional)
            connection.setAutoCommit(true);
            
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Error: Driver DB2 no encontrado");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ Error conectando a DB2");
            System.out.println("Mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Método público para obtener la instancia única
    public static Conexion_DB getInstance() {
        if (instance == null) {
            instance = new Conexion_DB();
        }
        return instance;
    }
    
    // Método para obtener la conexión
    public Connection getConnection() {
        return this.connection;
    }
    
    // Verificar si la conexión está activa
    public boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    // Método para reconectar si es necesario
    public void reconnectIfNeeded() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("⚠ Reconectando a DB2...");
                connection = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("✔ Reconexión exitosa");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error en reconexión: " + e.getMessage());
        }
    }
    
    // Método para cerrar la conexión (opcional, para cuando termines la aplicación)
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✔ Conexión cerrada correctamente");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error cerrando conexión: " + e.getMessage());
        }
    }
}
   
    
    
    