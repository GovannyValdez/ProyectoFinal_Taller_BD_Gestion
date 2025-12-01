/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.mavenproject1;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author govan
 */
public class ConsultarEmpleado extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ConsultarEmpleado.class.getName());

    /**
     * Creates new form ConsultarEmpleado
     */
    
    private static final String DB_URL = "jdbc:db2://localhost:25000/BD_AEROP";
    private static final String DB_USER = "db2admin";
    private static final String DB_PASSWORD = "Govanny27";
    private DefaultTableModel modelo;

    /**
     * Creates new form ConsultarEmpleado
     */
    public ConsultarEmpleado() {
        initComponents();
        setLocationRelativeTo(null);  // Centrar ventana
        inicializarTabla();
        cargarTodosEmpleados();  // Cargar datos al iniciar
    }
    
    /**
     * Inicializa la tabla con las columnas correctas
     */
    private void inicializarTabla() {
        // Definir nombres de columnas (igual que tu tabla EMPLEADO)
        String[] columnas = {
            "SSN", 
            "Nombre", 
            "Apellido Paterno", 
            "Apellido Materno",
            "Dirección",
            "Teléfono",
            "Salario",
            "Número Membresía"
        };
        
        // Crear modelo de tabla
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer la tabla de solo lectura
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Esto ayuda a que los datos se muestren correctamente
                return String.class;
            }
        };
        
        // Asignar modelo a la tabla
        jTable1.setModel(modelo);
        
        // Ajustar anchos de columnas para mejor visualización
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(100);  // SSN
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(150);  // Nombre
        jTable1.getColumnModel().getColumn(2).setPreferredWidth(120);  // Apellido Paterno
        jTable1.getColumnModel().getColumn(3).setPreferredWidth(120);  // Apellido Materno
        jTable1.getColumnModel().getColumn(4).setPreferredWidth(200);  // Dirección
        jTable1.getColumnModel().getColumn(5).setPreferredWidth(100);  // Teléfono
        jTable1.getColumnModel().getColumn(6).setPreferredWidth(100);  // Salario
        jTable1.getColumnModel().getColumn(7).setPreferredWidth(120);  // Membresía
        
        // Habilitar ordenamiento por columnas
        jTable1.setAutoCreateRowSorter(true);
    }
    
    /**
     * Carga TODOS los empleados de la base de datos
     */
    private void cargarTodosEmpleados() {
        cargarEmpleados("");  // Cadena vacía = mostrar todos
    }
    
    /**
     * Carga empleados, con opción de filtrar por SSN
     */
    private void cargarEmpleados(String filtroSSN) {
        // Limpiar tabla
        modelo.setRowCount(0);
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            // Cargar driver DB2
            Class.forName("com.ibm.db2.jcc.DB2Driver");
            
            // Establecer conexión
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            
            // Construir consulta SQL
            String sql = "SELECT ssn, nombre, apellido_paterno, apellido_materno, " +
                        "direccion, telefono, salario, numero_membresia_sindicato " +
                        "FROM EMPLEADO";
            
            // Si hay filtro, agregar WHERE
            if (filtroSSN != null && !filtroSSN.trim().isEmpty()) {
                sql += " WHERE ssn LIKE ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, "%" + filtroSSN.trim() + "%");
            } else {
                sql += " ORDER BY apellido_paterno, nombre";
                pstmt = conn.prepareStatement(sql);
            }
            
            // Ejecutar consulta
            rs = pstmt.executeQuery();
            
            // Contador de registros
            int contador = 0;
            
            // Llenar la tabla con los resultados
            while (rs.next()) {
                Object[] fila = {
                    rs.getString("ssn"),
                    rs.getString("nombre"),
                    rs.getString("apellido_paterno"),
                    rs.getString("apellido_materno") != null ? rs.getString("apellido_materno") : "", // Manejar nulos
                    rs.getString("direccion") != null ? rs.getString("direccion") : "",
                    rs.getString("telefono") != null ? rs.getString("telefono") : "",
                    String.format("$%,.2f", rs.getBigDecimal("salario")),  // Formatear salario como moneda
                    rs.getString("numero_membresia_sindicato")
                };
                
                modelo.addRow(fila);
                contador++;
            }
            
            // Mostrar mensaje si no hay resultados
            if (contador == 0) {
                if (filtroSSN != null && !filtroSSN.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "No se encontraron empleados con SSN: " + filtroSSN, 
                        "Sin resultados", 
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "No hay empleados registrados en el sistema.", 
                        "Sin datos", 
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
            
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Driver DB2 no encontrado", e);
            JOptionPane.showMessageDialog(this, 
                "Error: Driver DB2 no encontrado. Asegúrate de agregar db2jcc4.jar al proyecto.", 
                "Error del Sistema", 
                JOptionPane.ERROR_MESSAGE);
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al consultar empleados", e);
            
            // Verificar si la tabla existe
            if (e.getMessage().contains("does not exist") || e.getSQLState().equals("42704")) {
                JOptionPane.showMessageDialog(this, 
                    "Error: La tabla EMPLEADO no existe.\n" +
                    "Ejecuta primero el script de creación de tabla.", 
                    "Tabla no encontrada", 
                    JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Error de base de datos: " + e.getMessage(), 
                    "Error DB2", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } finally {
            // Cerrar recursos
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error al cerrar recursos", e);
            }
        }
    }
    
   /*(public ConsultarEmpleado() {
        initComponents();
    }*/

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        filtrarTxt = new javax.swing.JTextField();
        filtrarBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jTable1.setAutoCreateRowSorter(true);
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jButton1.setText("Regresar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(jButton1)
                .addContainerGap())
        );

        jPanel2.setBackground(new java.awt.Color(0, 0, 204));

        jLabel1.setFont(new java.awt.Font("Yu Gothic UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Consultar empleado");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Buscar por SSN");

        filtrarBtn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        filtrarBtn.setText("Buscar");
        filtrarBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filtrarBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addComponent(jLabel2)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(60, 60, 60)
                        .addComponent(jLabel1)
                        .addContainerGap(379, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(94, 94, 94)
                        .addComponent(filtrarTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(filtrarBtn)
                        .addGap(77, 77, 77))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(27, 27, 27)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(filtrarTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filtrarBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(74, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
          MenuEmpleados menu = new MenuEmpleados();   // crear ventana principal
       
    menu.setVisible(true);                // mostrar

    this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    
    
    
    private void filtrarBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filtrarBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_filtrarBtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new ConsultarEmpleado().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton filtrarBtn;
    private javax.swing.JTextField filtrarTxt;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
