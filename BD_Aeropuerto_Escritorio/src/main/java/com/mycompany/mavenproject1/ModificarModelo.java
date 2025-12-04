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
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;
/**
 *
 * @author govan
 */
public class ModificarModelo extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ModificarModelo.class.getName());

    private javax.swing.Timer timerBusqueda;
    private String modeloSeleccionado = "";
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter;

    public ModificarModelo() {
        initComponents();
        configurarTabla();
        configurarBusquedaAutomatica();
        setLocationRelativeTo(null);
        cargarTodosModelos();
        
        capacidadTxt.setEnabled(false);
        pesoTxt.setEnabled(false);
        modificarBtn.setEnabled(false);
        
        jButton4.addActionListener(e -> {
            MenuModelos menu = new MenuModelos();
            menu.setVisible(true);
            this.dispose();
        });
    }

    private void configurarTabla() {
        modeloTabla = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        modeloTabla.addColumn("Número de Modelo");
        modeloTabla.addColumn("Capacidad");
        modeloTabla.addColumn("Peso");
        
        jTable1.setModel(modeloTabla);
        
        sorter = new TableRowSorter<>(modeloTabla);
        jTable1.setRowSorter(sorter);
        
        jTable1.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && jTable1.getSelectedRow() != -1) {
                cargarDatosDesdeTabla();
            }
        });
    }
    
    private void cargarDatosDesdeTabla() {
        int filaSeleccionada = jTable1.getSelectedRow();
        if (filaSeleccionada >= 0) {
            int modeloIndex = jTable1.convertRowIndexToModel(filaSeleccionada);
            
            modeloSeleccionado = modeloTabla.getValueAt(modeloIndex, 0).toString();
            numeroModeloTxt.setText(modeloSeleccionado);
            capacidadTxt.setText(modeloTabla.getValueAt(modeloIndex, 1).toString());
            pesoTxt.setText(modeloTabla.getValueAt(modeloIndex, 2).toString());
            
            capacidadTxt.setEnabled(true);
            pesoTxt.setEnabled(true);
            modificarBtn.setEnabled(true);
        }
    }

    private void configurarBusquedaAutomatica() {
        timerBusqueda = new javax.swing.Timer(300, e -> {
            buscarAutomaticamente();
        });
        timerBusqueda.setRepeats(false);
        
        numeroModeloTxt.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                iniciarBusquedaAutomatica();
                capacidadTxt.setEnabled(false);
                pesoTxt.setEnabled(false);
                modificarBtn.setEnabled(false);
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                iniciarBusquedaAutomatica();
                if (numeroModeloTxt.getText().trim().isEmpty()) {
                    cargarTodosModelos();
                }
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
        String textoBusqueda = numeroModeloTxt.getText().trim();
        
        if (textoBusqueda.isEmpty()) {
            cargarTodosModelos();
            return;
        }
        
        if (textoBusqueda.length() < 2) {
            return;
        }
        
        if (sorter != null) {
            RowFilter<DefaultTableModel, Integer> filtro = RowFilter.regexFilter("(?i)" + textoBusqueda, 0);
            sorter.setRowFilter(filtro);
        }
    }
    
    
    private void buscarModeloEnDB(String numeroModelo) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = Conexion_DB.getInstance().getConnection();
            
            if (conn == null) {
                mostrarErrorConexion();
                return;
            }
            
            String sql = "SELECT ModelNumber, Capacidad, Peso FROM ModelosAvion WHERE UPPER(ModelNumber) = UPPER(?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, numeroModelo);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                modeloSeleccionado = rs.getString("ModelNumber");
                numeroModeloTxt.setText(modeloSeleccionado);
                capacidadTxt.setText(String.valueOf(rs.getInt("Capacidad")));
                pesoTxt.setText(String.valueOf(rs.getDouble("Peso")));
                
                capacidadTxt.setEnabled(true);
                pesoTxt.setEnabled(true);
                modificarBtn.setEnabled(true);
                
                seleccionarModeloEnTabla(modeloSeleccionado);
                
                javax.swing.JOptionPane.showMessageDialog(this,
                    "✅ Modelo encontrado",
                    "Búsqueda exitosa",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            } else {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "❌ No se encontró el modelo: " + numeroModelo,
                    "Modelo no encontrado",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
                
                // Limpiar campos
                capacidadTxt.setText("");
                pesoTxt.setText("");
                capacidadTxt.setEnabled(false);
                pesoTxt.setEnabled(false);
                modificarBtn.setEnabled(false);
            }
            
        } catch (SQLException e) {
            manejarErrorSQL(e, "buscar");
        } finally {
            cerrarRecursos(rs, pstmt);
        }
    }

    private void cargarTodosModelos() {
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
            
            jTable1.clearSelection();
            modeloSeleccionado = "";
            capacidadTxt.setText("");
            pesoTxt.setText("");
            capacidadTxt.setEnabled(false);
            pesoTxt.setEnabled(false);
            modificarBtn.setEnabled(false);
            
            if (sorter != null) {
                sorter.setRowFilter(null);
            }
            
        } catch (SQLException e) {
            manejarErrorSQL(e, "cargar");
        } finally {
            cerrarRecursos(rs, pstmt);
        }
    }

    private void seleccionarModeloEnTabla(String numeroModelo) {
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            if (numeroModelo.equalsIgnoreCase(modeloTabla.getValueAt(i, 0).toString())) {
                int vistaIndex = jTable1.convertRowIndexToView(i);
                jTable1.setRowSelectionInterval(vistaIndex, vistaIndex);
                jTable1.scrollRectToVisible(jTable1.getCellRect(vistaIndex, 0, true));
                break;
            }
        }
    }
    
    private boolean validarCampos() {
        String capacidadText = capacidadTxt.getText().trim();
        String pesoText = pesoTxt.getText().trim();
        
        if (capacidadText.isEmpty() || pesoText.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "❌ Todos los campos son requeridos",
                "Error de validación",
                javax.swing.JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        try {
            int capacidad = Integer.parseInt(capacidadText);
            if (capacidad < 1 || capacidad > 1000) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "❌ Capacidad debe estar entre 1 y 1000",
                    "Error de validación",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
                capacidadTxt.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "❌ Capacidad debe ser un número entero válido",
                "Error de validación",
                javax.swing.JOptionPane.WARNING_MESSAGE);
            capacidadTxt.requestFocus();
            return false;
        }
        
        try {
            double peso = Double.parseDouble(pesoText);
            if (peso < 500 || peso > 1000000) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "❌ Peso debe estar entre 500 y 1,000,000",
                    "Error de validación",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
                pesoTxt.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "❌ Peso debe ser un número decimal válido",
                "Error de validación",
                javax.swing.JOptionPane.WARNING_MESSAGE);
            pesoTxt.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void modificarModeloDB() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = Conexion_DB.getInstance().getConnection();
            
            if (conn == null) {
                mostrarErrorConexion();
                return;
            }
            
            if (!verificarExistenciaModelo(modeloSeleccionado)) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "❌ El modelo '" + modeloSeleccionado + "' no existe",
                    "Modelo no encontrado",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String sql = "UPDATE ModelosAvion SET Capacidad = ?, Peso = ? WHERE UPPER(ModelNumber) = UPPER(?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, Integer.parseInt(capacidadTxt.getText().trim()));
            pstmt.setDouble(2, Double.parseDouble(pesoTxt.getText().trim()));
            pstmt.setString(3, modeloSeleccionado);
            
            int filasActualizadas = pstmt.executeUpdate();
            
            if (filasActualizadas > 0) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "✅ Modelo '" + modeloSeleccionado + "' modificado exitosamente",
                    "Modificación exitosa",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
                
                cargarTodosModelos();
                
                if (!numeroModeloTxt.getText().trim().isEmpty()) {
                    buscarAutomaticamente();
                    
                    seleccionarModeloEnTabla(modeloSeleccionado);
                }
            } else {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "❌ No se pudo modificar el modelo '" + modeloSeleccionado + "'",
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (SQLException e) {
            manejarErrorSQL(e, "modificar");
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
    
    private void vaciarCampos() {
        numeroModeloTxt.setText("");
        capacidadTxt.setText("");
        pesoTxt.setText("");
        modeloSeleccionado = "";
        capacidadTxt.setEnabled(false);
        pesoTxt.setEnabled(false);
        modificarBtn.setEnabled(false);
        
        cargarTodosModelos();
        
        if (timerBusqueda != null && timerBusqueda.isRunning()) {
            timerBusqueda.stop();
        }
    }
    
    private void mostrarErrorConexion() {
        javax.swing.JOptionPane.showMessageDialog(this,
            "❌ Error: No hay conexión a la base de datos",
            "Error de conexión",
            javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    
    private void manejarErrorSQL(SQLException e, String operacion) {
        String mensaje = "❌ Error al " + operacion + " modelo: ";
        
        if (e.getErrorCode() == -545) { 
            String sqlState = e.getSQLState();
            if ("23513".equals(sqlState)) {
                mensaje += "Los datos no cumplen con las reglas de validación:\n";
                
                String errorMsg = e.getMessage();
                if (errorMsg.contains("CHK_Capacidad_Valida")) {
                    mensaje += "- Capacidad fuera de rango (1-1000)";
                } else if (errorMsg.contains("CHK_Peso_Valido")) {
                    mensaje += "- Peso fuera de rango (500-1,000,000)";
                } else {
                    mensaje += errorMsg;
                }
            } else {
                mensaje += "Error de validación: " + e.getMessage();
            }
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
    
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        numeroModeloTxt = new javax.swing.JTextField();
        capacidadTxt = new javax.swing.JTextField();
        pesoTxt = new javax.swing.JTextField();
        buscarBtn = new javax.swing.JButton();
        modificarBtn = new javax.swing.JButton();
        vaciarBtn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton4 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(0, 0, 204));

        jLabel1.setBackground(new java.awt.Color(0, 0, 0));
        jLabel1.setFont(new java.awt.Font("Yu Gothic UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Modificar modelo");

        jLabel2.setFont(new java.awt.Font("Yu Gothic UI", 1, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Numero de modelo");

        jLabel3.setFont(new java.awt.Font("Yu Gothic UI", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Capacidad");

        jLabel4.setFont(new java.awt.Font("Yu Gothic", 1, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Peso");

        numeroModeloTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numeroModeloTxtActionPerformed(evt);
            }
        });

        capacidadTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                capacidadTxtActionPerformed(evt);
            }
        });

        pesoTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pesoTxtActionPerformed(evt);
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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(72, 72, 72)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addGap(87, 87, 87))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(19, 19, 19)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(numeroModeloTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                    .addComponent(capacidadTxt, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pesoTxt, javax.swing.GroupLayout.Alignment.LEADING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(modificarBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(vaciarBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(buscarBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(57, 57, 57))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(262, 262, 262)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1)
                        .addGap(39, 39, 39)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(numeroModeloTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(69, 69, 69)
                        .addComponent(buscarBtn)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(capacidadTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modificarBtn))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(pesoTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(vaciarBtn)))
                .addContainerGap(45, Short.MAX_VALUE))
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

        jButton4.setIcon(new javax.swing.ImageIcon("C:\\Users\\govan\\OneDrive\\Documentos\\1-Repositorio_Taller_BD\\Proyecto_Final_Taller_BD\\BD_Aeropuerto_Escritorio\\src\\main\\java\\Imagenes_Diseño\\icons8-volver-24.png")); // NOI18N
        jButton4.setText("Regresar");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 756, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton4)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton4)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

    private void buscarBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buscarBtnActionPerformed
        // TODO add your handling code here:
        String numeroModelo = numeroModeloTxt.getText().trim();
        
        if (numeroModelo.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "❌ Por favor ingrese un número de modelo",
                "Campo vacío",
                javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        buscarModeloEnDB(numeroModelo);
    }//GEN-LAST:event_buscarBtnActionPerformed

    private void numeroModeloTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numeroModeloTxtActionPerformed
        // TODO add your handling code here:
        buscarBtn.doClick();
    }//GEN-LAST:event_numeroModeloTxtActionPerformed

    private void capacidadTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_capacidadTxtActionPerformed
        // TODO add your handling code here:
        pesoTxt.requestFocus();
    }//GEN-LAST:event_capacidadTxtActionPerformed

    private void pesoTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pesoTxtActionPerformed
        // TODO add your handling code here:
        modificarBtn.requestFocus();
    }//GEN-LAST:event_pesoTxtActionPerformed

    private void modificarBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modificarBtnActionPerformed
        // TODO add your handling code here:
        if (modeloSeleccionado.isEmpty()) {
            modeloSeleccionado = numeroModeloTxt.getText().trim();
            
            if (modeloSeleccionado.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "❌ Por favor seleccione o busque un modelo para modificar",
                    "Sin selección",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        
     
        if (!validarCampos()) {
            return;
        }
        
        int confirmacion = javax.swing.JOptionPane.showConfirmDialog(this,
            "¿Está seguro de modificar el modelo: " + modeloSeleccionado + "?\n\n" +
            "Nuevos valores:\n" +
            "Capacidad: " + capacidadTxt.getText() + "\n" +
            "Peso: " + pesoTxt.getText(),
            "Confirmar modificación",
            javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.WARNING_MESSAGE);
        
        if (confirmacion == javax.swing.JOptionPane.YES_OPTION) {
            modificarModeloDB();
        }
        
    }//GEN-LAST:event_modificarBtnActionPerformed

    private void vaciarBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vaciarBtnActionPerformed
        // TODO add your handling code here:
        vaciarCampos();
        
    }//GEN-LAST:event_vaciarBtnActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new ModificarModelo().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buscarBtn;
    private javax.swing.JTextField capacidadTxt;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JButton modificarBtn;
    private javax.swing.JTextField numeroModeloTxt;
    private javax.swing.JTextField pesoTxt;
    private javax.swing.JButton vaciarBtn;
    // End of variables declaration//GEN-END:variables

}
