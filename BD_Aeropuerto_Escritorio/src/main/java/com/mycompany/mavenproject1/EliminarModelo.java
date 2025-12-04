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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class EliminarModelo extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(EliminarModelo.class.getName());
    private javax.swing.Timer timerBusqueda;
    private String modeloSeleccionado = ""; 

    public EliminarModelo() {
         initComponents();
        configurarTabla();
        configurarBusquedaAutomatica();
        configurarEliminarPorDobleClic(); 
        setLocationRelativeTo(null);

    }

   
    private void configurarTabla() {
        DefaultTableModel modeloTabla = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        modeloTabla.addColumn("Número de Modelo");
        modeloTabla.addColumn("Capacidad");
        modeloTabla.addColumn("Peso");
        
        jTable1.setModel(modeloTabla);
        
        jTable1.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && jTable1.getSelectedRow() != -1) {
                int filaSeleccionada = jTable1.getSelectedRow();
                modeloSeleccionado = jTable1.getValueAt(filaSeleccionada, 0).toString();
                buscarModeloTxt.setText(modeloSeleccionado);
            }
        });
    }

    
    private void mostrarErrorConexion() {
        javax.swing.JOptionPane.showMessageDialog(this,
            "❌ Error: No hay conexión a la base de datos",
            "Error de conexión",
            javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    
    private void manejarErrorSQL(SQLException e, String operacion) {
        String mensaje = "❌ Error al " + operacion + " modelo: ";
        
        if (e.getErrorCode() == -803) { 
            mensaje += "No se puede eliminar porque está siendo usado en otras tablas";
        } else {
            mensaje += e.getMessage();
        }
        
        javax.swing.JOptionPane.showMessageDialog(this,
            mensaje,
            "Error DB2",
            javax.swing.JOptionPane.ERROR_MESSAGE);
        
        logger.severe("Error SQL en " + operacion + ": " + e.toString());
    }
    
    private void cerrarRecursos(ResultSet rs, PreparedStatement pstmt) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        } catch (SQLException e) {
            logger.warning("Error al cerrar recursos: " + e.toString());
        }
    }
    
    
    
     private void configurarBusquedaAutomatica() {
        timerBusqueda = new javax.swing.Timer(300, e -> {
            buscarAutomaticamente();
        });
        timerBusqueda.setRepeats(false);
        
        buscarModeloTxt.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                iniciarBusquedaAutomatica();
                modeloSeleccionado = ""; 
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                iniciarBusquedaAutomatica();
                modeloSeleccionado = ""; 
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                iniciarBusquedaAutomatica();
            }
            
            private void iniciarBusquedaAutomatica() {
                if (timerBusqueda.isRunning()) {
                    timerBusqueda.restart();
                } else {
                    timerBusqueda.start();
                }
            }
        });
    }

    private void buscarAutomaticamente() {
        String textoBusqueda = buscarModeloTxt.getText().trim();
        
        if (textoBusqueda.isEmpty()) {
            limpiarTabla();
            return;
        }
        
        if (textoBusqueda.length() < 2) {
            return;
        }
        
        buscarEnDB(textoBusqueda);
    }
    
     private void eliminarModeloDB(String numeroModelo) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = Conexion_DB.getInstance().getConnection();
            
            if (conn == null) {
                mostrarErrorConexion();
                return;
            }
            
            if (!verificarExistenciaModelo(numeroModelo)) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "❌ El modelo '" + numeroModelo + "' no existe",
                    "Modelo no encontrado",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String sql = "DELETE FROM ModelosAvion WHERE UPPER(ModelNumber) = UPPER(?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, numeroModelo);
            
            int filasEliminadas = pstmt.executeUpdate();
            
            if (filasEliminadas > 0) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "✅ Modelo '" + numeroModelo + "' eliminado exitosamente",
                    "Eliminación exitosa",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
                
                vaciarCampos();
                modeloSeleccionado = "";
                
                if (!buscarModeloTxt.getText().trim().isEmpty()) {
                    buscarAutomaticamente();
                }
            } else {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "❌ No se pudo eliminar el modelo '" + numeroModelo + "'",
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (SQLException e) {
            manejarErrorSQL(e, "eliminar");
        } finally {
            cerrarRecursos(null, pstmt);
        }
    }
     
     
        
        private boolean verificarExistenciaModelo(String numeroModelo) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = Conexion_DB.getInstance().getConnection();
            
            if (conn == null) {
                return false;
            }
            
            String sql = "SELECT 1 FROM ModelosAvion WHERE UPPER(ModelNumber) = UPPER(?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, numeroModelo);
            
            rs = pstmt.executeQuery();
            return rs.next();
            
        } catch (SQLException e) {
            logger.severe("Error al verificar existencia: " + e.toString());
            return false;
        } finally {
            cerrarRecursos(rs, pstmt);
        }
    }
        
   private void buscarEnDB(String textoBusqueda) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = Conexion_DB.getInstance().getConnection();
            
            if (conn == null) {
                mostrarErrorConexion();
                return;
            }
            
            String sql = "SELECT ModelNumber, Capacidad, Peso FROM ModelosAvion WHERE UPPER(ModelNumber) LIKE UPPER(?) ORDER BY ModelNumber";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + textoBusqueda + "%");
            
            rs = pstmt.executeQuery();
            
            actualizarTablaConResultados(rs);
            
        } catch (SQLException e) {
            manejarErrorSQL(e, "buscar");
        } finally {
            cerrarRecursos(rs, pstmt);
        }
    }

     private void actualizarTablaConResultados(ResultSet rs) throws SQLException {
        DefaultTableModel modeloTabla = (DefaultTableModel) jTable1.getModel();
        modeloTabla.setRowCount(0);
        
        int contador = 0;
        while (rs.next()) {
            String modelo = rs.getString("ModelNumber");
            int capacidad = rs.getInt("Capacidad");
            double peso = rs.getDouble("Peso");
            
            Object[] fila = {modelo, capacidad, peso};
            modeloTabla.addRow(fila);
            contador++;
        }
        
        if (contador == 1) {
            jTable1.setRowSelectionInterval(0, 0);
        }
        
        if (contador == 0) {
            modeloSeleccionado = "";
        }
    }
        
     private void vaciarCampos() {
        buscarModeloTxt.setText("");
        limpiarTabla();
        modeloSeleccionado = "";
        buscarModeloTxt.requestFocus();
        
        if (timerBusqueda != null && timerBusqueda.isRunning()) {
            timerBusqueda.stop();
        }
    }
    
    private void limpiarTabla() {
        DefaultTableModel modeloTabla = (DefaultTableModel) jTable1.getModel();
        modeloTabla.setRowCount(0);
        jTable1.clearSelection();
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
        jLabel2 = new javax.swing.JLabel();
        buscarModeloTxt = new javax.swing.JTextField();
        vaciarBtn = new javax.swing.JButton();
        eliminarModeloBtn = new javax.swing.JButton();
        mostratTodoBtn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(0, 0, 204));
        jPanel1.setForeground(new java.awt.Color(0, 0, 204));

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("Yu Gothic UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Eliminar modelo");

        jLabel2.setBackground(new java.awt.Color(255, 255, 255));
        jLabel2.setFont(new java.awt.Font("Yu Gothic UI", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Buscar por numero de modelo");

        buscarModeloTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buscarModeloTxtActionPerformed(evt);
            }
        });

        vaciarBtn.setText("Vaciar");
        vaciarBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vaciarBtnActionPerformed(evt);
            }
        });

        eliminarModeloBtn.setBackground(new java.awt.Color(204, 51, 0));
        eliminarModeloBtn.setIcon(new javax.swing.ImageIcon("C:\\Users\\govan\\OneDrive\\Documentos\\1-Repositorio_Taller_BD\\Proyecto_Final_Taller_BD\\BD_Aeropuerto_Escritorio\\src\\main\\java\\Imagenes_Diseño\\icons8-eliminar-30 (1).png")); // NOI18N
        eliminarModeloBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eliminarModeloBtnActionPerformed(evt);
            }
        });

        mostratTodoBtn.setBackground(new java.awt.Color(0, 102, 0));
        mostratTodoBtn.setForeground(new java.awt.Color(255, 255, 255));
        mostratTodoBtn.setText("Mostrar todo");
        mostratTodoBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mostratTodoBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(mostratTodoBtn)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(28, 28, 28)
                        .addComponent(buscarModeloTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(vaciarBtn)
                        .addGap(33, 33, 33)
                        .addComponent(eliminarModeloBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(10, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(279, 279, 279))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(68, 68, 68)
                        .addComponent(eliminarModeloBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1)
                        .addGap(50, 50, 50)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(buscarModeloTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(vaciarBtn)
                            .addComponent(jLabel2))))
                .addGap(18, 18, 18)
                .addComponent(mostratTodoBtn)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

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

        jButton1.setBackground(new java.awt.Color(102, 102, 102));
        jButton1.setIcon(new javax.swing.ImageIcon("C:\\Users\\govan\\OneDrive\\Documentos\\1-Repositorio_Taller_BD\\Proyecto_Final_Taller_BD\\BD_Aeropuerto_Escritorio\\src\\main\\java\\Imagenes_Diseño\\icons8-volver-24.png")); // NOI18N
        jButton1.setText("Regresar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

    private void buscarModeloTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buscarModeloTxtActionPerformed
        // TODO add your handling code here:
        buscarAutomaticamente();
    }//GEN-LAST:event_buscarModeloTxtActionPerformed

    private void vaciarBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vaciarBtnActionPerformed
        // TODO add your handling code here:
        vaciarCampos();
    }//GEN-LAST:event_vaciarBtnActionPerformed

    private void eliminarModeloBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eliminarModeloBtnActionPerformed
        // TODO add your handling code here:
         if (modeloSeleccionado.isEmpty()) {
            modeloSeleccionado = buscarModeloTxt.getText().trim();
            
            if (modeloSeleccionado.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "❌ Por favor ingrese o seleccione un modelo para eliminar",
                    "Sin selección",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        
        int confirmacion = javax.swing.JOptionPane.showConfirmDialog(this,
            "¿Está seguro de eliminar el modelo: " + modeloSeleccionado + "?",
            "Confirmar eliminación",
            javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.WARNING_MESSAGE);
        
        if (confirmacion == javax.swing.JOptionPane.YES_OPTION) {
            eliminarModeloDB(modeloSeleccionado);
        }
    }//GEN-LAST:event_eliminarModeloBtnActionPerformed

    private void mostratTodoBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mostratTodoBtnActionPerformed
        // TODO add your handling code here:
         mostrarTodosModelos();
    }//GEN-LAST:event_mostratTodoBtnActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        MenuModelos menu = new MenuModelos();  
       
    menu.setVisible(true);                

    this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    
    
    private void mostrarTodosModelos() {
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    
    try {
        conn = Conexion_DB.getInstance().getConnection();
        
        if (conn == null) {
            mostrarErrorConexion();
            return;
        }
        
        buscarModeloTxt.setText("");
        modeloSeleccionado = "";
        
        String sql = "SELECT ModelNumber, Capacidad, Peso FROM ModelosAvion ORDER BY ModelNumber";
        pstmt = conn.prepareStatement(sql);
        rs = pstmt.executeQuery();
        
        actualizarTablaConResultados(rs);
        
        DefaultTableModel modeloTabla = (DefaultTableModel) jTable1.getModel();
        int totalModelos = modeloTabla.getRowCount();
        
        if (totalModelos > 0) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "✅ Se muestran " + totalModelos + " modelos",
                "Mostrar todos",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } else {
            javax.swing.JOptionPane.showMessageDialog(this,
                "ℹ️ No hay modelos registrados",
                "Sin resultados",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
        
    } catch (SQLException e) {
        manejarErrorSQL(e, "mostrar todos");
    } finally {
        cerrarRecursos(rs, pstmt);
    }
}
    
    
    
    
    private void configurarEliminarPorDobleClic() {
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) { 
                    int fila = jTable1.getSelectedRow();
                    if (fila != -1) {
                        String modelo = jTable1.getValueAt(fila, 0).toString();
                        
                        int confirmacion = javax.swing.JOptionPane.showConfirmDialog(
                            EliminarModelo.this,
                            "¿Eliminar modelo " + modelo + "?",
                            "Confirmar eliminación",
                            javax.swing.JOptionPane.YES_NO_OPTION
                        );
                        
                        if (confirmacion == javax.swing.JOptionPane.YES_OPTION) {
                            eliminarModeloDB(modelo);
                        }
                    }
                }
            }
        });
    }
    
    
    
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
        java.awt.EventQueue.invokeLater(() -> new EliminarModelo().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField buscarModeloTxt;
    private javax.swing.JButton eliminarModeloBtn;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JButton mostratTodoBtn;
    private javax.swing.JButton vaciarBtn;
    // End of variables declaration//GEN-END:variables
}
