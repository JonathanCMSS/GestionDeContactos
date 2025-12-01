package vista;

import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import controlador.logica_ventana;

// ✨ UNIDAD 4: Imports para FlatLaf
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;

/**
 * Ventana principal - ACTUALIZADA UNIDAD 4
 * Mejoras: FlatLaf + Botones JSON
 */
public class ventana extends JFrame {
    
    private ResourceBundle messages;
    private Locale currentLocale;
    
    // Colores
    private static final Color PRIMARY_COLOR = new Color(0, 0, 0);
    private static final Color SECONDARY_COLOR = new Color(0, 255, 255);
    private static final Color ACCENT_COLOR = new Color(0, 200, 200);
    private static final Color BACKGROUND_COLOR = new Color(20, 20, 20);
    private static final Color PANEL_COLOR = new Color(30, 30, 30);
    private static final Color TEXT_COLOR = new Color(0, 255, 255);
    private static final Color TEXT_WHITE = Color.WHITE;
    
    // Componentes públicos
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
    
    // ✨ NUEVO UNIDAD 4
    public JButton btn_exportarJSON;
    public JButton btn_importarJSON;
    
    public JTable tbl_contactos;
    public JProgressBar progressBar;
    public JTabbedPane tabbedPane;
    public JLabel lbl_totalContactos;
    public JLabel lbl_totalFavoritos;
    public JLabel lbl_totalFamilia;
    public JLabel lbl_totalAmigos;
    public JLabel lbl_totalTrabajo;
    
    // Popup menu
    public JPopupMenu popupMenu;
    public JMenuItem menuEditar;
    public JMenuItem menuEliminar;
    public JMenuItem menuMarcarFavorito;
    
    // Menús
    private JMenuBar menuBar;
    private JMenu menuIdioma;
    
    // Labels
    private JLabel lbl_nombres;
    private JLabel lbl_telefono;
    private JLabel lbl_email;
    private JLabel lbl_buscar;
    private JLabel lbl_titulo_estadisticas;
    private javax.swing.border.TitledBorder borderFormulario;
    private JLabel lbl_titulo_total;
    private JLabel lbl_titulo_favoritos;
    private JLabel lbl_titulo_familia;
    private JLabel lbl_titulo_amigos;
    private JLabel lbl_titulo_trabajo;

