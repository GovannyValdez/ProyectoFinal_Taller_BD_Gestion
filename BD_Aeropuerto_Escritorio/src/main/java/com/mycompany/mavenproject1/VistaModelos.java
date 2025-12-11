/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.mavenproject1;

import DB2_Conexion.Conexion_DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author govan
 */
public class VistaModelos extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(VistaModelos.class.getName());

    /**
     * Creates new form VistaModelos
     */
    public VistaModelos() {
        initComponents();
                jPanel2.setLayout(new java.awt.BorderLayout());

        configurarTabla();
        cargarVistaModelosGrandes();
        
        setLocationRelativeTo(null);
        jScrollPane1.setViewportView(jTable1);
    }
    
    private void configurarTabla() {
        DefaultTableModel modeloTabla = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        modeloTabla.addColumn("Modelo");
        modeloTabla.addColumn("Pasajeros");
        modeloTabla.addColumn("Peso (kg)");
        modeloTabla.addColumn("% Capacidad");
        modeloTabla.addColumn("Categoría");
        
        jTable1.setModel(modeloTabla);
    }
    
    private void cargarVistaModelosGrandes() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = Conexion_DB.getInstance().getConnection();
            
            if (conn == null) {
                mostrarErrorConexion();
                return;
            }
            
            // Consultar la vista que creamos
            String sql = "SELECT Modelo, Pasajeros, Peso_Kg, " +
                         "Porcentaje_MaxCapacidad, Categoria_Capacidad " +
                         "FROM Vista_Aviones_MegaCapacidad " +
                         "ORDER BY Pasajeros DESC";
            
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            DefaultTableModel modeloTabla = (DefaultTableModel) jTable1.getModel();
            modeloTabla.setRowCount(0); // Limpiar tabla
            
            int contador = 0;
            int sumaPasajeros = 0;
            int megaCount = 0;
            int muyAltaCount = 0;
            int altaCount = 0;
            
            while (rs.next()) {
                String modelo = rs.getString("Modelo");
                int pasajeros = rs.getInt("Pasajeros");
                double peso = rs.getDouble("Peso_Kg");
                double porcentaje = rs.getDouble("Porcentaje_MaxCapacidad");
                String categoria = rs.getString("Categoria_Capacidad");
                
                Object[] fila = {
                    modelo,
                    pasajeros,
                    String.format("%.0f", peso),
                    String.format("%.1f%%", porcentaje),
                    categoria
                };
                modeloTabla.addRow(fila);
                
                contador++;
                sumaPasajeros += pasajeros;
                
                // Contar por categoría
                if (categoria.contains("MEGA")) {
                    megaCount++;
                } else if (categoria.contains("MUY ALTA")) {
                    muyAltaCount++;
                } else if (categoria.contains("ALTA")) {
                    altaCount++;
                }
            }
            
            if (contador > 0) {
                double promedio = (double) sumaPasajeros / contador;
                jLabel3.setText(String.format("Aviones grandes: %d total | MEGA: %d | MUY ALTA: %d | ALTA: %d | Promedio: %.0f pasajeros", 
                                               contador, megaCount, muyAltaCount, altaCount, promedio));
            } else {
                jLabel3.setText("No hay aviones con capacidad mayor a 400 pasajeros");
            }
            
        } catch (SQLException e) {
            manejarErrorSQL(e);
        } finally {
            cerrarRecursos(rs, pstmt);
        }
    }
    
    private void mostrarErrorConexion() {
        javax.swing.JOptionPane.showMessageDialog(this,
            "❌ Error: No hay conexión a la base de datos",
            "Error de conexión",
            javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    
    private void manejarErrorSQL(SQLException e) {
        String mensajeError = e.getMessage().toLowerCase();
        if (mensajeError.contains("vista_aviones_megacapacidad") || 
            mensajeError.contains("table/view") || 
            mensajeError.contains("does not exist") ||
            mensajeError.contains("no existe")) {
            
            javax.swing.JOptionPane.showMessageDialog(this,
                "❌ La vista 'Vista_Aviones_MegaCapacidad' no existe en la base de datos.\n\n" +
                "Por favor, ejecuta este SQL primero en DB2:\n\n" +
                "CREATE VIEW Vista_Aviones_MegaCapacidad AS " +
                "SELECT ModelNumber AS Modelo, Capacidad AS Pasajeros, " +
                "Peso AS Peso_Kg, ROUND((Capacidad / 1000.0) * 100, 1) AS Porcentaje_MaxCapacidad, " +
                "CASE WHEN Capacidad >= 800 THEN 'MEGA (800-1000)' " +
                "WHEN Capacidad >= 600 THEN 'MUY ALTA (600-800)' " +
                "WHEN Capacidad >= 400 THEN 'ALTA (400-600)' " +
                "ELSE 'Normal (<400)' END AS Categoria_Capacidad " +
                "FROM ModelosAvion " +
                "WHERE Capacidad >= 400 " +
                "ORDER BY Capacidad DESC",
                "Vista no encontrada",
                javax.swing.JOptionPane.ERROR_MESSAGE);
        } else {
            javax.swing.JOptionPane.showMessageDialog(this,
                "❌ Error de base de datos: " + e.getMessage(),
                "Error SQL",
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        logger.severe("Error SQL al cargar vista: " + e.toString());
    }
    
    private void cerrarRecursos(ResultSet rs, PreparedStatement pstmt) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        } catch (SQLException e) {
            logger.warning("Error al cerrar recursos: " + e.toString());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        btnMostrar = new javax.swing.JButton();
        btnActualizar = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(51, 0, 153));
        jPanel1.setForeground(new java.awt.Color(0, 0, 153));

        jLabel1.setFont(new java.awt.Font("Yu Gothic UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Vista modelos");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(247, 247, 247))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(jLabel1)
                .addContainerGap(52, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        btnMostrar.setText("Mostrar");
        btnMostrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMostrarActionPerformed(evt);
            }
        });

        btnActualizar.setText("Actualizar");
        btnActualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarActionPerformed(evt);
            }
        });

        btnCerrar.setText("Cerrar");
        btnCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarActionPerformed(evt);
            }
        });

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

        jLabel3.setForeground(new java.awt.Color(0, 0, 0));
        jLabel3.setText("Modelos con capacidad  grande");

        jLabel2.setFont(new java.awt.Font("Yu Gothic UI", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 0, 0));
        jLabel2.setText("Categorizacion de los modelos de avión");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnMostrar)
                        .addGap(18, 18, 18)
                        .addComponent(btnActualizar)
                        .addGap(11, 11, 11)
                        .addComponent(btnCerrar))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 632, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 624, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 12, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnMostrar)
                            .addComponent(btnActualizar)
                            .addComponent(btnCerrar))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addContainerGap(9, Short.MAX_VALUE)
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)))
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
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
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnMostrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMostrarActionPerformed
        // TODO add your handling code here:
                cargarVistaModelosGrandes();

    }//GEN-LAST:event_btnMostrarActionPerformed

    private void btnActualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarActionPerformed
        // TODO add your handling code here:
         cargarVistaModelosGrandes();
        javax.swing.JOptionPane.showMessageDialog(this, 
            "Datos actualizados correctamente", 
            "Actualización", 
            javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_btnActualizarActionPerformed

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        // TODO add your handling code here:
        MenuModelos menu = new MenuModelos();   
       
    menu.setVisible(true);                

    this.dispose();
    }//GEN-LAST:event_btnCerrarActionPerformed

    /**
     * @param args the command line arguments
     */
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnMostrar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
