/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.mavenproject1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModificarEmpleado extends javax.swing.JFrame {
    
    private static final Logger logger = Logger.getLogger(ModificarEmpleado.class.getName());
    
    private static final String DB_URL = "jdbc:db2://localhost:25000/BD_AEROP";
    private static final String DB_USER = "db2admin";
    private static final String DB_PASSWORD = "Govanny27";
    
    private DefaultTableModel modelo;
    private TableRowSorter<DefaultTableModel> sorter;
    private Timer timerFiltro;
    private static final int DELAY_FILTRO = 300;
    
    private String ssnActual;

    public ModificarEmpleado() {
        initComponents();
        setLocationRelativeTo(null);
        inicializarTabla();
        configurarFiltroTiempoReal();
        cargarTodosEmpleados();
        
       
        habilitarCampos(false);
        modificarBtn.setEnabled(false);
    }
    
    private void habilitarCampos(boolean habilitar) {
        nombreTxt.setEnabled(habilitar);
        apellidoPaTxt.setEnabled(habilitar);
        apellidoMaTxt.setEnabled(habilitar);
        direccionTxt.setEnabled(habilitar);
        telefonoTxt.setEnabled(habilitar);
        salarioTxt.setEnabled(habilitar);
        membresiaTxt.setEnabled(habilitar);
        modificarBtn.setEnabled(habilitar);
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
    }
    
    private void aplicarFiltroTiempoReal() {
        SwingUtilities.invokeLater(() -> {
            String ssn = ssnTxt.getText().trim();
            
            if (!ssn.isEmpty()) {
                filtrarEnMemoria(ssn);
            } else {
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
        
        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        jTable1.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int filaSeleccionada = jTable1.getSelectedRow();
                if (filaSeleccionada >= 0) {
                    cargarDatosDesdeTabla(filaSeleccionada);
                }
            }
        });
        
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
    
    private void cargarDatosDesdeTabla(int filaSeleccionada) {
        int modeloIndex = jTable1.convertRowIndexToModel(filaSeleccionada);
        
        ssnActual = (String) modelo.getValueAt(modeloIndex, 0);
        
        ssnTxt.setText(ssnActual);
        nombreTxt.setText((String) modelo.getValueAt(modeloIndex, 1));
        apellidoPaTxt.setText((String) modelo.getValueAt(modeloIndex, 2));
        apellidoMaTxt.setText((String) modelo.getValueAt(modeloIndex, 3));
        direccionTxt.setText((String) modelo.getValueAt(modeloIndex, 4));
        telefonoTxt.setText((String) modelo.getValueAt(modeloIndex, 5));
        
        String salarioStr = (String) modelo.getValueAt(modeloIndex, 6);
        if (salarioStr != null && salarioStr.startsWith("$")) {
            salarioStr = salarioStr.replace("$", "").replace(",", "");
        }
        salarioTxt.setText(salarioStr);
        
        membresiaTxt.setText((String) modelo.getValueAt(modeloIndex, 7));
        
        habilitarCampos(true);
        modificarBtn.setEnabled(true);
        
        setTitle("Modificar Empleado - Editando: " + ssnActual);
    }
    
    private void filtrarEnMemoria(String ssnFiltro) {
        if (sorter == null) return;
        
        RowFilter<DefaultTableModel, Integer> filtro = new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                if (ssnFiltro.isEmpty()) return true;
                
                String ssn = (String) entry.getValue(0);
                return ssn != null && ssn.toLowerCase().contains(ssnFiltro.toLowerCase());
            }
        };
        
        sorter.setRowFilter(filtro);
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
            
            setTitle("Modificar Empleado - " + contador + " empleados");
            
        } catch (Exception e) {
            manejarError(e);
        } finally {
            cerrarRecursos(rs, stmt, conn);
        }
    }
    
    private boolean buscarEmpleadoPorSSN(String ssn) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            
            String sql = "SELECT ssn, nombre, apellido_paterno, apellido_materno, " +
                        "direccion, telefono, salario, numero_membresia_sindicato " +
                        "FROM EMPLEADO WHERE ssn = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, ssn);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                ssnActual = ssn;
                
                ssnTxt.setText(rs.getString("ssn"));
                nombreTxt.setText(rs.getString("nombre"));
                apellidoPaTxt.setText(rs.getString("apellido_paterno"));
                apellidoMaTxt.setText(rs.getString("apellido_materno") != null ? rs.getString("apellido_materno") : "");
                direccionTxt.setText(rs.getString("direccion") != null ? rs.getString("direccion") : "");
                telefonoTxt.setText(rs.getString("telefono") != null ? rs.getString("telefono") : "");
                salarioTxt.setText(rs.getBigDecimal("salario").toString());
                membresiaTxt.setText(rs.getString("numero_membresia_sindicato"));
                
                habilitarCampos(true);
                modificarBtn.setEnabled(true);
                
               
                seleccionarEnTabla(ssn);
                
                return true;
            } else {
                JOptionPane.showMessageDialog(this,
                    "No se encontró un empleado con SSN: " + ssn,
                    "Empleado no encontrado",
                    JOptionPane.WARNING_MESSAGE);
                return false;
            }
            
        } catch (Exception e) {
            manejarError(e);
            return false;
        } finally {
            cerrarRecursos(rs, pstmt, conn);
        }
    }
    
    private void seleccionarEnTabla(String ssn) {
        for (int i = 0; i < modelo.getRowCount(); i++) {
            if (ssn.equals(modelo.getValueAt(i, 0))) {
                int vistaIndex = jTable1.convertRowIndexToView(i);
                jTable1.setRowSelectionInterval(vistaIndex, vistaIndex);
                jTable1.scrollRectToVisible(jTable1.getCellRect(vistaIndex, 0, true));
                break;
            }
        }
    }
    
    private boolean validarCampos() {
        if (nombreTxt.getText().trim().isEmpty()) {
            mostrarErrorCampo("Nombre");
            nombreTxt.requestFocus();
            return false;
        }
        
        if (apellidoPaTxt.getText().trim().isEmpty()) {
            mostrarErrorCampo("Apellido Paterno");
            apellidoPaTxt.requestFocus();
            return false;
        }
        
        if (salarioTxt.getText().trim().isEmpty()) {
            mostrarErrorCampo("Salario");
            salarioTxt.requestFocus();
            return false;
        }
        
        if (membresiaTxt.getText().trim().isEmpty()) {
            mostrarErrorCampo("Número de membresía");
            membresiaTxt.requestFocus();
            return false;
        }
        
        try {
            BigDecimal salario = new BigDecimal(salarioTxt.getText().trim());
            if (salario.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this,
                    "El salario no puede ser negativo.",
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE);
                salarioTxt.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "El salario debe ser un número válido.",
                "Error de validación",
                JOptionPane.ERROR_MESSAGE);
            salarioTxt.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private boolean modificarEmpleado() {
        if (!validarCampos()) {
            return false;
        }
        
        if (ssnActual == null || ssnActual.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Primero debe seleccionar un empleado para modificar.",
                "Selección requerida",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Confirmar modificación del empleado?\n\n" +
            "SSN: " + ssnActual + "\n" +
            "Nombre: " + nombreTxt.getText() + " " + apellidoPaTxt.getText() + "\n\n" +
            "Los cambios se guardarán permanentemente.",
            "Confirmar modificación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirmacion != JOptionPane.YES_OPTION) {
            return false;
        }
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            
            String sql = "UPDATE EMPLEADO SET " +
                        "nombre = ?, " +
                        "apellido_paterno = ?, " +
                        "apellido_materno = ?, " +
                        "direccion = ?, " +
                        "telefono = ?, " +
                        "salario = ?, " +
                        "numero_membresia_sindicato = ? " +
                        "WHERE ssn = ?";
            
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, nombreTxt.getText().trim());
            pstmt.setString(2, apellidoPaTxt.getText().trim());
            pstmt.setString(3, apellidoMaTxt.getText().trim().isEmpty() ? null : apellidoMaTxt.getText().trim());
            pstmt.setString(4, direccionTxt.getText().trim().isEmpty() ? null : direccionTxt.getText().trim());
            pstmt.setString(5, telefonoTxt.getText().trim().isEmpty() ? null : telefonoTxt.getText().trim());
            pstmt.setBigDecimal(6, new BigDecimal(salarioTxt.getText().trim()));
            pstmt.setString(7, membresiaTxt.getText().trim());
            pstmt.setString(8, ssnActual);
            
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this,
                    "✅ Empleado modificado exitosamente\n\n" +
                    "Los cambios han sido guardados en la base de datos.",
                    "Modificación exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
                
                cargarTodosEmpleados();
                seleccionarEnTabla(ssnActual);
                
                return true;
            } else {
                JOptionPane.showMessageDialog(this,
                    "No se pudo modificar el empleado.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                JOptionPane.showMessageDialog(this,
                    "❌ Error: El número de membresía ya existe.\n" +
                    "Cada empleado debe tener un número de membresía único.",
                    "Error de duplicado",
                    JOptionPane.ERROR_MESSAGE);
            } else if ("23514".equals(e.getSQLState())) {
                JOptionPane.showMessageDialog(this,
                    "❌ Error: El salario no puede ser negativo.",
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "❌ Error de base de datos: " + e.getMessage(),
                    "Error DB2",
                    JOptionPane.ERROR_MESSAGE);
            }
            return false;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "❌ Error: " + e.getMessage(),
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
    }
    
    private void mostrarErrorCampo(String campo) {
        JOptionPane.showMessageDialog(this,
            "El campo '" + campo + "' es obligatorio.",
            "Campo requerido",
            JOptionPane.WARNING_MESSAGE);
    }
    
    private void vaciarCampos() {
        ssnActual = null;
        ssnTxt.setText("");
        nombreTxt.setText("");
        apellidoPaTxt.setText("");
        apellidoMaTxt.setText("");
        direccionTxt.setText("");
        telefonoTxt.setText("");
        salarioTxt.setText("");
        membresiaTxt.setText("");
        
        habilitarCampos(false);
        modificarBtn.setEnabled(false);
        
        jTable1.clearSelection();
        
        if (sorter != null) {
            sorter.setRowFilter(null);
        }
        
        setTitle("Modificar Empleado");
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
        nombreTxt = new javax.swing.JTextField();
        apellidoPaTxt = new javax.swing.JTextField();
        apellidoMaTxt = new javax.swing.JTextField();
        direccionTxt = new javax.swing.JTextField();
        telefonoTxt = new javax.swing.JTextField();
        salarioTxt = new javax.swing.JTextField();
        membresiaTxt = new javax.swing.JTextField();
        modificarBtn = new javax.swing.JButton();
        vaciarBtn = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        ssnTxt = new javax.swing.JTextField();
        buscarBtn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(0, 0, 153));

        jLabel1.setFont(new java.awt.Font("Yu Gothic UI", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Modificar Empleado");

        nombreTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nombreTxtActionPerformed(evt);
            }
        });

        apellidoPaTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                apellidoPaTxtActionPerformed(evt);
            }
        });

        apellidoMaTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                apellidoMaTxtActionPerformed(evt);
            }
        });

        direccionTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                direccionTxtActionPerformed(evt);
            }
        });

        telefonoTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                telefonoTxtActionPerformed(evt);
            }
        });

        salarioTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                salarioTxtActionPerformed(evt);
            }
        });

        membresiaTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                membresiaTxtActionPerformed(evt);
            }
        });

        modificarBtn.setBackground(new java.awt.Color(102, 102, 102));
        modificarBtn.setForeground(new java.awt.Color(255, 255, 255));
        modificarBtn.setIcon(new javax.swing.ImageIcon("C:\\Users\\govan\\OneDrive\\Documentos\\1-Repositorio_Taller_BD\\Proyecto_Final_Taller_BD\\BD_Aeropuerto_Escritorio\\src\\main\\java\\Imagenes_Diseño\\icons8-ajustes-30.png")); // NOI18N
        modificarBtn.setText("Modificar");
        modificarBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modificarBtnActionPerformed(evt);
            }
        });

        vaciarBtn.setBackground(new java.awt.Color(102, 102, 102));
        vaciarBtn.setForeground(new java.awt.Color(255, 255, 255));
        vaciarBtn.setIcon(new javax.swing.ImageIcon("C:\\Users\\govan\\OneDrive\\Documentos\\1-Repositorio_Taller_BD\\Proyecto_Final_Taller_BD\\BD_Aeropuerto_Escritorio\\src\\main\\java\\Imagenes_Diseño\\icons8-limpiar-24.png")); // NOI18N
        vaciarBtn.setText("Vaciar");
        vaciarBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vaciarBtnActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Membresia");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Salario");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Telefono");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Dirección");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Apellido Materno");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Apellido Paterno");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Nombre");

        jButton3.setBackground(new java.awt.Color(102, 102, 102));
        jButton3.setIcon(new javax.swing.ImageIcon("C:\\Users\\govan\\OneDrive\\Documentos\\1-Repositorio_Taller_BD\\Proyecto_Final_Taller_BD\\BD_Aeropuerto_Escritorio\\src\\main\\java\\Imagenes_Diseño\\icons8-volver-24.png")); // NOI18N
        jButton3.setText("Regresar");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel9.setBackground(new java.awt.Color(255, 255, 255));
        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Ssn");

        ssnTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ssnTxtActionPerformed(evt);
            }
        });

        buscarBtn.setBackground(new java.awt.Color(102, 102, 102));
        buscarBtn.setForeground(new java.awt.Color(255, 255, 255));
        buscarBtn.setIcon(new javax.swing.ImageIcon("C:\\Users\\govan\\OneDrive\\Documentos\\1-Repositorio_Taller_BD\\Proyecto_Final_Taller_BD\\BD_Aeropuerto_Escritorio\\src\\main\\java\\Imagenes_Diseño\\icons8-buscar-24.png")); // NOI18N
        buscarBtn.setText("Buscar");
        buscarBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buscarBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(76, 76, 76)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2)
                            .addComponent(jLabel9))
                        .addGap(47, 47, 47)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(salarioTxt, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(telefonoTxt)
                            .addComponent(direccionTxt)
                            .addComponent(apellidoMaTxt, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(membresiaTxt)
                            .addComponent(apellidoPaTxt)
                            .addComponent(nombreTxt)
                            .addComponent(ssnTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(modificarBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buscarBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(vaciarBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(35, 35, 35))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jButton3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(46, 46, 46)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ssnTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(nombreTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(apellidoPaTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addGap(7, 7, 7)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(apellidoMaTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(direccionTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))
                        .addGap(7, 7, 7)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(telefonoTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(salarioTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(buscarBtn)
                        .addGap(35, 35, 35)
                        .addComponent(modificarBtn)
                        .addGap(35, 35, 35)
                        .addComponent(vaciarBtn)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(membresiaTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addGap(26, 26, 26)
                .addComponent(jButton3)
                .addContainerGap(15, Short.MAX_VALUE))
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
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
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
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    

    
    private void apellidoPaTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_apellidoPaTxtActionPerformed
        // TODO add your handling code here:
        apellidoMaTxt.requestFocus();
    }//GEN-LAST:event_apellidoPaTxtActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        
           MenuEmpleados menu = new MenuEmpleados();   
       
    menu.setVisible(true);               

    this.dispose();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void membresiaTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_membresiaTxtActionPerformed
        // TODO add your handling code here:
         modificarBtn.requestFocus();
    }//GEN-LAST:event_membresiaTxtActionPerformed

    private void ssnTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ssnTxtActionPerformed
        // TODO add your handling code here:
        buscarBtnActionPerformed(evt);
    }//GEN-LAST:event_ssnTxtActionPerformed

    private void nombreTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nombreTxtActionPerformed
        // TODO add your handling code here:
        apellidoPaTxt.requestFocus();
    }//GEN-LAST:event_nombreTxtActionPerformed

    private void apellidoMaTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_apellidoMaTxtActionPerformed
        // TODO add your handling code here:
         direccionTxt.requestFocus();
    }//GEN-LAST:event_apellidoMaTxtActionPerformed

    private void direccionTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_direccionTxtActionPerformed
        // TODO add your handling code here:
        telefonoTxt.requestFocus();
    }//GEN-LAST:event_direccionTxtActionPerformed

    private void telefonoTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_telefonoTxtActionPerformed
        // TODO add your handling code here:
         salarioTxt.requestFocus();
    }//GEN-LAST:event_telefonoTxtActionPerformed

    private void salarioTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_salarioTxtActionPerformed
        // TODO add your handling code here:
         membresiaTxt.requestFocus();
    }//GEN-LAST:event_salarioTxtActionPerformed

    private void buscarBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buscarBtnActionPerformed
        // TODO add your handling code here:
        String ssn = ssnTxt.getText().trim();
        if (!ssn.isEmpty()) {
            buscarEmpleadoPorSSN(ssn);
        } else {
            JOptionPane.showMessageDialog(this,
                "Ingrese un SSN para buscar.",
                "SSN requerido",
                JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_buscarBtnActionPerformed

    private void modificarBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modificarBtnActionPerformed
        // TODO add your handling code here:
        modificarEmpleado();
    }//GEN-LAST:event_modificarBtnActionPerformed

    private void vaciarBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vaciarBtnActionPerformed
        // TODO add your handling code here:
        vaciarCampos();
        cargarTodosEmpleados();
    }//GEN-LAST:event_vaciarBtnActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new ModificarEmpleado().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField apellidoMaTxt;
    private javax.swing.JTextField apellidoPaTxt;
    private javax.swing.JButton buscarBtn;
    private javax.swing.JTextField direccionTxt;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField membresiaTxt;
    private javax.swing.JButton modificarBtn;
    private javax.swing.JTextField nombreTxt;
    private javax.swing.JTextField salarioTxt;
    private javax.swing.JTextField ssnTxt;
    private javax.swing.JTextField telefonoTxt;
    private javax.swing.JButton vaciarBtn;
    // End of variables declaration//GEN-END:variables
}