    /**
     * ✨ MAIN ACTUALIZADO CON FLATLAF
     */
    public static void main(String[] args) {
        try {
            // ✨ UNIDAD 4: Aplicar FlatLaf
            FlatDarkLaf.setup();
            
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ventana frame = new ventana();
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public ventana() {
        currentLocale = new Locale("es", "ES");
        messages = ResourceBundle.getBundle("recursos.messages", currentLocale);
        
        initComponents();
        aplicarDisenoVisual();
        crearMenuIdioma();
        
        new logica_ventana(this);
    }
    
    private void initComponents() {
        setTitle(messages.getString("app.title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setBounds(100, 100, 1200, 800);
        setMinimumSize(new Dimension(1000, 700));
        
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPane.setBackground(BACKGROUND_COLOR);
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(10, 10));
        
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        contentPane.add(tabbedPane, BorderLayout.CENTER);
        
        JPanel panelContactos = crearPanelContactos();
        tabbedPane.addTab(messages.getString("tab.contacts"), panelContactos);
        
        JPanel panelEstadisticas = crearPanelEstadisticas();
        tabbedPane.addTab(messages.getString("tab.statistics"), panelEstadisticas);
        
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(BACKGROUND_COLOR);
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString(messages.getString("app.ready"));
        progressBar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        progressBar.setPreferredSize(new Dimension(0, 30));
        panelInferior.add(progressBar, BorderLayout.CENTER);
        contentPane.add(panelInferior, BorderLayout.SOUTH);
        
        crearMenuContextual();
    }
    
    private void crearMenuIdioma() {
        menuBar = new JMenuBar();
        menuBar.setBackground(PRIMARY_COLOR);
        
        menuIdioma = new JMenu("🌍 " + getLanguageName(currentLocale));
        menuIdioma.setForeground(SECONDARY_COLOR);
        menuIdioma.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        JMenuItem itemEspanol = new JMenuItem("🇪🇸 Español");
        itemEspanol.addActionListener(e -> cambiarIdioma(new Locale("es", "ES")));
        
        JMenuItem itemIngles = new JMenuItem("🇬🇧 English");
        itemIngles.addActionListener(e -> cambiarIdioma(new Locale("en", "US")));
        
        JMenuItem itemFrances = new JMenuItem("🇫🇷 Français");
        itemFrances.addActionListener(e -> cambiarIdioma(new Locale("fr", "FR")));
        
        menuIdioma.add(itemEspanol);
        menuIdioma.add(itemIngles);
        menuIdioma.add(itemFrances);
        
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(menuIdioma);
        
        setJMenuBar(menuBar);
    }
    
    private String getLanguageName(Locale locale) {
        if (locale.getLanguage().equals("es")) return "Español";
        if (locale.getLanguage().equals("en")) return "English";
        if (locale.getLanguage().equals("fr")) return "Français";
        return locale.getDisplayLanguage();
    }
    
    public void cambiarIdioma(Locale nuevoLocale) {
        currentLocale = nuevoLocale;
        messages = ResourceBundle.getBundle("recursos.messages", currentLocale);
        actualizarTextos();
        menuIdioma.setText("🌍 " + getLanguageName(currentLocale));
    }
    
    private void actualizarTextos() {
        setTitle(messages.getString("app.title"));
        tabbedPane.setTitleAt(0, messages.getString("tab.contacts"));
        tabbedPane.setTitleAt(1, messages.getString("tab.statistics"));
        
        lbl_nombres.setText(messages.getString("form.name"));
        lbl_telefono.setText(messages.getString("form.phone"));
        lbl_email.setText(messages.getString("form.email"));
        lbl_buscar.setText(messages.getString("form.search"));
        
        btn_add.setText("➕ " + messages.getString("btn.add"));
        btn_modificar.setText("✏️ " + messages.getString("btn.modify"));
        btn_eliminar.setText("🗑️ " + messages.getString("btn.delete"));
        btn_exportar.setText("📊 " + messages.getString("btn.export"));
        
        DefaultTableModel modelo = (DefaultTableModel) tbl_contactos.getModel();
        modelo.setColumnIdentifiers(new String[]{
            messages.getString("table.name"),
            messages.getString("table.phone"),
            messages.getString("table.email"),
            messages.getString("table.category"),
            messages.getString("table.favorite")
        });
        
        contentPane.revalidate();
        contentPane.repaint();
    }
    
    private JPanel crearPanelContactos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JPanel panelSuperior = new JPanel();
        panelSuperior.setLayout(new BoxLayout(panelSuperior, BoxLayout.Y_AXIS));
        panelSuperior.setBackground(PANEL_COLOR);
        
        panelSuperior.add(crearPanelFormulario());
        panelSuperior.add(Box.createRigidArea(new Dimension(0, 10)));
        panelSuperior.add(crearPanelBotones());
        panelSuperior.add(Box.createRigidArea(new Dimension(0, 10)));
        panelSuperior.add(crearPanelBusqueda());
        
        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(crearTablaContactos(), BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL_COLOR);
        borderFormulario = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(SECONDARY_COLOR, 2),
            messages.getString("tab.contacts"),
            0, 0, new Font("Segoe UI", Font.BOLD, 14), SECONDARY_COLOR);
        panel.setBorder(borderFormulario);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        
        // Nombres
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        lbl_nombres = new JLabel(messages.getString("form.name"));
        lbl_nombres.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl_nombres.setForeground(TEXT_COLOR);
        panel.add(lbl_nombres, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txt_nombres = new JTextField();
        estilizarTextField(txt_nombres);
        panel.add(txt_nombres, gbc);
        
        // Teléfono
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        lbl_telefono = new JLabel(messages.getString("form.phone"));
        lbl_telefono.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl_telefono.setForeground(TEXT_COLOR);
        panel.add(lbl_telefono, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txt_telefono = new JTextField();
        estilizarTextField(txt_telefono);
        panel.add(txt_telefono, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        lbl_email = new JLabel(messages.getString("form.email"));
        lbl_email.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl_email.setForeground(TEXT_COLOR);
        panel.add(lbl_email, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txt_email = new JTextField();
        estilizarTextField(txt_email);
        panel.add(txt_email, gbc);
        
        // Opciones
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JPanel panelOpciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelOpciones.setBackground(PANEL_COLOR);
        
        chb_favorito = new JCheckBox(messages.getString("form.favorite"));
        chb_favorito.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chb_favorito.setBackground(PANEL_COLOR);
        chb_favorito.setForeground(TEXT_COLOR);
        panelOpciones.add(chb_favorito);
        
        cmb_categoria = new JComboBox<>();
        cmb_categoria.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmb_categoria.setPreferredSize(new Dimension(200, 35));
        cmb_categoria.addItem(messages.getString("category.select"));
        cmb_categoria.addItem(messages.getString("category.family"));
        cmb_categoria.addItem(messages.getString("category.friends"));
        cmb_categoria.addItem(messages.getString("category.work"));
        panelOpciones.add(cmb_categoria);
        
        panel.add(panelOpciones, gbc);
        
        return panel;
    }
    
    /**
     * ✨ UNIDAD 4: PANEL ACTUALIZADO CON BOTONES JSON
     */
    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBackground(PANEL_COLOR);
        
        // Botones originales
        btn_add = crearBoton(messages.getString("btn.add"), SECONDARY_COLOR, "➕");
        btn_modificar = crearBoton(messages.getString("btn.modify"), ACCENT_COLOR, "✏️");
        btn_eliminar = crearBoton(messages.getString("btn.delete"), new Color(100, 100, 100), "🗑️");
        btn_exportar = crearBoton(messages.getString("btn.export"), PRIMARY_COLOR, "📊");
        
        // ✨ NUEVOS BOTONES JSON
        btn_exportarJSON = crearBoton("EXPORTAR JSON", new Color(0, 150, 136), "💾");
        btn_exportarJSON.setToolTipText("Exportar contactos a formato JSON");
        btn_exportarJSON.setPreferredSize(new Dimension(180, 45));
        
        btn_importarJSON = crearBoton("IMPORTAR JSON", new Color(103, 58, 183), "📥");
        btn_importarJSON.setToolTipText("Importar contactos desde archivo JSON");
        btn_importarJSON.setPreferredSize(new Dimension(180, 45));
        
        panel.add(btn_add);
        panel.add(btn_modificar);
        panel.add(btn_eliminar);
        panel.add(btn_exportar);
        panel.add(btn_exportarJSON);    // ✨ NUEVO
        panel.add(btn_importarJSON);    // ✨ NUEVO
        
        return panel;
    }
    
    private JPanel crearPanelBusqueda() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(PANEL_COLOR);
        
        lbl_buscar = new JLabel(messages.getString("form.search"));
        lbl_buscar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl_buscar.setForeground(TEXT_COLOR);
        panel.add(lbl_buscar, BorderLayout.WEST);
        
        txt_buscar = new JTextField();
        estilizarTextField(txt_buscar);
        panel.add(txt_buscar, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JScrollPane crearTablaContactos() {
        String[] columnas = {
            messages.getString("table.name"),
            messages.getString("table.phone"),
            messages.getString("table.email"),
            messages.getString("table.category"),
            messages.getString("table.favorite")
        };
        
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) return Boolean.class;
                return String.class;
            }
        };
        
        tbl_contactos = new JTable(modelo);
        tbl_contactos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl_contactos.setRowHeight(30);
        tbl_contactos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(tbl_contactos);
        scrollPane.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR, 1));
        
        return scrollPane;
    }
    
