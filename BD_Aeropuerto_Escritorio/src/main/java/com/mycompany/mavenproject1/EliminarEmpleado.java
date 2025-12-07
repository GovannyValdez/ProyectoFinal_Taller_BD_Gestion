/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.mavenproject1;
import javax.swing.Timer;
import javax.swing.table.TableRowSorter;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.SwingUtilities;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.SwingUtilities;
/**
 *
 * @author govan
 */
public class EliminarEmpleado extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(EliminarEmpleado.class.getName());

    private static final String DB_URL = "jdbc:db2://localhost:25000/BD_AEROP";
    private static final String DB_USER = "db2admin";
    private static final String DB_PASSWORD = "Govanny27";
    
    private DefaultTableModel modelo;
    private TableRowSorter<DefaultTableModel> sorter;
    private String ssnSeleccionado;
    
    private Timer timerFiltro;
    private static final int DELAY_FILTRO = 300;

    public EliminarEmpleado() {
    initComponents();
    setLocationRelativeTo(null);
    
    inicializarTabla();
    configurarFiltroTiempoReal();
    cargarTodosEmpleados();  
    
     mostrarInfoTabla();
}
    
    private void configurarFiltroTiempoReal() {
        timerFiltro = new Timer(DELAY_FILTRO, e -> aplicarFiltroTiempoReal());
        timerFiltro.setRepeats(false);
        
        ssnTxt.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                timerFiltro.restart();
            }
        });
        
        nombreTxt.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                timerFiltro.restart();
            }
        });
    }
    
    private void aplicarFiltroTiempoReal() {
    SwingUtilities.invokeLater(() -> {
        String ssn = obtenerTextoReal(ssnTxt);
        String nombre = obtenerTextoReal(nombreTxt);
        
        System.out.println("Filtrando - SSN: '" + ssn + "', Nombre: '" + nombre + "'");
        
        if ((ssn != null && !ssn.isEmpty()) || (nombre != null && !nombre.isEmpty())) {
            filtrarEnMemoria(ssn, nombre);
        } else {
            // Cuando ambos campos están vacíos, QUITAR el filtro
            if (sorter != null) {
                sorter.setRowFilter(null);
            }
            System.out.println("Sin filtros - Mostrando todos los empleados");
        }
    });
}    
    private String obtenerTextoReal(JTextField campo) {
    String texto = campo.getText().trim();
    
    System.out.println("Campo texto: '" + texto + "'");
    
    if (texto.isEmpty() || 
        texto.equals("Ingresar...") || 
        texto.equals("Ej: 123-45-6789") || 
        texto.equals("Ej: Juan Pérez") ||
        texto.equals("Ej: Juan")) {
        return null;
    }
    
    return texto;
}
    
    private void inicializarTabla() {
    String[] columnas = {
        "SSN", "Nombre", "Apellido Paterno", "Apellido Materno",
        "Dirección", "Teléfono", "Salario", "Número Membresía"
    };
    
    modelo = new DefaultTableModel(columnas, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    
    jTable1.setModel(modelo);
    
    sorter = new TableRowSorter<>(modelo);
    jTable1.setRowSorter(sorter);
    
    
    jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    jTable1.getSelectionModel().addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
            int filaSeleccionada = jTable1.getSelectedRow();
            if (filaSeleccionada >= 0) {
                int modeloIndex = jTable1.convertRowIndexToModel(filaSeleccionada);
                ssnSeleccionado = (String) modelo.getValueAt(modeloIndex, 0);
                eliminarTxt.setEnabled(true);
                System.out.println("Seleccionado SSN: " + ssnSeleccionado);
            }
        }
    });
    
    ajustarAnchosColumnas();
}
    
    private void ajustarAnchosColumnas() {
        if (jTable1.getColumnModel().getColumnCount() >= 8) {
            jTable1.getColumnModel().getColumn(0).setPreferredWidth(100);
            jTable1.getColumnModel().getColumn(1).setPreferredWidth(150);
            jTable1.getColumnModel().getColumn(2).setPreferredWidth(120);
            jTable1.getColumnModel().getColumn(3).setPreferredWidth(120);
            jTable1.getColumnModel().getColumn(4).setPreferredWidth(180);
            jTable1.getColumnModel().getColumn(5).setPreferredWidth(100);
            jTable1.getColumnModel().getColumn(6).setPreferredWidth(80);
            jTable1.getColumnModel().getColumn(7).setPreferredWidth(120);
        }
    }
    
   private void filtrarEnMemoria(String ssnFiltro, String nombreFiltro) {
    if (sorter == null) {
        System.out.println("Error: sorter es null");
        return;
    }
    
    System.out.println("Aplicando filtro - SSN: '" + ssnFiltro + "', Nombre: '" + nombreFiltro + "'");
    System.out.println("Total filas en modelo: " + modelo.getRowCount());
    
    RowFilter<DefaultTableModel, Integer> filtro = new RowFilter<DefaultTableModel, Integer>() {
        @Override
        public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
            boolean mostrar = true;
            
            try {
                // Filtrar por SSN
                if (ssnFiltro != null && !ssnFiltro.trim().isEmpty()) {
                    String ssn = (String) entry.getValue(0);
                    String filtroSSN = ssnFiltro.toLowerCase().trim();
                    
                    if (ssn == null || !ssn.toLowerCase().contains(filtroSSN)) {
                        mostrar = false;
                    }
                }
                
                if (mostrar && nombreFiltro != null && !nombreFiltro.trim().isEmpty()) {
                    String nombre = (String) entry.getValue(1);
                    String apellidoP = (String) entry.getValue(2);
                    String apellidoM = (String) entry.getValue(3);
                    String filtroNombre = nombreFiltro.toLowerCase().trim();
                    
                    boolean coincide = false;
                    
                    if (nombre != null && nombre.toLowerCase().contains(filtroNombre)) {
                        coincide = true;
                    }
                    if (apellidoP != null && apellidoP.toLowerCase().contains(filtroNombre)) {
                        coincide = true;
                    }
                    if (apellidoM != null && apellidoM.toLowerCase().contains(filtroNombre)) {
                        coincide = true;
                    }
                    
                    if (!coincide) {
                        mostrar = false;
                    }
                }
                
                return mostrar;
                
            } catch (Exception e) {
                System.out.println("Error en filtro: " + e.getMessage());
                return true;
            }
        }
    };
    
    sorter.setRowFilter(filtro);
    
    int filasVisibles = jTable1.getRowCount();
    System.out.println("Resultado: " + filasVisibles + " filas visibles");
}
    
    private void cargarTodosEmpleados() {
    Connection conn = null;
    Statement stmt = null;
    ResultSet rs = null;
    
    try {
        modelo.setRowCount(0);
        ssnSeleccionado = null;
        eliminarTxt.setEnabled(false);
        
        Class.forName("com.ibm.db2.jcc.DB2Driver");
        conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        
        String sql = "SELECT ssn, nombre, apellido_paterno, apellido_materno, " +
                    "direccion, telefono, salario, numero_membresia_sindicato " +
                    "FROM EMPLEADO ORDER BY apellido_paterno, nombre";
        
        stmt = conn.createStatement();
        rs = stmt.executeQuery(sql);
        
        
        
        
        int contador = 0;
        while (rs.next()) {
            modelo.addRow(new Object[]{
                rs.getString("ssn"),
                rs.getString("nombre"),
                rs.getString("apellido_paterno"),
                rs.getString("apellido_materno") != null ? rs.getString("apellido_materno") : "",
                rs.getString("direccion") != null ? rs.getString("direccion") : "",
                rs.getString("telefono") != null ? rs.getString("telefono") : "",
                String.format("$%,.2f", rs.getBigDecimal("salario")),
                rs.getString("numero_membresia_sindicato")
            });
            contador++;
        }
        
        System.out.println("Cargados " + contador + " empleados desde DB2");
        setTitle("Eliminar Empleado - " + contador + " empleados");
        
        if (contador == 0) {
            JOptionPane.showMessageDialog(this, 
                "No hay empleados registrados.", 
                "Sin datos", 
                JOptionPane.INFORMATION_MESSAGE);
        }
        
    } catch (Exception e) {
        manejarError(e);
    } finally {
        cerrarRecursos(rs, stmt, conn);
    }
}
    
    
    
    private boolean eliminarEmpleado(String ssn) {
        if (ssn == null || ssn.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Seleccione un empleado de la tabla primero.", 
                "Selección requerida", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        
        
        
        String nombreEmpleado = "";
        for (int i = 0; i < modelo.getRowCount(); i++) {
            if (ssn.equals(modelo.getValueAt(i, 0))) {
                nombreEmpleado = modelo.getValueAt(i, 1) + " " + modelo.getValueAt(i, 2);
                break;
            }
        }
        
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Eliminar al empleado?\n\n" +
            "SSN: " + ssn + "\n" +
            "Nombre: " + nombreEmpleado + "\n\n" +
            "Esta acción no se puede deshacer.",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirmacion != JOptionPane.YES_OPTION) {
            return false;
        }
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            
            String sql = "DELETE FROM EMPLEADO WHERE ssn = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, ssn);
            
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this,
                    "✅ Empleado eliminado exitosamente",
                    "Eliminación exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Recargar datos
                cargarTodosEmpleados();
                return true;
            }
            
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                JOptionPane.showMessageDialog(this,
                    "No se puede eliminar el empleado porque tiene registros relacionados.",
                    "Error de integridad referencial",
                    JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error de base de datos: " + e.getMessage(),
                    "Error DB2",
                    JOptionPane.ERROR_MESSAGE);
            }
            return false;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error al cerrar recursos", e);
            }
        }
        
        
        return false;
    }
    
    private void manejarError(Exception e) {
        logger.log(Level.SEVERE, "Error", e);
        
        if (e instanceof ClassNotFoundException) {
            JOptionPane.showMessageDialog(this, 
                "Driver DB2 no encontrado. Agrega db2jcc4.jar al proyecto.", 
                "Error", JOptionPane.ERROR_MESSAGE);
        } else if (e instanceof SQLException) {
            SQLException sqlEx = (SQLException) e;
            if (sqlEx.getMessage().contains("does not exist") || "42704".equals(sqlEx.getSQLState())) {
                JOptionPane.showMessageDialog(this, 
                    "La tabla EMPLEADO no existe. Créala primero en DB2.", 
                    "Tabla no encontrada", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Error DB2: " + e.getMessage(), 
                    "Error de base de datos", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void cerrarRecursos(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al cerrar recursos", e);
        }
          
        
        
    }
    
    private void mostrarInfoTabla() {
    System.out.println("=== INFO TABLA ===");
    System.out.println("Filas en modelo: " + modelo.getRowCount());
    System.out.println("Columnas: " + modelo.getColumnCount());
    
    for (int i = 0; i < modelo.getColumnCount(); i++) {
        System.out.println("Col " + i + ": " + modelo.getColumnName(i));
    }
    
    if (modelo.getRowCount() > 0) {
        System.out.println("Primera fila de datos:");
        for (int i = 0; i < modelo.getColumnCount(); i++) {
            System.out.println("  " + modelo.getColumnName(i) + ": " + modelo.getValueAt(0, i));
        }
    }
    System.out.println("==================");
}
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton4 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        ssnTxt = new javax.swing.JTextField();
        nombreTxt = new javax.swing.JTextField();
        filtrarSsnTxt = new javax.swing.JButton();
        filtratNombreTxt = new javax.swing.JButton();
        eliminarTxt = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(0, 0, 204));

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

        jButton4.setIcon(new javax.swing.ImageIcon("C:\\Users\\govan\\OneDrive\\Documentos\\1-Repositorio_Taller_BD\\Proyecto_Final_Taller_BD\\BD_Aeropuerto_Escritorio\\src\\main\\java\\Imagenes_Diseño\\icons8-volver-24.png")); // NOI18N
        jButton4.setText("Regresar");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton4)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 657, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addContainerGap(28, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Yu Gothic UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setText("Eliminar Empleado");

        jLabel2.setFont(new java.awt.Font("Yu Gothic UI", 1, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 0, 0));
        jLabel2.setText("Filtrar por SSN");

        jLabel3.setFont(new java.awt.Font("Yu Gothic UI", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(0, 0, 0));
        jLabel3.setText("Filtrar por nombre");

        ssnTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ssnTxtActionPerformed(evt);
            }
        });

        nombreTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nombreTxtActionPerformed(evt);
            }
        });

        filtrarSsnTxt.setBackground(new java.awt.Color(102, 102, 102));
        filtrarSsnTxt.setText("Filtrar ");
        filtrarSsnTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filtrarSsnTxtActionPerformed(evt);
            }
        });

        filtratNombreTxt.setBackground(new java.awt.Color(102, 102, 102));
        filtratNombreTxt.setText("Filtrar");
        filtratNombreTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filtratNombreTxtActionPerformed(evt);
            }
        });

        eliminarTxt.setBackground(new java.awt.Color(255, 0, 0));
        eliminarTxt.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        eliminarTxt.setForeground(new java.awt.Color(255, 255, 255));
        eliminarTxt.setIcon(new javax.swing.ImageIcon("C:\\Users\\govan\\OneDrive\\Documentos\\1-Repositorio_Taller_BD\\Proyecto_Final_Taller_BD\\BD_Aeropuerto_Escritorio\\src\\main\\java\\Imagenes_Diseño\\icons8-eliminar-30 (1).png")); // NOI18N
        eliminarTxt.setText("Eliminar");
        eliminarTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eliminarTxtActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(237, 237, 237)
                        .addComponent(jLabel1))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(32, 32, 32)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(nombreTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                            .addComponent(ssnTxt))))
                .addGap(28, 28, 28)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(filtrarSsnTxt)
                    .addComponent(filtratNombreTxt))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(eliminarTxt)
                .addGap(28, 28, 28))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(ssnTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(filtrarSsnTxt))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(nombreTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(filtratNombreTxt)))
                    .addComponent(eliminarTxt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 36, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    
    
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
         MenuEmpleados menu = new MenuEmpleados();   // crear ventana principal
       
    menu.setVisible(true);                // mostrar

    this.dispose();
    }//GEN-LAST:event_jButton4ActionPerformed

    
    
    
    
    
    private void eliminarTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eliminarTxtActionPerformed
        // TODO add your handling code here:
        eliminarEmpleado(ssnSeleccionado);
    }//GEN-LAST:event_eliminarTxtActionPerformed

    private void ssnTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ssnTxtActionPerformed
        // TODO add your handling code here:
        
        filtrarSsnTxtActionPerformed(evt);
        
    }//GEN-LAST:event_ssnTxtActionPerformed

    private void nombreTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nombreTxtActionPerformed
        // TODO add your handling code here:
       aplicarFiltroTiempoReal();
    }//GEN-LAST:event_nombreTxtActionPerformed

    private void filtrarSsnTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filtrarSsnTxtActionPerformed
        // TODO add your handling code here:
        aplicarFiltroTiempoReal();
    }//GEN-LAST:event_filtrarSsnTxtActionPerformed

    private void filtratNombreTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filtratNombreTxtActionPerformed
        // TODO add your handling code here:
         aplicarFiltroTiempoReal();
    }//GEN-LAST:event_filtratNombreTxtActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new EliminarEmpleado().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton eliminarTxt;
    private javax.swing.JButton filtrarSsnTxt;
    private javax.swing.JButton filtratNombreTxt;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField nombreTxt;
    private javax.swing.JTextField ssnTxt;
    // End of variables declaration//GEN-END:variables
}
