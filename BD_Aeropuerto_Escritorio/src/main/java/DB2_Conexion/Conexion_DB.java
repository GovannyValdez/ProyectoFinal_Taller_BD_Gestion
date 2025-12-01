/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DB2_Conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class Conexion_DB {

    private static final String URL = "jdbc:db2://localhost:25000/BD_AEROP";
    private static final String USER = "db2admin";
    private static final String PASS = "Govanny27";

    public Connection conectar() {
        Connection con = null;

        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
            System.out.println("✔ Driver DB2 cargado correctamente");

            con = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("✔ Conexión exitosa");
        } catch (Exception e) {
            System.out.println("❌ Error conectando a DB2");
            System.out.println("Mensaje: " + e.getMessage());
        }

        return con;
    }

   
    
    
    public void insertarPrueba(int id, String nombre) {
        String sql = "INSERT INTO PRUEBA (ID, NOMBRE) VALUES (?, ?)";

        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setString(2, nombre);

            int filas = ps.executeUpdate();

            if (filas > 0) {
                System.out.println("✔ Registro insertado correctamente");
            }

            
            
            
        } catch (Exception e) {
            System.out.println("❌ Error al insertar");
            System.out.println("Mensaje: " + e.getMessage());
        }
    }
}