    private JPanel crearPanelEstadisticas() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        lbl_titulo_estadisticas = new JLabel(messages.getString("stats.title"));
        lbl_titulo_estadisticas.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lbl_titulo_estadisticas.setForeground(SECONDARY_COLOR);
        lbl_titulo_estadisticas.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lbl_titulo_estadisticas, BorderLayout.NORTH);
        
        JPanel panelStats = new JPanel(new GridLayout(3, 2, 20, 20));
        panelStats.setBackground(PANEL_COLOR);
        panelStats.setBorder(new EmptyBorder(30, 50, 30, 50));
        
        panelStats.add(crearTarjetaEstadistica("Total", "0", SECONDARY_COLOR, "total"));
        panelStats.add(crearTarjetaEstadistica("Favoritos", "0", ACCENT_COLOR, "favoritos"));
        panelStats.add(crearTarjetaEstadistica("Familia", "0", new Color(0, 180, 180), "familia"));
        panelStats.add(crearTarjetaEstadistica("Amigos", "0", new Color(0, 150, 150), "amigos"));
        panelStats.add(crearTarjetaEstadistica("Trabajo", "0", new Color(0, 120, 120), "trabajo"));
        
        panel.add(panelStats, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel crearTarjetaEstadistica(String titulo, String valor, Color color, String tipo) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(color);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.brighter(), 2),
            new EmptyBorder(20, 20, 20, 20)));
        
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(PRIMARY_COLOR);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblTitulo, BorderLayout.NORTH);
        
        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 48));
        lblValor.setForeground(PRIMARY_COLOR);
        lblValor.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblValor, BorderLayout.CENTER);
        
        switch(tipo) {
            case "total":
                lbl_totalContactos = lblValor;
                lbl_titulo_total = lblTitulo;
                break;
            case "favoritos":
                lbl_totalFavoritos = lblValor;
                lbl_titulo_favoritos = lblTitulo;
                break;
            case "familia":
                lbl_totalFamilia = lblValor;
                lbl_titulo_familia = lblTitulo;
                break;
            case "amigos":
                lbl_totalAmigos = lblValor;
                lbl_titulo_amigos = lblTitulo;
                break;
            case "trabajo":
                lbl_totalTrabajo = lblValor;
                lbl_titulo_trabajo = lblTitulo;
                break;
        }
        
        return panel;
    }
    
    private void crearMenuContextual() {
        popupMenu = new JPopupMenu();
        
        menuEditar = new JMenuItem(messages.getString("menu.edit"));
        menuEliminar = new JMenuItem(messages.getString("menu.delete"));
        menuMarcarFavorito = new JMenuItem(messages.getString("menu.favorite"));
        
        popupMenu.add(menuEditar);
        popupMenu.add(menuEliminar);
        popupMenu.addSeparator();
        popupMenu.add(menuMarcarFavorito);
    }
    
    private JButton crearBoton(String texto, Color color, String emoji) {
        JButton boton = new JButton(emoji + " " + texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setBackground(color);
        boton.setForeground(PRIMARY_COLOR);
        boton.setFocusPainted(false);
        boton.setBorderPainted(true);
        boton.setPreferredSize(new Dimension(160, 45));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(color);
            }
        });
        
        return boton;
    }
    
    private void estilizarTextField(JTextField textField) {
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textField.setPreferredSize(new Dimension(0, 35));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SECONDARY_COLOR, 2),
            new EmptyBorder(5, 10, 5, 10)));
    }
    
    private void aplicarDisenoVisual() {
        // Configuraciones adicionales si es necesario
    }
    
    public ResourceBundle getMessages() {
        return messages;
    }
    
    public Locale getCurrentLocale() {
        return currentLocale;
    }
}