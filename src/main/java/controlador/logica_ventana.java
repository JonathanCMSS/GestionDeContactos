package controlador;

import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import vista.ventana;
import modelo.*;
import java.util.List;

public class logica_ventana implements ActionListener, ListSelectionListener, 
                                       ItemListener, KeyListener, MouseListener, 
                                       ChangeListener {
    
    private ventana delegado;
    private String nombres, email, telefono, categoria = "";
    private persona persona;
    private List<persona> contactos;
    private boolean favorito = false;
    private TableRowSorter<DefaultTableModel> sorter;
    private int filaSeleccionada = -1;

    public logica_ventana(ventana delegado) {
        this.delegado = delegado;
        cargarContactosRegistrados();
        registrarEventos();
        configurarAtajosTeclado();
        actualizarEstadisticas();
    }

    private void registrarEventos() {
        // ActionListeners para botones
        delegado.btn_add.addActionListener(this);
        delegado.btn_eliminar.addActionListener(this);
        delegado.btn_modificar.addActionListener(this);
        delegado.btn_exportar.addActionListener(this);
        
        // ListSelectionListener para la tabla
        delegado.tbl_contactos.getSelectionModel().addListSelectionListener(this);
        
        // ItemListeners
        delegado.cmb_categoria.addItemListener(this);
        delegado.chb_favorito.addItemListener(this);
        
        // KeyListeners para búsqueda en tiempo real
        delegado.txt_buscar.addKeyListener(this);
        delegado.txt_nombres.addKeyListener(this);
        delegado.txt_telefono.addKeyListener(this);
        delegado.txt_email.addKeyListener(this);
        
        // MouseListener para menú contextual
        delegado.tbl_contactos.addMouseListener(this);
        
        // ActionListeners para menú contextual
        delegado.menuEditar.addActionListener(this);
        delegado.menuEliminar.addActionListener(this);
        delegado.menuMarcarFavorito.addActionListener(this);
        
        // ChangeListener para cambio de pestañas
        delegado.tabbedPane.addChangeListener(this);
    }

    private void configurarAtajosTeclado() {
        // Ctrl+A para agregar
        KeyStroke ctrlA = KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK);
        delegado.btn_add.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlA, "agregar");
        delegado.btn_add.getActionMap().put("agregar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delegado.btn_add.doClick();
            }
        });
        
        // Ctrl+M para modificar
        KeyStroke ctrlM = KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK);
        delegado.btn_modificar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlM, "modificar");
        delegado.btn_modificar.getActionMap().put("modificar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delegado.btn_modificar.doClick();
            }
        });
        
        // Delete para eliminar
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        delegado.tbl_contactos.getInputMap(JComponent.WHEN_FOCUSED).put(delete, "eliminar");
        delegado.tbl_contactos.getActionMap().put("eliminar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delegado.btn_eliminar.doClick();
            }
        });
        
        // Ctrl+E para exportar
        KeyStroke ctrlE = KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK);
        delegado.btn_exportar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlE, "exportar");
        delegado.btn_exportar.getActionMap().put("exportar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delegado.btn_exportar.doClick();
            }
        });
        
        // Ctrl+F para buscar
        KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK);
        delegado.txt_buscar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlF, "buscar");
        delegado.txt_buscar.getActionMap().put("buscar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delegado.txt_buscar.requestFocus();
            }
        });
    }

    private void inicializacionCampos() {
        nombres = delegado.txt_nombres.getText().trim();
        email = delegado.txt_email.getText().trim();
        telefono = delegado.txt_telefono.getText().trim();
    }

    private void cargarContactosRegistrados() {
        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish(0);
                try {
                    contactos = new personaDAO(new persona()).leerArchivo();
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
                                "Error al cargar los contactos",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }
                return null;
            }
            
            @Override
            protected void process(List<Integer> chunks) {
                int ultimo = chunks.get(chunks.size() - 1);
                delegado.progressBar.setValue(ultimo);
                delegado.progressBar.setString("Cargando contactos... " + ultimo + "%");
            }
            
            @Override
            protected void done() {
                delegado.progressBar.setValue(0);
                delegado.progressBar.setString("Listo");
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
        
        for (persona p : contactos) {
            if (p.isFavorito()) favoritos++;
            
            String cat = p.getCategoria();
            if (cat.equals("Familia")) {
                familia++;
            } else if (cat.equals("Amigos")) {
                amigos++;
            } else if (cat.equals("Trabajo")) {
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
        inicializacionCampos();

        if (e.getSource() == delegado.btn_add) {
            agregarContacto();
        } else if (e.getSource() == delegado.btn_eliminar || e.getSource() == delegado.menuEliminar) {
            eliminarContacto();
        } else if (e.getSource() == delegado.btn_modificar || e.getSource() == delegado.menuEditar) {
            modificarContacto();
        } else if (e.getSource() == delegado.btn_exportar) {
            exportarContactos();
        } else if (e.getSource() == delegado.menuMarcarFavorito) {
            cambiarEstadoFavorito();
        }
    }

    private void agregarContacto() {
        if (nombres.isEmpty() || telefono.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(delegado, 
                "Todos los campos deben ser llenados", 
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (categoria.equals("Elija una Categoria") || categoria.isEmpty()) {
            JOptionPane.showMessageDialog(delegado, 
                "Debe seleccionar una categoría válida", 
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        delegado.progressBar.setString("Guardando contacto...");
        delegado.progressBar.setIndeterminate(true);
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                persona = new persona(nombres, telefono, email, categoria, favorito);
                new personaDAO(persona).escribirArchivo();
                return null;
            }
            
            @Override
            protected void done() {
                delegado.progressBar.setIndeterminate(false);
                delegado.progressBar.setString("Listo");
                limpiarCampos();
                cargarContactosRegistrados();
                JOptionPane.showMessageDialog(delegado, 
                    "Contacto registrado exitosamente", 
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
        };
        worker.execute();
    }

    private void eliminarContacto() {
        int fila = delegado.tbl_contactos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(delegado, 
                "Debe seleccionar un contacto para eliminar", 
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        final int modelRow = delegado.tbl_contactos.convertRowIndexToModel(fila);
        String nombre = contactos.get(modelRow).getNombre();
        
        int confirmacion = JOptionPane.showConfirmDialog(delegado, 
            "¿Está seguro de eliminar el contacto: " + nombre + "?", 
            "Confirmar eliminación", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            delegado.progressBar.setString("Eliminando contacto...");
            delegado.progressBar.setIndeterminate(true);
            
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    contactos.remove(modelRow);
                    new personaDAO(new persona()).actualizarContactos(contactos);
                    return null;
                }
                
                @Override
                protected void done() {
                    delegado.progressBar.setIndeterminate(false);
                    delegado.progressBar.setString("Listo");
                    limpiarCampos();
                    cargarContactosRegistrados();
                    JOptionPane.showMessageDialog(delegado, 
                        "Contacto eliminado exitosamente", 
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            };
            worker.execute();
        }
    }

    private void modificarContacto() {
        int fila = delegado.tbl_contactos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(delegado, 
                "Debe seleccionar un contacto para modificar", 
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (nombres.isEmpty() || telefono.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(delegado, 
                "Todos los campos deben ser llenados", 
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (categoria.equals("Elija una Categoria") || categoria.isEmpty()) {
            JOptionPane.showMessageDialog(delegado, 
                "Debe seleccionar una categoría válida", 
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        final int modelRow = delegado.tbl_contactos.convertRowIndexToModel(fila);
        
        delegado.progressBar.setString("Modificando contacto...");
        delegado.progressBar.setIndeterminate(true);
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                persona p = contactos.get(modelRow);
                p.setNombre(nombres);
                p.setTelefono(telefono);
                p.setEmail(email);
                p.setCategoria(categoria);
                p.setFavorito(favorito);
                
                new personaDAO(new persona()).actualizarContactos(contactos);
                return null;
            }
            
            @Override
            protected void done() {
                delegado.progressBar.setIndeterminate(false);
                delegado.progressBar.setString("Listo");
                limpiarCampos();
                cargarContactosRegistrados();
                JOptionPane.showMessageDialog(delegado, 
                    "Contacto modificado exitosamente", 
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
        };
        worker.execute();
    }

    private void exportarContactos() {
        if (contactos == null || contactos.isEmpty()) {
            JOptionPane.showMessageDialog(delegado, 
                "No hay contactos para exportar", 
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar archivo CSV");
        fileChooser.setSelectedFile(new java.io.File("contactos_exportados.csv"));
        
        int userSelection = fileChooser.showSaveDialog(delegado);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            final java.io.File fileToSave = fileChooser.getSelectedFile();
            
            delegado.progressBar.setString("Exportando contactos...");
            delegado.progressBar.setIndeterminate(true);
            
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    FileWriter writer = new FileWriter(fileToSave.getAbsolutePath());
                    
                    // Escribir encabezado
                    writer.write("Nombre;Teléfono;Email;Categoría;Favorito\n");
                    
                    // Escribir datos
                    for (persona p : contactos) {
                        writer.write(p.datosContacto() + "\n");
                    }
                    
                    writer.close();
                    return null;
                }
                
                @Override
                protected void done() {
                    delegado.progressBar.setIndeterminate(false);
                    delegado.progressBar.setString("Listo");
                    JOptionPane.showMessageDialog(delegado, 
                        "Contactos exportados exitosamente a:\n" + fileToSave.getAbsolutePath(), 
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            };
            worker.execute();
        }
    }

    private void cambiarEstadoFavorito() {
        int fila = delegado.tbl_contactos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(delegado, 
                "Debe seleccionar un contacto", 
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        final int modelRow = delegado.tbl_contactos.convertRowIndexToModel(fila);
        persona p = contactos.get(modelRow);
        p.setFavorito(!p.isFavorito());
        
        try {
            new personaDAO(new persona()).actualizarContactos(contactos);
            cargarContactosRegistrados();
            JOptionPane.showMessageDialog(delegado, 
                "Estado de favorito actualizado", 
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(delegado, 
                "Error al actualizar favorito", 
                "Error", JOptionPane.ERROR_MESSAGE);
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
            categoria = delegado.cmb_categoria.getSelectedItem().toString();
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
            String texto = delegado.txt_buscar.getText();
            filtrarTabla(texto);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) { // Clic derecho
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
            if (index == 1) { // Pestaña de estadísticas
                actualizarEstadisticas();
            }
        }
    }
}