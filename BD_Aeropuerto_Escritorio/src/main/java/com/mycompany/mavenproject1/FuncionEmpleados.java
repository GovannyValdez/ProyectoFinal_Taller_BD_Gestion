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

/**
 *
 * @author govan
 */
public class FuncionEmpleados extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FuncionEmpleados.class.getName());

    /**
     * Creates new form FuncionEmpleados
     */
    public FuncionEmpleados() {
        initComponents();
        setLocationRelativeTo(null);
        setTitle("Función - Cálculo Salarial");
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    }
    
    private void calcularSalarioTotal() {
    String salarioBaseText = txtSalarioBase.getText().trim();
    String porcentajeText = txtPorcentajeBonificacion.getText().trim();
    
    if (salarioBaseText.isEmpty() || porcentajeText.isEmpty()) {
        JOptionPane.showMessageDialog(this, 
            "Todos los campos son requeridos", 
            "Campos incompletos", 
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    try {
        salarioBaseText = salarioBaseText.replace(',', '.');
        porcentajeText = porcentajeText.replace(',', '.');
        
        java.math.BigDecimal salarioBase = new java.math.BigDecimal(salarioBaseText);
        java.math.BigDecimal porcentaje = new java.math.BigDecimal(porcentajeText);
        
        if (salarioBase.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, 
                "El salario base debe ser mayor a 0", 
                "Valor inválido", 
                JOptionPane.ERROR_MESSAGE);
            txtSalarioBase.requestFocus();
            txtSalarioBase.selectAll();
            return;
        }
        
        if (porcentaje.compareTo(new java.math.BigDecimal("0")) < 0 || 
            porcentaje.compareTo(new java.math.BigDecimal("1000")) > 0) {
            JOptionPane.showMessageDialog(this, 
                "El porcentaje debe estar entre 0 y 1000", 
                "Valor inválido", 
                JOptionPane.ERROR_MESSAGE);
            txtPorcentajeBonificacion.requestFocus();
            txtPorcentajeBonificacion.selectAll();
            return;
        }
        
        Connection conn = Conexion_DB.getInstance().getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, 
                "Error de conexión a la base de datos", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String sql = "SELECT FN_CALCULAR_SALARIO_TOTAL(?, ?) FROM SYSIBM.SYSDUMMY1";
        java.sql.CallableStatement stmt = conn.prepareCall(sql);
        
        stmt.setBigDecimal(1, salarioBase.setScale(2, java.math.RoundingMode.HALF_UP));
        stmt.setBigDecimal(2, porcentaje.setScale(2, java.math.RoundingMode.HALF_UP));
        
        java.sql.ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            java.math.BigDecimal salarioTotal = rs.getBigDecimal(1);
            
            txtMostrarResultado.setText("$" + String.format("%,.2f", salarioTotal));
            
            JOptionPane.showMessageDialog(this,
                "✅ CÁLCULO COMPLETADO\n\n" +
                "Salario base: $" + String.format("%,.2f", salarioBase) + "\n" +
                "Porcentaje bonificación: " + String.format("%.2f", porcentaje) + "%\n" +
                "Salario total: $" + String.format("%,.2f", salarioTotal),
                "Resultado de la Función",
                JOptionPane.INFORMATION_MESSAGE);
                
        } else {
            JOptionPane.showMessageDialog(this, 
                "No se obtuvo resultado de la función", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
        
        rs.close();
        stmt.close();
        
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, 
            "Por favor ingresa valores numéricos válidos\n" +
            "Ejemplos:\n" +
            "• Salario base: 5000.00 o 5000,00\n" +
            "• Porcentaje: 15.5 o 15,5", 
            "Error de formato", 
            JOptionPane.ERROR_MESSAGE);
        txtSalarioBase.requestFocus();
        txtSalarioBase.selectAll();
        
    } catch (SQLException e) {
        // Mensaje de error MÁS DETALLADO
        String errorMsg = "Error SQL: " + e.getMessage() + 
                         "\nSQL State: " + e.getSQLState() + 
                         "\nError Code: " + e.getErrorCode();
        
        JOptionPane.showMessageDialog(this, 
            errorMsg, 
            "Error en Base de Datos", 
            JOptionPane.ERROR_MESSAGE);
        
        e.printStackTrace();
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, 
            "Error inesperado: " + e.getMessage(), 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}
    
    private void limpiarCampos() {
        txtSalarioBase.setText("");
        txtPorcentajeBonificacion.setText("");
        txtMostrarResultado.setText("");
        txtSalarioBase.requestFocus();
    }
    
    private void cerrarVentana() {
        dispose();
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
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtSalarioBase = new javax.swing.JTextField();
        txtPorcentajeBonificacion = new javax.swing.JTextField();
        btnCalcular = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        txtMostrarResultado = new javax.swing.JTextField();
        btnCerrar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(0, 0, 153));

        jLabel1.setFont(new java.awt.Font("Yu Gothic UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Funcion empleado");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(104, 104, 104)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jLabel1)
                .addContainerGap(43, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel2.setFont(new java.awt.Font("Yu Gothic UI", 1, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 0, 0));
        jLabel2.setText("Salario base");

        jLabel3.setFont(new java.awt.Font("Yu Gothic UI", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(0, 0, 0));
        jLabel3.setText("Porcentaje de bonificacion");

        txtSalarioBase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSalarioBaseActionPerformed(evt);
            }
        });

        txtPorcentajeBonificacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPorcentajeBonificacionActionPerformed(evt);
            }
        });

        btnCalcular.setText("Calcular");
        btnCalcular.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCalcularActionPerformed(evt);
            }
        });

        btnLimpiar.setText("Limpiar");
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
            }
        });

        jLabel4.setForeground(new java.awt.Color(0, 0, 0));
        jLabel4.setText("Resultado");

        txtMostrarResultado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMostrarResultadoActionPerformed(evt);
            }
        });

        btnCerrar.setText("Cerrar");
        btnCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnCerrar)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel4)
                            .addGap(91, 91, 91)
                            .addComponent(txtMostrarResultado))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(btnCalcular, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(132, 132, 132)
                                .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3))
                                .addGap(26, 26, 26)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(txtPorcentajeBonificacion, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtSalarioBase, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap(58, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtSalarioBase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtPorcentajeBonificacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(39, 39, 39)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCalcular, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 70, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtMostrarResultado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23)
                .addComponent(btnCerrar)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtSalarioBaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSalarioBaseActionPerformed
        // TODO add your handling code here:
                txtPorcentajeBonificacion.requestFocus();

    }//GEN-LAST:event_txtSalarioBaseActionPerformed

    private void txtPorcentajeBonificacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPorcentajeBonificacionActionPerformed
        // TODO add your handling code here:
                calcularSalarioTotal();

    }//GEN-LAST:event_txtPorcentajeBonificacionActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        // TODO add your handling code here:
                limpiarCampos();

    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnCalcularActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCalcularActionPerformed
        // TODO add your handling code here:
                calcularSalarioTotal();

    }//GEN-LAST:event_btnCalcularActionPerformed

    private void txtMostrarResultadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMostrarResultadoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMostrarResultadoActionPerformed

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        // TODO add your handling code here:
        MenuEmpleados menu = new MenuEmpleados();   // crear ventana principal
       
    menu.setVisible(true);                // mostrar

    this.dispose();
    }//GEN-LAST:event_btnCerrarActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new FuncionEmpleados().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCalcular;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField txtMostrarResultado;
    private javax.swing.JTextField txtPorcentajeBonificacion;
    private javax.swing.JTextField txtSalarioBase;
    // End of variables declaration//GEN-END:variables
}
