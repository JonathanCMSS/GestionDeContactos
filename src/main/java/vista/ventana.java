package vista;

import java.awt.EventQueue;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import controlador.logica_ventana;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class ventana extends JFrame {

    public JPanel contentPane;
    public JTextField txt_nombres;
    public JTextField txt_telefono;
    public JTextField txt_email;
    public JTextField txt_buscar;
    public JCheckBox chb_favorito;
    public JComboBox<String> cmb_categoria;
    public JButton btn_add;
    public JButton btn_modificar;
    public JButton btn_eliminar;
    public JButton btn_exportar;
    public JTable tbl_contactos;
    public JProgressBar progressBar;
    public JTabbedPane tabbedPane;
    public JLabel lbl_totalContactos;
    public JLabel lbl_totalFavoritos;
    public JLabel lbl_totalFamilia;
    public JLabel lbl_totalAmigos;
    public JLabel lbl_totalTrabajo;
    
    // Popup menu para clic derecho
    public JPopupMenu popupMenu;
    public JMenuItem menuEditar;
    public JMenuItem menuEliminar;
    public JMenuItem menuMarcarFavorito;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ventana frame = new ventana();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public ventana() {
        setTitle("GESTIÓN DE CONTACTOS - Sistema Optimizado");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setBounds(100, 100, 1200, 800);
        
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout());
        
        // Crear JTabbedPane
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        contentPane.add(tabbedPane, BorderLayout.CENTER);
        
        // PESTAÑA 1: GESTIÓN DE CONTACTOS
        JPanel panelContactos = crearPanelContactos();
        tabbedPane.addTab("Gestión de Contactos", null, panelContactos, "Administrar contactos");
        
        // PESTAÑA 2: ESTADÍSTICAS
        JPanel panelEstadisticas = crearPanelEstadisticas();
        tabbedPane.addTab("Estadísticas", null, panelEstadisticas, "Ver estadísticas de contactos");
        
        // Barra de progreso en la parte inferior
        JPanel panelInferior = new JPanel(new BorderLayout());
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Listo");
        panelInferior.add(progressBar, BorderLayout.CENTER);
        contentPane.add(panelInferior, BorderLayout.SOUTH);
        
        // Crear menú contextual
        crearMenuContextual();
        
        // Instanciar el controlador
        logica_ventana lv = new logica_ventana(this);
    }
    
    private JPanel crearPanelContactos() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        
        // Etiquetas
        JLabel lbl_nombres = new JLabel("NOMBRES:");
        lbl_nombres.setBounds(25, 20, 100, 25);
        lbl_nombres.setFont(new Font("Tahoma", Font.BOLD, 14));
        panel.add(lbl_nombres);
        
        JLabel lbl_telefono = new JLabel("TELÉFONO:");
        lbl_telefono.setBounds(25, 55, 100, 25);
        lbl_telefono.setFont(new Font("Tahoma", Font.BOLD, 14));
        panel.add(lbl_telefono);
        
        JLabel lbl_email = new JLabel("EMAIL:");
        lbl_email.setBounds(25, 90, 100, 25);
        lbl_email.setFont(new Font("Tahoma", Font.BOLD, 14));
        panel.add(lbl_email);
        
        // Campos de texto
        txt_nombres = new JTextField();
        txt_nombres.setBounds(135, 20, 350, 25);
        txt_nombres.setFont(new Font("Tahoma", Font.PLAIN, 13));
        txt_nombres.setToolTipText("Ingrese el nombre completo (Ctrl+N para enfocar)");
        panel.add(txt_nombres);
        
        txt_telefono = new JTextField();
        txt_telefono.setBounds(135, 55, 350, 25);
        txt_telefono.setFont(new Font("Tahoma", Font.PLAIN, 13));
        txt_telefono.setToolTipText("Ingrese el número de teléfono");
        panel.add(txt_telefono);
        
        txt_email = new JTextField();
        txt_email.setBounds(135, 90, 350, 25);
        txt_email.setFont(new Font("Tahoma", Font.PLAIN, 13));
        txt_email.setToolTipText("Ingrese el correo electrónico");
        panel.add(txt_email);
        
        // CheckBox y ComboBox
        chb_favorito = new JCheckBox("CONTACTO FAVORITO");
        chb_favorito.setBounds(25, 130, 180, 25);
        chb_favorito.setFont(new Font("Tahoma", Font.PLAIN, 13));
        panel.add(chb_favorito);
        
        cmb_categoria = new JComboBox<>();
        cmb_categoria.setBounds(220, 130, 265, 25);
        String[] categorias = {"Elija una Categoria", "Familia", "Amigos", "Trabajo"};
        for (String categoria : categorias) {
            cmb_categoria.addItem(categoria);
        }
        panel.add(cmb_categoria);
        
        // Botones
        btn_add = new JButton("AGREGAR");
        btn_add.setFont(new Font("Tahoma", Font.BOLD, 12));
        btn_add.setBounds(520, 20, 130, 40);
        btn_add.setToolTipText("Agregar nuevo contacto (Ctrl+A)");
        panel.add(btn_add);
        
        btn_modificar = new JButton("MODIFICAR");
        btn_modificar.setFont(new Font("Tahoma", Font.BOLD, 12));
        btn_modificar.setBounds(660, 20, 130, 40);
        btn_modificar.setToolTipText("Modificar contacto seleccionado (Ctrl+M)");
        panel.add(btn_modificar);
        
        btn_eliminar = new JButton("ELIMINAR");
        btn_eliminar.setFont(new Font("Tahoma", Font.BOLD, 12));
        btn_eliminar.setBounds(800, 20, 130, 40);
        btn_eliminar.setToolTipText("Eliminar contacto seleccionado (Delete)");
        panel.add(btn_eliminar);
        
        btn_exportar = new JButton("EXPORTAR CSV");
        btn_exportar.setFont(new Font("Tahoma", Font.BOLD, 12));
        btn_exportar.setBounds(940, 20, 150, 40);
        btn_exportar.setToolTipText("Exportar contactos a CSV (Ctrl+E)");
        panel.add(btn_exportar);
        
        // Campo de búsqueda
        JLabel lbl_buscar = new JLabel("BUSCAR:");
        lbl_buscar.setBounds(25, 170, 80, 25);
        lbl_buscar.setFont(new Font("Tahoma", Font.BOLD, 14));
        panel.add(lbl_buscar);
        
        txt_buscar = new JTextField();
        txt_buscar.setBounds(110, 170, 1020, 25);
        txt_buscar.setFont(new Font("Tahoma", Font.PLAIN, 13));
        txt_buscar.setToolTipText("Buscar contactos en tiempo real (Ctrl+F)");
        panel.add(txt_buscar);
        
        // Tabla de contactos
        String[] columnas = {"Nombre", "Teléfono", "Email", "Categoría", "Favorito"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer la tabla no editable directamente
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) {
                    return Boolean.class; // Columna favorito como checkbox
                }
                return String.class;
            }
        };
        
        tbl_contactos = new JTable(modelo);
        tbl_contactos.setFont(new Font("Tahoma", Font.PLAIN, 12));
        tbl_contactos.setRowHeight(25);
        tbl_contactos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl_contactos.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 13));
        
        // Ajustar ancho de columnas
        tbl_contactos.getColumnModel().getColumn(0).setPreferredWidth(200);
        tbl_contactos.getColumnModel().getColumn(1).setPreferredWidth(120);
        tbl_contactos.getColumnModel().getColumn(2).setPreferredWidth(200);
        tbl_contactos.getColumnModel().getColumn(3).setPreferredWidth(100);
        tbl_contactos.getColumnModel().getColumn(4).setPreferredWidth(80);
        
        JScrollPane scrollPane = new JScrollPane(tbl_contactos);
        scrollPane.setBounds(25, 210, 1105, 440);
        panel.add(scrollPane);
        
        return panel;
    }
    
    private JPanel crearPanelEstadisticas() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        
        // Título
        JLabel titulo = new JLabel("ESTADÍSTICAS DE CONTACTOS");
        titulo.setBounds(350, 20, 400, 40);
        titulo.setFont(new Font("Tahoma", Font.BOLD, 24));
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titulo);
        
        // Panel de estadísticas generales
        JPanel panelStats = new JPanel();
        panelStats.setBounds(150, 100, 800, 400);
        panelStats.setLayout(null);
        panelStats.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Resumen General",
            0, 0, new Font("Tahoma", Font.BOLD, 16)));
        
        // Total de contactos
        JLabel lblTotalText = new JLabel("Total de Contactos:");
        lblTotalText.setBounds(50, 50, 250, 30);
        lblTotalText.setFont(new Font("Tahoma", Font.BOLD, 16));
        panelStats.add(lblTotalText);
        
        lbl_totalContactos = new JLabel("0");
        lbl_totalContactos.setBounds(350, 50, 150, 30);
        lbl_totalContactos.setFont(new Font("Tahoma", Font.PLAIN, 16));
        panelStats.add(lbl_totalContactos);
        
        // Total de favoritos
        JLabel lblFavText = new JLabel("Contactos Favoritos:");
        lblFavText.setBounds(50, 100, 250, 30);
        lblFavText.setFont(new Font("Tahoma", Font.BOLD, 16));
        panelStats.add(lblFavText);
        
        lbl_totalFavoritos = new JLabel("0");
        lbl_totalFavoritos.setBounds(350, 100, 150, 30);
        lbl_totalFavoritos.setFont(new Font("Tahoma", Font.PLAIN, 16));
        panelStats.add(lbl_totalFavoritos);
        
        // Separador
        JSeparator separator = new JSeparator();
        separator.setBounds(50, 150, 700, 2);
        panelStats.add(separator);
        
        // Por categorías
        JLabel lblCatText = new JLabel("DISTRIBUCIÓN POR CATEGORÍAS:");
        lblCatText.setBounds(50, 170, 350, 30);
        lblCatText.setFont(new Font("Tahoma", Font.BOLD, 16));
        panelStats.add(lblCatText);
        
        JLabel lblFamiliaText = new JLabel("Familia:");
        lblFamiliaText.setBounds(100, 220, 200, 25);
        lblFamiliaText.setFont(new Font("Tahoma", Font.BOLD, 14));
        panelStats.add(lblFamiliaText);
        
        lbl_totalFamilia = new JLabel("0");
        lbl_totalFamilia.setBounds(350, 220, 150, 25);
        lbl_totalFamilia.setFont(new Font("Tahoma", Font.PLAIN, 14));
        panelStats.add(lbl_totalFamilia);
        
        JLabel lblAmigosText = new JLabel("Amigos:");
        lblAmigosText.setBounds(100, 260, 200, 25);
        lblAmigosText.setFont(new Font("Tahoma", Font.BOLD, 14));
        panelStats.add(lblAmigosText);
        
        lbl_totalAmigos = new JLabel("0");
        lbl_totalAmigos.setBounds(350, 260, 150, 25);
        lbl_totalAmigos.setFont(new Font("Tahoma", Font.PLAIN, 14));
        panelStats.add(lbl_totalAmigos);
        
        JLabel lblTrabajoText = new JLabel("Trabajo:");
        lblTrabajoText.setBounds(100, 300, 200, 25);
        lblTrabajoText.setFont(new Font("Tahoma", Font.BOLD, 14));
        panelStats.add(lblTrabajoText);
        
        lbl_totalTrabajo = new JLabel("0");
        lbl_totalTrabajo.setBounds(350, 300, 150, 25);
        lbl_totalTrabajo.setFont(new Font("Tahoma", Font.PLAIN, 14));
        panelStats.add(lbl_totalTrabajo);
        
        panel.add(panelStats);
        
        return panel;
    }
    
    private void crearMenuContextual() {
        popupMenu = new JPopupMenu();
        
        menuEditar = new JMenuItem("Editar Contacto");
        menuEditar.setFont(new Font("Tahoma", Font.PLAIN, 12));
        popupMenu.add(menuEditar);
        
        menuEliminar = new JMenuItem("Eliminar Contacto");
        menuEliminar.setFont(new Font("Tahoma", Font.PLAIN, 12));
        popupMenu.add(menuEliminar);
        
        popupMenu.addSeparator();
        
        menuMarcarFavorito = new JMenuItem("Marcar/Desmarcar Favorito");
        menuMarcarFavorito.setFont(new Font("Tahoma", Font.PLAIN, 12));
        popupMenu.add(menuMarcarFavorito);
    }
}