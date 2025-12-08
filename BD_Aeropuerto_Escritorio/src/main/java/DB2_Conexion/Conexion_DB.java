/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DB2_Conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class Conexion_DB {
    
    
   
    private static Conexion_DB instance;
    private Connection connection;
    
    private static final String HOST = "localhost";
    private static final String PORT = "25000";
    private static final String DATABASE = "BD_AEROP";
    private static final String URL = "jdbc:db2://" + HOST + ":" + PORT + "/" + DATABASE;
    private static final String USER = "db2admin";
    private static final String PASS = "Govanny27";
    private static final String DRIVER = "com.ibm.db2.jcc.DB2Driver";
    
    private Conexion_DB() {
        conectar();
    }
    
    
    
    private void conectar() {
        try {
            Class.forName(DRIVER);
            System.out.println("✔ Driver DB2 cargado correctamente");
            
            DriverManager.setLoginTimeout(10); // 10 segundos timeout
            connection = DriverManager.getConnection(URL, USER, PASS);
            
            connection.setAutoCommit(true);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            
            System.out.println("✅ Conexión establecida a: " + DATABASE);
            System.out.println("   Host: " + HOST + ":" + PORT);
            System.out.println("   Usuario: " + USER);
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ ERROR: Driver DB2 no encontrado");
            System.err.println("   Asegúrate de tener el archivo db2jcc.jar en el classpath");
            mostrarError("Error de Driver", "No se encontró el driver de DB2. Verifique la configuración.");
            e.printStackTrace();
            
        } catch (SQLException e) {
            System.err.println("❌ ERROR SQL al conectar a DB2");
            System.err.println("   Código: " + e.getErrorCode());
            System.err.println("   Estado: " + e.getSQLState());
            System.err.println("   Mensaje: " + e.getMessage());
            mostrarError("Error de Conexión", 
                "No se pudo conectar a la base de datos.\n" +
                "Verifique:\n" +
                "1. Servidor DB2 está activo\n" +
                "2. Puerto 25000 está accesible\n" +
                "3. Credenciales correctas\n" +
                "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static synchronized Conexion_DB getInstance() {
        if (instance == null) {
            instance = new Conexion_DB();
        } else if (!instance.isConnectionValid()) {
            instance.reconectar();
        }
        return instance;
    }
    
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                reconectar();
            }
        } catch (SQLException e) {
            System.err.println("Error verificando conexión: " + e.getMessage());
            reconectar();
        }
        return this.connection;
    }
    
    public boolean isConnectionValid() {
        try {
            return connection != null 
                && !connection.isClosed() 
                && connection.isValid(5); 
        } catch (SQLException e) {
            return false;
        }
    }
    
    // Reconectar
    public void reconectar() {
        System.out.println("🔄 Intentando reconectar...");
        closeConnection(); 
        conectar();
    }
    
    // Cerrar conexión
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔒 Conexión cerrada correctamente");
            }
        } catch (SQLException e) {
            System.err.println("Error cerrando conexión: " + e.getMessage());
        }
    }
    
    private void mostrarError(String titulo, String mensaje) {
        try {
            JOptionPane.showMessageDialog(null, mensaje, titulo, JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            System.err.println(titulo + ": " + mensaje);
        }
    }
    
    public String getDatabaseInfo() {
        return "DB2 - " + DATABASE + " en " + HOST + ":" + PORT;
    }
    
    public static boolean testConnection() {
        try {
            Conexion_DB conexion = getInstance();
            return conexion.isConnectionValid();
        } catch (Exception e) {
            return false;
        }
    }
}