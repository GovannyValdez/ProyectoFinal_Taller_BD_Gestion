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

public class ConsultarModelo extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ConsultarModelo.class.getName());
    private javax.swing.Timer timerBusqueda; // Timer para buscar después de pausa

    public ConsultarModelo() {
        initComponents();
        configurarTabla();
        configurarBusquedaAutomatica();
    }

    // ========== CONFIGURAR TABLA ==========
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
    }

    // ========== CONFIGURAR BÚSQUEDA AUTOMÁTICA ==========
    private void configurarBusquedaAutomatica() {
        // Crear timer que se activa 300ms después de dejar de escribir
        timerBusqueda = new javax.swing.Timer(300, e -> {
            buscarAutomaticamente();
        });
        timerBusqueda.setRepeats(false); // Solo se ejecuta una vez
        
        // Agregar DocumentListener al campo de búsqueda
        buscarModeloTxt.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                iniciarBusquedaAutomatica();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                iniciarBusquedaAutomatica();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                iniciarBusquedaAutomatica();
            }
            
            private void iniciarBusquedaAutomatica() {
                // Reiniciar timer cada vez que se escribe
                if (timerBusqueda.isRunning()) {
                    timerBusqueda.restart();
                } else {
                    timerBusqueda.start();
                }
            }
        });
    }

    // ========== BÚSQUEDA AUTOMÁTICA ==========
    private void buscarAutomaticamente() {
        String textoBusqueda = buscarModeloTxt.getText().trim();
        
        // Si está vacío, limpiar tabla
        if (textoBusqueda.isEmpty()) {
            limpiarTabla();
            return;
        }
        
        // Si tiene menos de 2 caracteres, no buscar (opcional)
        if (textoBusqueda.length() < 2) {
            return;
        }
        
        // Buscar en la base de datos
        buscarEnDB(textoBusqueda);
    }
    // ========== MÉTODO PARA BUSCAR EN BASE DE DATOS ==========
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
            
            // Búsqueda por coincidencia parcial (LIKE)
            String sql = "SELECT ModelNumber, Capacidad, Peso FROM ModelosAvion WHERE UPPER(ModelNumber) LIKE ? ORDER BY ModelNumber";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + textoBusqueda.toUpperCase() + "%");
            
            rs = pstmt.executeQuery();
            
            // Actualizar tabla con resultados
            actualizarTablaConResultados(rs, textoBusqueda);
            
        } catch (SQLException e) {
            manejarErrorSQL(e);
        } finally {
            cerrarRecursos(rs, pstmt);
        }
    }
    private void actualizarTablaConResultados(ResultSet rs, String textoBusqueda) throws SQLException {
        DefaultTableModel modeloTabla = (DefaultTableModel) jTable1.getModel();
        modeloTabla.setRowCount(0); // Limpiar tabla
        
        int contador = 0;
        while (rs.next()) {
            String modelo = rs.getString("ModelNumber");
            int capacidad = rs.getInt("Capacidad");
            double peso = rs.getDouble("Peso");
            
            Object[] fila = {modelo, capacidad, peso};
            modeloTabla.addRow(fila);
            contador++;
        }
        
        // Mostrar mensaje en la barra de estado si quieres (opcional)
        if (contador == 0) {
            mostrarMensajeInfo("No se encontraron modelos que coincidan con: " + textoBusqueda);
        }
    }
    
    private void vaciarCampos() {
        buscarModeloTxt.setText("");
        limpiarTabla();
        buscarModeloTxt.requestFocus();
        
        // Detener timer si está activo
        if (timerBusqueda != null && timerBusqueda.isRunning()) {
            timerBusqueda.stop();
        }
    }
    
    // ========== LIMPIAR TABLA ==========
    private void limpiarTabla() {
        DefaultTableModel modeloTabla = (DefaultTableModel) jTable1.getModel();
        modeloTabla.setRowCount(0);
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    private void mostrarErrorConexion() {
        javax.swing.JOptionPane.showMessageDialog(this,
            "❌ Error: No hay conexión a la base de datos",
            "Error de conexión",
            javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    
    
    private void manejarErrorSQL(SQLException e) {
        javax.swing.JOptionPane.showMessageDialog(this,
            "❌ Error de base de datos: " + e.getMessage(),
            "Error DB2",
            javax.swing.JOptionPane.ERROR_MESSAGE);
        logger.severe("Error SQL al buscar modelo: " + e.toString());
    }
    
    private void cerrarRecursos(ResultSet rs, PreparedStatement pstmt) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        } catch (SQLException e) {
            logger.warning("Error al cerrar recursos: " + e.toString());
        }
    }
    
    private void mostrarMensajeInfo(String mensaje) {
        // Opcional: Puedes mostrar un mensaje en la interfaz o en consola
        System.out.println("ℹ️ " + mensaje);
    }
    
    // ========== MÉTODO PARA MOSTRAR TODOS LOS MODELOS (OPCIONAL) ==========
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
            
            String sql = "SELECT ModelNumber, Capacidad, Peso FROM ModelosAvion ORDER BY ModelNumber";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
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
            
            javax.swing.JOptionPane.showMessageDialog(this,
                "✅ Se encontraron " + contador + " modelos",
                "Resultados",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
            
        } catch (SQLException e) {
            manejarErrorSQL(e);
        } finally {
            cerrarRecursos(rs, pstmt);
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
        jLabel2 = new javax.swing.JLabel();
        buscarModeloTxt = new javax.swing.JTextField();
        buscarBtn = new javax.swing.JButton();
        vaciarBtn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(0, 0, 204));

        jLabel1.setText("Consultar Modelo");

        jLabel2.setText("Numero del modelo");

        buscarModeloTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buscarModeloTxtActionPerformed(evt);
            }
        });

        buscarBtn.setBackground(new java.awt.Color(0, 153, 204));
        buscarBtn.setIcon(new javax.swing.ImageIcon("C:\\Users\\govan\\OneDrive\\Documentos\\1-Repositorio_Taller_BD\\Proyecto_Final_Taller_BD\\BD_Aeropuerto_Escritorio\\src\\main\\java\\Imagenes_Diseño\\icons8-buscar-24.png")); // NOI18N
        buscarBtn.setText("Buscar");
        buscarBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buscarBtnActionPerformed(evt);
            }
        });

        vaciarBtn.setBackground(new java.awt.Color(204, 51, 0));
        vaciarBtn.setText("X");
        vaciarBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vaciarBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(323, 323, 323)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addComponent(jLabel2)
                .addGap(64, 64, 64)
                .addComponent(buscarModeloTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(vaciarBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 61, Short.MAX_VALUE)
                .addComponent(buscarBtn)
                .addGap(64, 64, 64))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(50, 50, 50)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(buscarModeloTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buscarBtn)
                    .addComponent(vaciarBtn))
                .addContainerGap(50, Short.MAX_VALUE))
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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                .addContainerGap())
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
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buscarModeloTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buscarModeloTxtActionPerformed
        // TODO add your handling code here:
         String numeroModelo = buscarModeloTxt.getText().trim();
        
        if (numeroModelo.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "❌ Por favor ingrese un número de modelo",
                "Campo vacío",
                javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        buscarEnDB(numeroModelo);
    }//GEN-LAST:event_buscarModeloTxtActionPerformed

    private void vaciarBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vaciarBtnActionPerformed
        // TODO add your handling code here:
        vaciarCampos();
    }//GEN-LAST:event_vaciarBtnActionPerformed

    private void buscarBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buscarBtnActionPerformed
        // TODO add your handling code here:
        buscarBtn.doClick();
    }//GEN-LAST:event_buscarBtnActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new ConsultarModelo().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buscarBtn;
    private javax.swing.JTextField buscarModeloTxt;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JButton vaciarBtn;
    // End of variables declaration//GEN-END:variables
}
