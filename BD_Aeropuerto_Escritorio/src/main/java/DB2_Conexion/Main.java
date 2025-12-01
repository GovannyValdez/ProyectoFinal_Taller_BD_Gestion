/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DB2_Conexion;

public class Main {

    public static void main(String[] args) {
        Conexion_DB db = new Conexion_DB();

        // Inserción de prueba
        db.insertarPrueba(3, "Prueba 2");

        // Puedes intentar otro insert para probar:
        db.insertarPrueba(4, "dato de prueba");
        
        
        
    }
}

