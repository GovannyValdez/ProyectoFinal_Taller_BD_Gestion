/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.mavenproject1;

import DB2_Conexion.Conexion_DB;
import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class TransaccionEmpleado extends javax.swing.JFrame {
    
    private String empleadoSSN = "";
    private double salarioActual = 0;
    private String nombreEmpleado = "";

    public TransaccionEmpleado() {
        initComponents();
        setLocationRelativeTo(null);
        setTitle("Transacción - Aumento Salarial");
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        
        txtCantidadAumento.setEnabled(false);
        txtCantidadAumento.setText("");
        jTextArea1.setText(
            "TRANSACCIÓN DE AUMENTO SALARIAL\n" +
            "================================\n" +
            "1. Ingresa SSN del empleado\n" +
            "2. Presiona 'Buscar'\n" +
            "3. Ingresa porcentaje de aumento\n" +
            "4. Presiona 'Ejecutar'"
        );
    }
    
    private void buscarEmpleado() {
        String ssn = TxtSSN.getText().trim();
        
        if (ssn.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa el SSN del empleado", "Campo vacío", JOptionPane.WARNING_MESSAGE);
            TxtSSN.requestFocus();
            return;
        }
        
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = Conexion_DB.getInstance().getConnection();
            
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Error de conexión a la BD", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String sqlCheck = "SELECT COUNT(*) as total FROM EMPLEADO WHERE ssn = ?";
            java.sql.PreparedStatement psCheck = conn.prepareStatement(sqlCheck);
            psCheck.setString(1, ssn);
            ResultSet rsCheck = psCheck.executeQuery();
            
            if (rsCheck.next() && rsCheck.getInt("total") == 0) {
                JOptionPane.showMessageDialog(this, "Empleado no encontrado", "Error", JOptionPane.ERROR_MESSAGE);
                psCheck.close();
                return;
            }
            psCheck.close();
            
            String sql = "{call SP_BUSCAR_EMPLEADO(?)}";
            stmt = conn.prepareCall(sql);
            stmt.setString(1, ssn);
            
            rs = stmt.executeQuery();
            
            if (rs != null && rs.next()) {
                empleadoSSN = rs.getString("ssn");
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido_paterno");
                salarioActual = rs.getDouble("salario");
                
                nombreEmpleado = nombre + " " + apellido;
                
                jTextArea1.setText(
                    "✅ EMPLEADO ENCONTRADO\n" +
                    "=======================\n" +
                    "SSN: " + empleadoSSN + "\n" +
                    "Nombre: " + nombreEmpleado + "\n" +
                    "Salario actual: $" + String.format("%.2f", salarioActual) + "\n" +
                    "=======================\n" +
                    "Ingresa el porcentaje de aumento:"
                );
                
                txtCantidadAumento.setEnabled(true);
                txtCantidadAumento.requestFocus();
                txtCantidadAumento.setText("");
                
            } else {
                jTextArea1.setText("❌ ERROR: No se obtuvieron datos\nSSN: " + ssn);
                resetearDatos();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al buscar empleado:\n" + e.getMessage(), 
                "Error SQL", 
                JOptionPane.ERROR_MESSAGE);
            resetearDatos();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {}
        }
    }
    
    private void ejecutarTransaccion() {
        if (empleadoSSN.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Primero busca un empleado", 
                "Empleado no seleccionado", 
                JOptionPane.WARNING_MESSAGE);
            TxtSSN.requestFocus();
            return;
        }
        
        String porcentajeText = txtCantidadAumento.getText().trim();
        
        if (porcentajeText.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Ingresa el porcentaje de aumento", 
                "Campo vacío", 
                JOptionPane.WARNING_MESSAGE);
            txtCantidadAumento.requestFocus();
            return;
        }
        
        try {
            double porcentaje = Double.parseDouble(porcentajeText);
            
            if (porcentaje <= 0) {
                JOptionPane.showMessageDialog(this, 
                    "El porcentaje debe ser mayor a 0", 
                    "Valor inválido", 
                    JOptionPane.ERROR_MESSAGE);
                txtCantidadAumento.requestFocus();
                txtCantidadAumento.selectAll();
                return;
            }
            
            if (porcentaje > 1000) {
                JOptionPane.showMessageDialog(this, 
                    "Porcentaje muy alto. Máximo: 1000%", 
                    "Valor inválido", 
                    JOptionPane.ERROR_MESSAGE);
                txtCantidadAumento.requestFocus();
                txtCantidadAumento.selectAll();
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "¿APLICAR AUMENTO DEL " + String.format("%.2f", porcentaje) + "%?\n\n" +
                "Empleado: " + nombreEmpleado + "\n" +
                "SSN: " + empleadoSSN + "\n" +
                "Salario actual: $" + String.format("%.2f", salarioActual) + "\n\n" +
                "✅ TRANSACCIÓN ATOMICA: Todo o Nada",
                "CONFIRMAR TRANSACCIÓN",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            
            Connection conn = Conexion_DB.getInstance().getConnection();
            CallableStatement stmt = null;
            ResultSet rs = null;
            
            try {
                String sql = "{call SP_AUMENTO_SALARIO(?, ?)}";
                stmt = conn.prepareCall(sql);
                stmt.setString(1, empleadoSSN);
                stmt.setDouble(2, porcentaje);
                
                rs = stmt.executeQuery();
                
                if (rs != null && rs.next()) {
                    String nombre = rs.getString("NOMBRE");
                    double salarioAnterior = rs.getDouble("SALARIO_ANTERIOR");
                    double salarioNuevo = rs.getDouble("SALARIO_NUEVO");
                    double aumento = rs.getDouble("AUMENTO");
                    double porcentajeAplicado = rs.getDouble("PORCENTAJE");
                    
                    jTextArea1.setText(
                        "✅ TRANSACCIÓN EXITOSA\n" +
                        "===============================\n" +
                        "Empleado: " + nombre + "\n" +
                        "SSN: " + empleadoSSN + "\n" +
                        "-------------------------------\n" +
                        "Porcentaje: " + String.format("%.2f", porcentajeAplicado) + "%\n" +
                        "Salario anterior: $" + String.format("%.2f", salarioAnterior) + "\n" +
                        "Aumento: +$" + String.format("%.2f", aumento) + "\n" +
                        "Nuevo salario: $" + String.format("%.2f", salarioNuevo) + "\n" +
                        "===============================\n" +
                        "✔ Transacción completada\n" +
                        "✔ Cambios confirmados en BD"
                    );
                    
                    salarioActual = salarioNuevo;
                    
                    JOptionPane.showMessageDialog(this,
                        "✅ TRANSACCIÓN COMPLETADA\n\n" +
                        "Aumento aplicado exitosamente.\n" +
                        "Nuevo salario: $" + String.format("%.2f", salarioNuevo),
                        "ÉXITO",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    txtCantidadAumento.setText("");
                    txtCantidadAumento.requestFocus();
                    
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Transacción ejecutada pero no se obtuvieron resultados", 
                        "Aviso", 
                        JOptionPane.WARNING_MESSAGE);
                }
                
            } catch (SQLException e) {
                manejarErrorTransaccion(e);
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                } catch (SQLException e) {}
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Porcentaje inválido. Debe ser un número.", 
                "Error de formato", 
                JOptionPane.ERROR_MESSAGE);
            txtCantidadAumento.requestFocus();
            txtCantidadAumento.selectAll();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error inesperado: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void manejarErrorTransaccion(SQLException e) {
        String errorMsg = e.getMessage();
        String mensajeUsuario;
        
        if (errorMsg.contains("not found") || errorMsg.contains("no data")) {
            mensajeUsuario = "Error: Empleado no encontrado durante la transacción";
        } else if (errorMsg.contains("constraint") || errorMsg.contains("CHECK")) {
            mensajeUsuario = "Error: El salario resultante viola reglas de validación";
        } else if (errorMsg.contains("SQLCODE=-802")) {
            mensajeUsuario = "Error: Valor numérico fuera de rango (posible salario muy alto)";
        } else {
            mensajeUsuario = "Error en transacción: " + errorMsg;
        }
        
        JOptionPane.showMessageDialog(this,
            "❌ TRANSACCIÓN FALLIDA\n\n" +
            mensajeUsuario + "\n\n" +
            "La transacción fue revertida (ROLLBACK).\n" +
            "No se realizaron cambios en la base de datos.",
            "ERROR EN TRANSACCIÓN",
            JOptionPane.ERROR_MESSAGE);
        
        jTextArea1.setText(
            "❌ TRANSACCIÓN FALLIDA\n" +
            "================================\n" +
            "Error: " + mensajeUsuario + "\n" +
            "--------------------------------\n" +
            "La transacción fue revertida.\n" +
            "Ningún cambio se aplicó.\n" +
            "================================\n" +
            "Puedes intentar nuevamente."
        );
        
        resetearDatos();
    }
    
    private void resetearDatos() {
        empleadoSSN = "";
        salarioActual = 0;
        nombreEmpleado = "";
        txtCantidadAumento.setEnabled(false);
        txtCantidadAumento.setText("");
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
        btnCerrar = new javax.swing.JButton();
        btnEjecutar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        TxtSSN = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtCantidadAumento = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        btnBuscar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(51, 0, 153));

        jLabel1.setFont(new java.awt.Font("Yu Gothic UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Procedimientos Empleado");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(135, 135, 135)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        btnCerrar.setText("Cerrar");
        btnCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarActionPerformed(evt);
            }
        });

        btnEjecutar.setText("Ejecutar");
        btnEjecutar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEjecutarActionPerformed(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        TxtSSN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TxtSSNActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Yu Gothic UI", 1, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 0, 0));
        jLabel2.setText("Transaccion Aumento de salario");

        jLabel5.setFont(new java.awt.Font("Yu Gothic UI", 1, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 0, 0));
        jLabel5.setText("Ingresar SSN de empleado");

        txtCantidadAumento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCantidadAumentoActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Yu Gothic UI", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(0, 0, 0));
        jLabel3.setText("Porcentaje de aumento");

        btnBuscar.setText("Buscar");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnCerrar))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(TxtSSN, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel5))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtCantidadAumento, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(btnBuscar)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnEjecutar, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(37, 37, 37))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TxtSSN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCantidadAumento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEjecutar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnBuscar)
                .addGap(21, 21, 21)
                .addComponent(btnCerrar)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(7, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnEjecutarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEjecutarActionPerformed
        // TODO add your handling code here:
        ejecutarTransaccion();
    }//GEN-LAST:event_btnEjecutarActionPerformed

    private void txtCantidadAumentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCantidadAumentoActionPerformed
        // TODO add your handling code here:
        ejecutarTransaccion();
    }//GEN-LAST:event_txtCantidadAumentoActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        // TODO add your handling code here:
        buscarEmpleado();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        // TODO add your handling code here:
        MenuEmpleados menu = new MenuEmpleados();   
       
    menu.setVisible(true);               

    this.dispose();
    }//GEN-LAST:event_btnCerrarActionPerformed

    private void TxtSSNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TxtSSNActionPerformed
        // TODO add your handling code here:
         buscarEmpleado();
    }//GEN-LAST:event_TxtSSNActionPerformed

    /**
     * @param args the command line arguments
     */
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField TxtSSN;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnEjecutar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField txtCantidadAumento;
    // End of variables declaration//GEN-END:variables
}
