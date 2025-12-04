/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DB2_Conexion;

import java.sql.Connection;

public class PruebaConexion {
    public static void main(String[] args) {
        System.out.println("Probando conexión...");
        
        // Usa TU conexión original
        Connection conn = Conexion_DB.getInstance().getConnection();
        
        if (conn != null) {
            System.out.println("✅ CONEXIÓN EXITOSA!");
            System.out.println("Ya puedes usar: conn.createStatement()");
        } else {
            System.out.println("❌ CONEXIÓN FALLIDA");
        }
    }
}
