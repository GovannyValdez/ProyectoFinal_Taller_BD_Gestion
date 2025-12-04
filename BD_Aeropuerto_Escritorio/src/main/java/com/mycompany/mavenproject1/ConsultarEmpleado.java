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
import javax.swing.Timer;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.SwingUtilities;

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
    private TableRowSorter<DefaultTableModel> sorter;
    private Timer timerFiltro;
    private static final int DELAY_FILTRO = 300;

    public ConsultarEmpleado() {
        initComponents();
        setLocationRelativeTo(null);
        inicializarTabla();
        configurarFiltroTiempoReal();
        cargarTodosEmpleados();
    }
    
    private void configurarFiltroTiempoReal() {
        timerFiltro = new Timer(DELAY_FILTRO, e -> aplicarFiltroTiempoReal());
        timerFiltro.setRepeats(false);
        
        
        filtrarTxt.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                timerFiltro.restart();
            }
        });
        
        
        filtrarNombreTxt.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                timerFiltro.restart();
            }
        });
    }
    
    private void aplicarFiltroTiempoReal() {
        SwingUtilities.invokeLater(() -> {
            String ssn = filtrarTxt.getText().trim();
            String nombre = filtrarNombreTxt.getText().trim();
            
            if (!ssn.isEmpty() || !nombre.isEmpty()) {
                filtrarEnMemoria(ssn, nombre);
            } else {
                // Si ambos campos están vacíos, mostrar todos
                if (sorter != null) {
                    sorter.setRowFilter(null);
                }
            }
        });
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
        
        
        jTable1.setAutoCreateRowSorter(false);
        
       
        ajustarAnchosColumnas();
    }
    
    private void ajustarAnchosColumnas() {
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(100);
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(150);
        jTable1.getColumnModel().getColumn(2).setPreferredWidth(120);
        jTable1.getColumnModel().getColumn(3).setPreferredWidth(120);
        jTable1.getColumnModel().getColumn(4).setPreferredWidth(200);
        jTable1.getColumnModel().getColumn(5).setPreferredWidth(100);
        jTable1.getColumnModel().getColumn(6).setPreferredWidth(100);
        jTable1.getColumnModel().getColumn(7).setPreferredWidth(120);
    }
    
    private void filtrarEnMemoria(String ssnFiltro, String nombreFiltro) {
        if (sorter == null) return;
        
        RowFilter<DefaultTableModel, Integer> filtro = new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                boolean cumpleSSN = true;
                boolean cumpleNombre = true;
                
                if (!ssnFiltro.isEmpty()) {
                    String ssn = (String) entry.getValue(0);
                    cumpleSSN = ssn != null && ssn.toLowerCase().contains(ssnFiltro.toLowerCase());
                }
                
                if (!nombreFiltro.isEmpty()) {
                    String nombre = (String) entry.getValue(1);
                    String apellidoP = (String) entry.getValue(2);
                    String apellidoM = (String) entry.getValue(3);
                    
                    String buscar = nombreFiltro.toLowerCase();
                    
                    boolean coincide = false;
                    if (nombre != null && nombre.toLowerCase().contains(buscar)) {
                        coincide = true;
                    }
                    if (apellidoP != null && apellidoP.toLowerCase().contains(buscar)) {
                        coincide = true;
                    }
                    if (apellidoM != null && apellidoM.toLowerCase().contains(buscar)) {
                        coincide = true;
                    }
                    
                    cumpleNombre = coincide;
                }
                
                return cumpleSSN && cumpleNombre;
            }
        };
        
        sorter.setRowFilter(filtro);
        
        int visible = jTable1.getRowCount();
        int total = modelo.getRowCount();
        setTitle("Consultar Empleado - Mostrando " + visible + " de " + total + " empleados");
    }
    
    private void cargarTodosEmpleados() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            modelo.setRowCount(0);
            
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
            
            setTitle("Consultar Empleado - " + contador + " empleados");
            
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
        vaciarSsnBtn = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        filtrarNombreTxt = new javax.swing.JTextField();
        vaciarNombreBtn = new javax.swing.JButton();
        mostrarTodoBtn = new javax.swing.JButton();

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

        filtrarTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filtrarTxtActionPerformed(evt);
            }
        });

        filtrarBtn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        filtrarBtn.setIcon(new javax.swing.ImageIcon("C:\\Users\\govan\\OneDrive\\Documentos\\1-Repositorio_Taller_BD\\Proyecto_Final_Taller_BD\\BD_Aeropuerto_Escritorio\\src\\main\\java\\Imagenes_Diseño\\icons8-buscar-24.png")); // NOI18N
        filtrarBtn.setText("Buscar");
        filtrarBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filtrarBtnActionPerformed(evt);
            }
        });

        vaciarSsnBtn.setBackground(new java.awt.Color(204, 0, 0));
        vaciarSsnBtn.setForeground(new java.awt.Color(255, 255, 255));
        vaciarSsnBtn.setText("X");
        vaciarSsnBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vaciarSsnBtnActionPerformed(evt);
            }
        });

        jLabel3.setText("Buscar por Nombre");

        filtrarNombreTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filtrarNombreTxtActionPerformed(evt);
            }
        });

        vaciarNombreBtn.setBackground(new java.awt.Color(204, 0, 51));
        vaciarNombreBtn.setForeground(new java.awt.Color(255, 255, 255));
        vaciarNombreBtn.setText("X");
        vaciarNombreBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vaciarNombreBtnActionPerformed(evt);
            }
        });

        mostrarTodoBtn.setText("Mostrar todo");
        mostrarTodoBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mostrarTodoBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(60, 60, 60)
                                .addComponent(jLabel1)
                                .addContainerGap(379, Short.MAX_VALUE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(94, 94, 94)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(filtrarNombreTxt)
                                    .addComponent(filtrarTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(vaciarSsnBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                                    .addComponent(vaciarNombreBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(filtrarBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(mostrarTodoBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(74, 74, 74))))))
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
                    .addComponent(filtrarBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vaciarSsnBtn))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(filtrarNombreTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(vaciarNombreBtn)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(mostrarTodoBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
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
        aplicarFiltroTiempoReal();
    }//GEN-LAST:event_filtrarBtnActionPerformed

    private void vaciarSsnBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vaciarSsnBtnActionPerformed
        
        filtrarTxt.setText("");
        filtrarTxt.requestFocus();
        aplicarFiltroTiempoReal(); // Actualizar filtro
    }//GEN-LAST:event_vaciarSsnBtnActionPerformed

    private void filtrarTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filtrarTxtActionPerformed
        // TODO add your handling code here:
        aplicarFiltroTiempoReal();
    }//GEN-LAST:event_filtrarTxtActionPerformed

    private void filtrarNombreTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filtrarNombreTxtActionPerformed
        // TODO add your handling code here:
        aplicarFiltroTiempoReal();
    }//GEN-LAST:event_filtrarNombreTxtActionPerformed

    private void vaciarNombreBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vaciarNombreBtnActionPerformed
        // TODO add your handling code here:
        filtrarNombreTxt.setText("");
        filtrarNombreTxt.requestFocus();
        aplicarFiltroTiempoReal(); // Actualizar filtro
    }//GEN-LAST:event_vaciarNombreBtnActionPerformed

    private void mostrarTodoBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mostrarTodoBtnActionPerformed
        // TODO add your handling code here:
        filtrarTxt.setText("");
        filtrarNombreTxt.setText("");
        
        if (sorter != null) {
            sorter.setRowFilter(null);
        }
        
        cargarTodosEmpleados();
    }//GEN-LAST:event_mostrarTodoBtnActionPerformed

    

    private void manejarError(Exception e) {
        logger.log(Level.SEVERE, "Error", e);
        
        if (e instanceof ClassNotFoundException) {
            JOptionPane.showMessageDialog(this, 
                "Error: Driver DB2 no encontrado.\nAgrega db2jcc4.jar al proyecto.", 
                "Error", JOptionPane.ERROR_MESSAGE);
        } else if (e instanceof SQLException) {
            SQLException sqlEx = (SQLException) e;
            if (sqlEx.getMessage().contains("does not exist") || "42704".equals(sqlEx.getSQLState())) {
                JOptionPane.showMessageDialog(this, 
                    "La tabla EMPLEADO no existe.\nCréala primero en DB2.", 
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
    private javax.swing.JTextField filtrarNombreTxt;
    private javax.swing.JTextField filtrarTxt;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JButton mostrarTodoBtn;
    private javax.swing.JButton vaciarNombreBtn;
    private javax.swing.JButton vaciarSsnBtn;
    // End of variables declaration//GEN-END:variables
}
