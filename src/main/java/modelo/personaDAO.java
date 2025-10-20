package modelo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class personaDAO {
    
    private File archivo;
    private persona persona;
    
    public personaDAO(persona persona) {
        this.persona = persona;
        
        // Opción 1: Usar directorio del usuario (RECOMENDADO)
        String rutaUsuario = System.getProperty("user.home");
        archivo = new File(rutaUsuario, "gestionContactos");
        
        // Opción 2: Usar directorio del proyecto
        // archivo = new File("gestionContactos");
        
        // Opción 3: Ruta absoluta (cambiar según tu sistema)
        // archivo = new File("/home/jhonas/gestionContactos");
        // archivo = new File("C:\\Users\\TuUsuario\\gestionContactos");
        
        prepararArchivo();
    }
    
    private void prepararArchivo() {
        // Verificar si el directorio existe
        if (!archivo.exists()) {
            archivo.mkdir();
        }
        
        // Acceder al archivo dentro del directorio
        archivo = new File(archivo.getAbsolutePath(), "datosContactos.csv");
        
        // Verificar si el archivo existe
        if (!archivo.exists()) {
            try {
                archivo.createNewFile();
                // Escribir encabezado
                String encabezado = String.format("%s;%s;%s;%s;%s", 
                    "NOMBRE", "TELEFONO", "EMAIL", "CATEGORIA", "FAVORITO");
                escribir(encabezado);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void escribir(String texto) {
        try {
            FileWriter escribir = new FileWriter(archivo.getAbsolutePath(), true);
            escribir.write(texto + "\n");
            escribir.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public boolean escribirArchivo() {
        escribir(persona.datosContacto());
        return true;
    }
    
    public List<persona> leerArchivo() throws IOException {
        String contactos = "";
        FileReader leer = new FileReader(archivo.getAbsolutePath());
        int c;
        
        while ((c = leer.read()) != -1) {
            contactos += String.valueOf((char) c);
        }
        leer.close();
        
        // Separar cada contacto por salto de línea
        String[] datos = contactos.split("\n");
        List<persona> personas = new ArrayList<>();
        
        // Recorrer cada contacto (saltar el encabezado en índice 0)
        for (int i = 0; i < datos.length; i++) {
            String contacto = datos[i].trim();
            
            // Saltar líneas vacías y el encabezado
            if (contacto.isEmpty() || contacto.startsWith("NOMBRE")) {
                continue;
            }
            
            try {
                String[] campos = contacto.split(";");
                
                // Verificar que tengamos todos los campos necesarios
                if (campos.length >= 5) {
                    persona p = new persona();
                    p.setNombre(campos[0].trim());
                    p.setTelefono(campos[1].trim());
                    p.setEmail(campos[2].trim());
                    p.setCategoria(campos[3].trim());
                    p.setFavorito(Boolean.parseBoolean(campos[4].trim()));
                    personas.add(p);
                }
            } catch (Exception e) {
                // Saltar líneas con formato incorrecto
                System.err.println("Error al procesar línea: " + contacto);
            }
        }
        
        return personas;
    }
    
    public void actualizarContactos(List<persona> personas) throws IOException {
        // Eliminar el archivo actual
        if (archivo.exists()) {
            archivo.delete();
        }
        
        // Crear nuevo archivo con encabezado
        archivo.createNewFile();
        String encabezado = String.format("%s;%s;%s;%s;%s", 
            "NOMBRE", "TELEFONO", "EMAIL", "CATEGORIA", "FAVORITO");
        escribir(encabezado);
        
        // Escribir todos los contactos
        for (persona p : personas) {
            escribir(p.datosContacto());
        }
    }
    
    // Método adicional para obtener la ruta del archivo (útil para debugging)
    public String getRutaArchivo() {
        return archivo.getAbsolutePath();
    }
}