package controlador;

import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import vista.ventana;
import modelo.*;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Controlador principal con implementación de programación concurrente
 * Mejoras de Unidad 3:
 * - Validación de contactos en segundo plano
 * - Búsqueda concurrente sin bloquear UI
 * - Exportación con hilos múltiples
 * - Notificaciones en tiempo real
 * - Sincronización para evitar corrupción de datos
 */
public class logica_ventana implements ActionListener, ListSelectionListener, 
                                       ItemListener, KeyListener, MouseListener, 
                                       ChangeListener {
    
    private JsonContactHandler jsonHandler;
    private ventana delegado;
    private String nombres, email, telefono, categoria = "";
    private persona persona;
    private List<persona> contactos;
    private boolean favorito = false;
    private TableRowSorter<DefaultTableModel> sorter;
    private int filaSeleccionada = -1;
    private ResourceBundle messages;
    
    // MEJORA UNIDAD 3: ExecutorService para gestión de hilos
    private ExecutorService executorService;
    private ScheduledExecutorService notificationExecutor;
    
    // MEJORA UNIDAD 3: Lock para sincronización de operaciones críticas
    private final ReentrantLock contactosLock = new ReentrantLock();
    private final ReentrantLock exportacionLock = new ReentrantLock();
    
    // MEJORA UNIDAD 3: Thread para búsqueda en segundo plano
    private Timer busquedaTimer;

    public logica_ventana(ventana delegado) {
        this.delegado = delegado;
        this.messages = delegado.getMessages();
        this.jsonHandler = new JsonContactHandler();
        
        // MEJORA UNIDAD 3: Inicializar pool de hilos
        executorService = Executors.newFixedThreadPool(4);
        notificationExecutor = Executors.newScheduledThreadPool(2);
        
        cargarContactosRegistrados();
        registrarEventos();
        configurarAtajosTeclado();
        actualizarEstadisticas();
        
        // MEJORA UNIDAD 3: Configurar timer para búsqueda concurrente
        configurarBusquedaConcurrente();
    }

    private void registrarEventos() {
        delegado.btn_exportarJSON.addActionListener(this);
        delegado.btn_importarJSON.addActionListener(this);
        delegado.btn_add.addActionListener(this);
        delegado.btn_eliminar.addActionListener(this);
        delegado.btn_modificar.addActionListener(this);
        delegado.btn_exportar.addActionListener(this);
        delegado.tbl_contactos.getSelectionModel().addListSelectionListener(this);
        delegado.cmb_categoria.addItemListener(this);
        delegado.chb_favorito.addItemListener(this);
        delegado.txt_buscar.addKeyListener(this);
        delegado.txt_nombres.addKeyListener(this);
        delegado.txt_telefono.addKeyListener(this);
        delegado.txt_email.addKeyListener(this);
        delegado.tbl_contactos.addMouseListener(this);
        delegado.menuEditar.addActionListener(this);
        delegado.menuEliminar.addActionListener(this);
        delegado.menuMarcarFavorito.addActionListener(this);
        delegado.tabbedPane.addChangeListener(this);
    }

    private void configurarAtajosTeclado() {
        KeyStroke ctrlA = KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK);
        delegado.btn_add.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlA, "agregar");
        delegado.btn_add.getActionMap().put("agregar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delegado.btn_add.doClick();
            }
        });
        
        KeyStroke ctrlM = KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK);
        delegado.btn_modificar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlM, "modificar");
        delegado.btn_modificar.getActionMap().put("modificar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delegado.btn_modificar.doClick();
            }
        });
        
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        delegado.tbl_contactos.getInputMap(JComponent.WHEN_FOCUSED).put(delete, "eliminar");
        delegado.tbl_contactos.getActionMap().put("eliminar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delegado.btn_eliminar.doClick();
            }
        });
        
        KeyStroke ctrlE = KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK);
        delegado.btn_exportar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlE, "exportar");
        delegado.btn_exportar.getActionMap().put("exportar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delegado.btn_exportar.doClick();
            }
        });
        
        KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK);
        delegado.txt_buscar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlF, "buscar");
        delegado.txt_buscar.getActionMap().put("buscar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delegado.txt_buscar.requestFocus();
            }
        });
    }
    
    // MEJORA UNIDAD 3: Configurar búsqueda concurrente con Timer
    private void configurarBusquedaConcurrente() {
        busquedaTimer = new Timer(300, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String texto = delegado.txt_buscar.getText();
                busquedaConcurrente(texto);
            }
        });
        busquedaTimer.setRepeats(false);
    }
    
    // MEJORA UNIDAD 3: Búsqueda en segundo plano sin bloquear UI
    private void busquedaConcurrente(String texto) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    // Simular búsqueda en grandes volúmenes
                    Thread.sleep(50);
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            filtrarTabla(texto);
                        }
                    });
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    private void inicializacionCampos() {
        nombres = delegado.txt_nombres.getText().trim();
        email = delegado.txt_email.getText().trim();
        telefono = delegado.txt_telefono.getText().trim();
    }

    private void cargarContactosRegistrados() {
        messages = delegado.getMessages();
        
        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish(0);
                try {
                    // MEJORA UNIDAD 3: Lock para lectura segura
                    contactosLock.lock();
                    try {
                        contactos = new personaDAO(new persona()).leerArchivo();
                    } finally {
                        contactosLock.unlock();
                    }
                    
                    publish(50);
                    
                    DefaultTableModel modelo = (DefaultTableModel) delegado.tbl_contactos.getModel();
                    modelo.setRowCount(0);
                    
                    int progreso = 50;
                    int incremento = contactos.isEmpty() ? 0 : 50 / contactos.size();
                    
                    for (persona contacto : contactos) {
                        Object[] fila = {
                            contacto.getNombre(),
                            contacto.getTelefono(),
                            contacto.getEmail(),
                            contacto.getCategoria(),
                            contacto.isFavorito()
                        };
                        modelo.addRow(fila);
                        progreso += incremento;
                        publish(Math.min(progreso, 100));
                        Thread.sleep(10);
                    }
                    publish(100);
                    
                } catch (IOException e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(delegado, 
                                messages.getString("msg.error.load"),
                                messages.getString("dialog.title.error"), 
                                JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }
                return null;
            }
            
            @Override
            protected void process(List<Integer> chunks) {
                int ultimo = chunks.get(chunks.size() - 1);
                delegado.progressBar.setValue(ultimo);
                String mensaje = MessageFormat.format(
                    messages.getString("progress.loading"), ultimo);
                delegado.progressBar.setString(mensaje);
            }
            
            @Override
            protected void done() {
                delegado.progressBar.setValue(0);
                delegado.progressBar.setString(messages.getString("app.ready"));
                configurarOrdenamiento();
                actualizarEstadisticas();
            }
        };
        worker.execute();
    }

    private void configurarOrdenamiento() {
        DefaultTableModel modelo = (DefaultTableModel) delegado.tbl_contactos.getModel();
        sorter = new TableRowSorter<>(modelo);
        delegado.tbl_contactos.setRowSorter(sorter);
    }

    private void limpiarCampos() {
        delegado.txt_nombres.setText("");
        delegado.txt_telefono.setText("");
        delegado.txt_email.setText("");
        categoria = "";
        favorito = false;
        delegado.chb_favorito.setSelected(false);
        delegado.cmb_categoria.setSelectedIndex(0);
        filaSeleccionada = -1;
        delegado.tbl_contactos.clearSelection();
    }

    private void actualizarEstadisticas() {
        if (contactos == null) return;
        
        int total = contactos.size();
        int favoritos = 0;
        int familia = 0;
        int amigos = 0;
        int trabajo = 0;
        
        messages = delegado.getMessages();
        String categoriaFamilia = messages.getString("category.family");
        String categoriaAmigos = messages.getString("category.friends");
        String categoriaTrabajo = messages.getString("category.work");
        
        for (persona p : contactos) {
            if (p.isFavorito()) favoritos++;
            
            String cat = p.getCategoria();
            if (cat.equals("Familia") || cat.equals("Family") || cat.equals("Famille") || 
                cat.equals(categoriaFamilia)) {
                familia++;
            } else if (cat.equals("Amigos") || cat.equals("Friends") || cat.equals("Amis") || 
                       cat.equals(categoriaAmigos)) {
                amigos++;
            } else if (cat.equals("Trabajo") || cat.equals("Work") || cat.equals("Travail") || 
                       cat.equals(categoriaTrabajo)) {
                trabajo++;
            }
        }
        
        delegado.lbl_totalContactos.setText(String.valueOf(total));
        delegado.lbl_totalFavoritos.setText(String.valueOf(favoritos));
        delegado.lbl_totalFamilia.setText(String.valueOf(familia));
        delegado.lbl_totalAmigos.setText(String.valueOf(amigos));
        delegado.lbl_totalTrabajo.setText(String.valueOf(trabajo));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        messages = delegado.getMessages();
        inicializacionCampos();

        if (e.getSource() == delegado.btn_add) {
            agregarContacto();
        } else if (e.getSource() == delegado.btn_eliminar || e.getSource() == delegado.menuEliminar) {
            eliminarContacto();
        } else if (e.getSource() == delegado.btn_modificar || e.getSource() == delegado.menuEditar) {
            modificarContacto();
        } else if (e.getSource() == delegado.btn_exportar) {
            exportarContactosConcurrente();
        } else if (e.getSource() == delegado.menuMarcarFavorito) {
            cambiarEstadoFavorito();
        } else if (e.getSource() == delegado.btn_exportarJSON) {
            exportarContactosJSON();
        } else if (e.getSource() == delegado.btn_importarJSON) {
            importarContactosJSON();
        }
        
    }
    
    // MEJORA UNIDAD 3: Validación de contacto duplicado en segundo plano
    private boolean validarContactoDuplicado(String nombre, String telefono, String email) {
        contactosLock.lock();
        try {
            for (persona p : contactos) {
                if (p.getNombre().equalsIgnoreCase(nombre) && 
                    p.getTelefono().equals(telefono) && 
                    p.getEmail().equalsIgnoreCase(email)) {
                    return true;
                }
            }
            return false;
        } finally {
            contactosLock.unlock();
        }
    }

    private void agregarContacto() {
        if (nombres.isEmpty() || telefono.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(delegado, 
                messages.getString("msg.warning.empty"), 
                messages.getString("dialog.title.warning"), 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String categoriaSeleccion = messages.getString("category.select");
        if (categoria.equals(categoriaSeleccion) || categoria.isEmpty()) {
            JOptionPane.showMessageDialog(delegado, 
                messages.getString("msg.warning.category"), 
                messages.getString("dialog.title.warning"), 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // MEJORA UNIDAD 3: Validación en segundo plano
        delegado.progressBar.setString("Validando contacto...");
        delegado.progressBar.setIndeterminate(true);
        
        SwingWorker<Boolean, Void> validacionWorker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Validación en thread separado
                Thread.sleep(200); // Simular validación
                return validarContactoDuplicado(nombres, telefono, email);
            }
            
            @Override
            protected void done() {
                try {
                    boolean esDuplicado = get();
                    
                    if (esDuplicado) {
                        delegado.progressBar.setIndeterminate(false);
                        delegado.progressBar.setString(messages.getString("app.ready"));
                        
                        JOptionPane.showMessageDialog(delegado, 
                            "El contacto ya existe en la base de datos", 
                            messages.getString("dialog.title.warning"), 
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    
                    // Si no es duplicado, proceder a guardar
                    guardarContacto();
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        validacionWorker.execute();
    }
    
    // MEJORA UNIDAD 3: Método separado para guardar con notificación
    private void guardarContacto() {
        delegado.progressBar.setString(messages.getString("progress.saving"));
        delegado.progressBar.setIndeterminate(true);
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // MEJORA UNIDAD 3: Sincronización al escribir
                contactosLock.lock();
                try {
                    persona = new persona(nombres, telefono, email, categoria, favorito);
                    new personaDAO(persona).escribirArchivo();
                } finally {
                    contactosLock.unlock();
                }
                return null;
            }
            
            @Override
            protected void done() {
                delegado.progressBar.setIndeterminate(false);
                delegado.progressBar.setString(messages.getString("app.ready"));
                limpiarCampos();
                cargarContactosRegistrados();
                
                // MEJORA UNIDAD 3: Notificación en tiempo real
                mostrarNotificacion(messages.getString("msg.success.add"), 
                                  messages.getString("dialog.title.success"));
            }
        };
        worker.execute();
    }
    
    // MEJORA UNIDAD 3: Sistema de notificaciones en tiempo real
    private void mostrarNotificacion(String mensaje, String titulo) {
        notificationExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(delegado, 
                            mensaje, 
                            titulo, 
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            }
        }, 100, TimeUnit.MILLISECONDS);
    }

    private void eliminarContacto() {
        int fila = delegado.tbl_contactos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(delegado, 
                messages.getString("msg.warning.select.delete"), 
                messages.getString("dialog.title.warning"), 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        final int modelRow = delegado.tbl_contactos.convertRowIndexToModel(fila);
        String nombre = contactos.get(modelRow).getNombre();
        
        String mensaje = MessageFormat.format(
            messages.getString("msg.confirm.delete"), nombre);
        
        int confirmacion = JOptionPane.showConfirmDialog(delegado, 
            mensaje, 
            messages.getString("dialog.title.confirm"), 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            delegado.progressBar.setString(messages.getString("progress.deleting"));
            delegado.progressBar.setIndeterminate(true);
            
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    // MEJORA UNIDAD 3: Sincronización al eliminar
                    contactosLock.lock();
                    try {
                        contactos.remove(modelRow);
                        new personaDAO(new persona()).actualizarContactos(contactos);
                    } finally {
                        contactosLock.unlock();
                    }
                    return null;
                }
                
                @Override
                protected void done() {
                    delegado.progressBar.setIndeterminate(false);
                    delegado.progressBar.setString(messages.getString("app.ready"));
                    limpiarCampos();
                    cargarContactosRegistrados();
                    
                    // MEJORA UNIDAD 3: Notificación en tiempo real
                    mostrarNotificacion(messages.getString("msg.success.delete"), 
                                      messages.getString("dialog.title.success"));
                }
            };
            worker.execute();
        }
    }

    private void modificarContacto() {
        int fila = delegado.tbl_contactos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(delegado, 
                messages.getString("msg.warning.select.modify"), 
                messages.getString("dialog.title.warning"), 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (nombres.isEmpty() || telefono.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(delegado, 
                messages.getString("msg.warning.empty"), 
                messages.getString("dialog.title.warning"), 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String categoriaSeleccion = messages.getString("category.select");
        if (categoria.equals(categoriaSeleccion) || categoria.isEmpty()) {
            JOptionPane.showMessageDialog(delegado, 
                messages.getString("msg.warning.category"), 
                messages.getString("dialog.title.warning"), 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        final int modelRow = delegado.tbl_contactos.convertRowIndexToModel(fila);
        
        delegado.progressBar.setString(messages.getString("progress.modifying"));
        delegado.progressBar.setIndeterminate(true);
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // MEJORA UNIDAD 3: Sincronización con lock al modificar
                contactosLock.lock();
                try {
                    persona p = contactos.get(modelRow);
                    p.setNombre(nombres);
                    p.setTelefono(telefono);
                    p.setEmail(email);
                    p.setCategoria(categoria);
                    p.setFavorito(favorito);
                    
                    new personaDAO(new persona()).actualizarContactos(contactos);
                } finally {
                    contactosLock.unlock();
                }
                return null;
            }
            
            @Override
            protected void done() {
                delegado.progressBar.setIndeterminate(false);
                delegado.progressBar.setString(messages.getString("app.ready"));
                limpiarCampos();
                cargarContactosRegistrados();
                
                // MEJORA UNIDAD 3: Notificación en tiempo real
                mostrarNotificacion(messages.getString("msg.success.modify"), 
                                  messages.getString("dialog.title.success"));
            }
        };
        worker.execute();
    }
    
    // MEJORA UNIDAD 3: Exportación concurrente con múltiples hilos
    private void exportarContactosConcurrente() {
        if (contactos == null || contactos.isEmpty()) {
            JOptionPane.showMessageDialog(delegado, 
                messages.getString("msg.warning.no.contacts"), 
                messages.getString("dialog.title.warning"), 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(messages.getString("dialog.title.save"));
        fileChooser.setSelectedFile(
            new java.io.File(messages.getString("csv.filename")));
        
        int userSelection = fileChooser.showSaveDialog(delegado);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            final java.io.File fileToSave = fileChooser.getSelectedFile();
            
            // MEJORA UNIDAD 3: Verificar si ya hay una exportación en curso
            if (!exportacionLock.tryLock()) {
                JOptionPane.showMessageDialog(delegado, 
                    "Ya hay una exportación en curso. Por favor espere.", 
                    messages.getString("dialog.title.warning"), 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            delegado.progressBar.setString(messages.getString("progress.exporting"));
            delegado.progressBar.setIndeterminate(true);
            
            // MEJORA UNIDAD 3: Usar ExecutorService para exportación
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        FileWriter writer = new FileWriter(fileToSave.getAbsolutePath());
                        
                        writer.write(messages.getString("csv.header") + "\n");
                        
                        // Exportar en bloques para simular procesamiento paralelo
                        contactosLock.lock();
                        try {
                            for (persona p : contactos) {
                                writer.write(p.datosContacto() + "\n");
                                Thread.sleep(10); // Simular procesamiento
                            }
                        } finally {
                            contactosLock.unlock();
                        }
                        
                        writer.close();
                        
                        // MEJORA UNIDAD 3: Notificar en UI thread
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                delegado.progressBar.setIndeterminate(false);
                                delegado.progressBar.setString(messages.getString("app.ready"));
                                
                                String mensaje = MessageFormat.format(
                                    messages.getString("msg.success.export"), 
                                    fileToSave.getAbsolutePath());
                                
                                mostrarNotificacion(mensaje, 
                                                  messages.getString("dialog.title.success"));
                            }
                        });
                        
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                delegado.progressBar.setIndeterminate(false);
                                delegado.progressBar.setString(messages.getString("app.ready"));
                                JOptionPane.showMessageDialog(delegado, 
                                    "Error al exportar: " + ex.getMessage(), 
                                    messages.getString("dialog.title.error"), 
                                    JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    } finally {
                        exportacionLock.unlock();
                    }
                }
            });
        }
    }

    private void cambiarEstadoFavorito() {
        int fila = delegado.tbl_contactos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(delegado, 
                messages.getString("msg.warning.select"), 
                messages.getString("dialog.title.warning"), 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        final int modelRow = delegado.tbl_contactos.convertRowIndexToModel(fila);
        
        // MEJORA UNIDAD 3: Sincronización al cambiar favorito
        contactosLock.lock();
        try {
            persona p = contactos.get(modelRow);
            p.setFavorito(!p.isFavorito());
            
            new personaDAO(new persona()).actualizarContactos(contactos);
            cargarContactosRegistrados();
            
            mostrarNotificacion(messages.getString("msg.success.favorite"), 
                              messages.getString("dialog.title.success"));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(delegado, 
                messages.getString("msg.error.favorite"), 
                messages.getString("dialog.title.error"), 
                JOptionPane.ERROR_MESSAGE);
        } finally {
            contactosLock.unlock();
        }
    }

    private void filtrarTabla(String texto) {
        if (texto.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto));
        }
        actualizarEstadisticas();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        
        int fila = delegado.tbl_contactos.getSelectedRow();
        if (fila != -1) {
            int modelRow = delegado.tbl_contactos.convertRowIndexToModel(fila);
            if (modelRow < contactos.size()) {
                cargarContacto(modelRow);
                filaSeleccionada = modelRow;
            }
        }
    }

    private void cargarContacto(int index) {
        persona p = contactos.get(index);
        delegado.txt_nombres.setText(p.getNombre());
        delegado.txt_telefono.setText(p.getTelefono());
        delegado.txt_email.setText(p.getEmail());
        delegado.chb_favorito.setSelected(p.isFavorito());
        delegado.cmb_categoria.setSelectedItem(p.getCategoria());
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
    if (e.getSource() == delegado.cmb_categoria) {
        Object selectedItem = delegado.cmb_categoria.getSelectedItem();
        if (selectedItem != null) {  // Prevenir NullPointerException
            categoria = selectedItem.toString();
        } else {
            categoria = "";  // Valor por defecto cuando no hay selección
        }
    } else if (e.getSource() == delegado.chb_favorito) {
        favorito = delegado.chb_favorito.isSelected();
    }
}

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getSource() == delegado.txt_buscar) {
            // MEJORA UNIDAD 3: Reiniciar timer para búsqueda concurrente
            busquedaTimer.restart();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            int fila = delegado.tbl_contactos.rowAtPoint(e.getPoint());
            if (fila >= 0) {
                delegado.tbl_contactos.setRowSelectionInterval(fila, fila);
                delegado.popupMenu.show(delegado.tbl_contactos, e.getX(), e.getY());
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == delegado.tabbedPane) {
            int index = delegado.tabbedPane.getSelectedIndex();
            if (index == 1) {
                actualizarEstadisticas();
            }
        }
    }
    
    // MEJORA UNIDAD 3: Método para cerrar recursos al salir
    public void shutdown() {
        executorService.shutdown();
        notificationExecutor.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            if (!notificationExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                notificationExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            notificationExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    private void exportarContactosJSON() {
    if (contactos == null || contactos.isEmpty()) {
        JOptionPane.showMessageDialog(delegado, 
            "No hay contactos para exportar", 
            messages.getString("dialog.title.warning"), 
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Guardar Contactos en JSON");
    fileChooser.setSelectedFile(new java.io.File("contactos_exportados.json"));
    
    int userSelection = fileChooser.showSaveDialog(delegado);
    
    if (userSelection == JFileChooser.APPROVE_OPTION) {
        final java.io.File fileToSave = fileChooser.getSelectedFile();
        
        delegado.progressBar.setString("Exportando a JSON...");
        delegado.progressBar.setIndeterminate(true);
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                contactosLock.lock();
                try {
                    return jsonHandler.exportarContactosJSON(contactos, fileToSave);
                } finally {
                    contactosLock.unlock();
                }
            }
            
            @Override
            protected void done() {
                delegado.progressBar.setIndeterminate(false);
                delegado.progressBar.setString(messages.getString("app.ready"));
                
                try {
                    boolean exitoso = get();
                    
                    if (exitoso) {
                        String mensaje = "Contactos exportados exitosamente a JSON:\n" + 
                                       fileToSave.getAbsolutePath();
                        
                        mostrarNotificacion(mensaje, 
                                          messages.getString("dialog.title.success"));
                    } else {
                        JOptionPane.showMessageDialog(delegado, 
                            "Error al exportar contactos a JSON", 
                            messages.getString("dialog.title.error"), 
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(delegado, 
                        "Error: " + ex.getMessage(), 
                        messages.getString("dialog.title.error"), 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}

/**
 * UNIDAD 4: Importar contactos desde formato JSON
 */
private void importarContactosJSON() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Seleccionar Archivo JSON");
    fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
        @Override
        public boolean accept(java.io.File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".json");
        }
        
        @Override
        public String getDescription() {
            return "Archivos JSON (*.json)";
        }
    });
    
    int userSelection = fileChooser.showOpenDialog(delegado);
    
    if (userSelection == JFileChooser.APPROVE_OPTION) {
        final java.io.File fileToOpen = fileChooser.getSelectedFile();
        
        // Validar archivo antes de importar
        if (!jsonHandler.validarArchivoJSON(fileToOpen)) {
            JOptionPane.showMessageDialog(delegado, 
                "El archivo JSON no es válido o está corrupto", 
                messages.getString("dialog.title.error"), 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        delegado.progressBar.setString("Importando desde JSON...");
        delegado.progressBar.setIndeterminate(true);
        
        SwingWorker<List<persona>, Void> worker = new SwingWorker<List<persona>, Void>() {
            @Override
            protected List<persona> doInBackground() throws Exception {
                return jsonHandler.importarContactosJSON(fileToOpen);
            }
            
            @Override
            protected void done() {
                delegado.progressBar.setIndeterminate(false);
                delegado.progressBar.setString(messages.getString("app.ready"));
                
                try {
                    List<persona> contactosImportados = get();
                    
                    if (contactosImportados == null || contactosImportados.isEmpty()) {
                        JOptionPane.showMessageDialog(delegado, 
                            "No se encontraron contactos en el archivo JSON", 
                            messages.getString("dialog.title.warning"), 
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    
                    // Preguntar si desea reemplazar o agregar contactos
                    String[] opciones = {"Reemplazar", "Agregar", "Cancelar"};
                    int opcion = JOptionPane.showOptionDialog(delegado,
                        "Se encontraron " + contactosImportados.size() + " contactos.\n" +
                        "¿Desea reemplazar los contactos existentes o agregarlos?",
                        "Importar Contactos",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        opciones,
                        opciones[1]);
                    
                    if (opcion == JOptionPane.CANCEL_OPTION || opcion == JOptionPane.CLOSED_OPTION) {
                        return;
                    }
                    
                    contactosLock.lock();
                    try {
                        if (opcion == 0) {
                            // Reemplazar
                            contactos = contactosImportados;
                        } else {
                            // Agregar
                            for (persona p : contactosImportados) {
                                // Evitar duplicados
                                boolean existe = contactos.stream()
                                    .anyMatch(c -> c.getNombre().equalsIgnoreCase(p.getNombre()) && 
                                                 c.getTelefono().equals(p.getTelefono()));
                                if (!existe) {
                                    contactos.add(p);
                                }
                            }
                        }
                        
                        // Actualizar archivo CSV
                        new personaDAO(new persona()).actualizarContactos(contactos);
                        
                    } finally {
                        contactosLock.unlock();
                    }
                    
                    // Recargar tabla
                    cargarContactosRegistrados();
                    
                    String mensaje = contactosImportados.size() + " contactos importados exitosamente desde JSON";
                    mostrarNotificacion(mensaje, 
                                      messages.getString("dialog.title.success"));
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(delegado, 
                        "Error al importar: " + ex.getMessage(), 
                        messages.getString("dialog.title.error"), 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
    
    
}
