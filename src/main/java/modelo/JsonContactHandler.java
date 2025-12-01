package modelo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase para gestionar la serialización y deserialización de contactos en formato JSON.
 * 
 * UNIDAD 4 - MEJORAS IMPLEMENTADAS:
 * - Uso de Gson para serialización JSON
 * - Manejo robusto de errores
 * - Importación y exportación de contactos en JSON
 * - Formato JSON legible para humanos (pretty printing)
 * 
 * @author jonathan
 * @version 2.0
 */
public class JsonContactHandler {
    
    private final Gson gson;
    private final File defaultJsonPath;
    
    /**
     * Constructor que inicializa Gson con formato legible
     */
    public JsonContactHandler() {
        // Configurar Gson con formato "pretty printing" para mejor legibilidad
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();
        
        // Definir ruta por defecto en el directorio del usuario
        String rutaUsuario = System.getProperty("user.home");
        File directorioApp = new File(rutaUsuario, "gestionContactos");
        
        if (!directorioApp.exists()) {
            directorioApp.mkdir();
        }
        
        this.defaultJsonPath = new File(directorioApp, "contactos.json");
    }
    
    /**
     * Exporta una lista de contactos a formato JSON
     * 
     * @param contactos Lista de contactos a exportar
     * @param archivoDestino Archivo donde se guardará el JSON
     * @return true si la exportación fue exitosa, false en caso contrario
     */
    public boolean exportarContactosJSON(List<persona> contactos, File archivoDestino) {
        try (FileWriter writer = new FileWriter(archivoDestino, StandardCharsets.UTF_8)) {
            
            // Convertir lista de contactos a JSON
            String json = gson.toJson(contactos);
            
            // Escribir al archivo
            writer.write(json);
            writer.flush();
            
            System.out.println("✅ Contactos exportados exitosamente a: " + 
                             archivoDestino.getAbsolutePath());
            return true;
            
        } catch (IOException e) {
            System.err.println("❌ Error al exportar contactos a JSON: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Exporta contactos a la ruta por defecto
     * 
     * @param contactos Lista de contactos a exportar
     * @return true si la exportación fue exitosa
     */
    public boolean exportarContactosJSON(List<persona> contactos) {
        return exportarContactosJSON(contactos, defaultJsonPath);
    }
    
    /**
     * Importa contactos desde un archivo JSON
     * 
     * @param archivoOrigen Archivo JSON con los contactos
     * @return Lista de contactos deserializados, o lista vacía si hay error
     */
    public List<persona> importarContactosJSON(File archivoOrigen) {
        try (FileReader reader = new FileReader(archivoOrigen, StandardCharsets.UTF_8)) {
            
            // Validar que el archivo existe y no está vacío
            if (!archivoOrigen.exists()) {
                System.err.println("❌ El archivo no existe: " + archivoOrigen.getAbsolutePath());
                return new ArrayList<>();
            }
            
            if (archivoOrigen.length() == 0) {
                System.err.println("⚠️ El archivo JSON está vacío");
                return new ArrayList<>();
            }
            
            // Definir el tipo de dato para la deserialización
            Type tipoListaContactos = new TypeToken<List<persona>>(){}.getType();
            
            // Deserializar JSON a lista de contactos
            List<persona> contactos = gson.fromJson(reader, tipoListaContactos);
            
            if (contactos == null) {
                System.err.println("⚠️ No se pudieron leer contactos del archivo JSON");
                return new ArrayList<>();
            }
            
            System.out.println("✅ " + contactos.size() + " contactos importados exitosamente desde: " + 
                             archivoOrigen.getAbsolutePath());
            
            return contactos;
            
        } catch (IOException e) {
            System.err.println("❌ Error al importar contactos desde JSON: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } catch (com.google.gson.JsonSyntaxException e) {
            System.err.println("❌ Error de sintaxis JSON: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Importa contactos desde la ruta por defecto
     * 
     * @return Lista de contactos deserializados
     */
    public List<persona> importarContactosJSON() {
        return importarContactosJSON(defaultJsonPath);
    }
    
    /**
     * Convierte un único contacto a formato JSON
     * 
     * @param contacto Contacto a serializar
     * @return String con el JSON del contacto
     */
    public String contactoToJson(persona contacto) {
        return gson.toJson(contacto);
    }
    
    /**
     * Convierte un JSON a un objeto persona
     * 
     * @param json String con el JSON
     * @return Objeto persona deserializado
     */
    public persona jsonToContacto(String json) {
        try {
            return gson.fromJson(json, persona.class);
        } catch (com.google.gson.JsonSyntaxException e) {
            System.err.println("❌ Error al deserializar JSON: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Verifica si existe un archivo JSON de respaldo
     * 
     * @return true si existe el archivo por defecto
     */
    public boolean existeArchivoJSON() {
        return defaultJsonPath.exists() && defaultJsonPath.length() > 0;
    }
    
    /**
     * Obtiene la ruta del archivo JSON por defecto
     * 
     * @return File con la ruta del archivo JSON
     */
    public File getRutaArchivoJSON() {
        return defaultJsonPath;
    }
    
    /**
     * Crea un archivo JSON de ejemplo con contactos de prueba
     * Útil para demostración o testing
     * 
     * @return true si se creó exitosamente
     */
    public boolean crearArchivoEjemplo() {
        List<persona> contactosEjemplo = new ArrayList<>();
        
        contactosEjemplo.add(new persona(
            "Juan Pérez", 
            "0991234567", 
            "juan.perez@email.com", 
            "Familia", 
            true
        ));
        
        contactosEjemplo.add(new persona(
            "María González", 
            "0987654321", 
            "maria.gonzalez@email.com", 
            "Amigos", 
            false
        ));
        
        contactosEjemplo.add(new persona(
            "Carlos Rodríguez", 
            "0998765432", 
            "carlos.rodriguez@empresa.com", 
            "Trabajo", 
            true
        ));
        
        File archivoEjemplo = new File(defaultJsonPath.getParent(), "contactos_ejemplo.json");
        return exportarContactosJSON(contactosEjemplo, archivoEjemplo);
    }
    
    /**
     * Valida la estructura de un archivo JSON antes de importarlo
     * 
     * @param archivo Archivo JSON a validar
     * @return true si el JSON es válido
     */
    public boolean validarArchivoJSON(File archivo) {
        try {
            String contenido = FileUtils.readFileToString(archivo, StandardCharsets.UTF_8);
            
            // Intentar parsear para validar sintaxis
            Type tipoListaContactos = new TypeToken<List<persona>>(){}.getType();
            List<persona> test = gson.fromJson(contenido, tipoListaContactos);
            
            return test != null;
            
        } catch (IOException | com.google.gson.JsonSyntaxException e) {
            System.err.println("❌ Archivo JSON inválido: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Método de utilidad para obtener estadísticas de un archivo JSON
     * 
     * @param archivo Archivo JSON a analizar
     * @return String con las estadísticas
     */
    public String obtenerEstadisticasJSON(File archivo) {
        List<persona> contactos = importarContactosJSON(archivo);
        
        if (contactos.isEmpty()) {
            return "No hay contactos en el archivo";
        }
        
        int totalContactos = contactos.size();
        long favoritos = contactos.stream().filter(persona::isFavorito).count();
        long familia = contactos.stream()
                .filter(p -> p.getCategoria().equalsIgnoreCase("Familia"))
                .count();
        long amigos = contactos.stream()
                .filter(p -> p.getCategoria().equalsIgnoreCase("Amigos"))
                .count();
        long trabajo = contactos.stream()
                .filter(p -> p.getCategoria().equalsIgnoreCase("Trabajo"))
                .count();
        
        return String.format(
            "📊 Estadísticas del archivo JSON:\n" +
            "Total de contactos: %d\n" +
            "Favoritos: %d\n" +
            "Familia: %d\n" +
            "Amigos: %d\n" +
            "Trabajo: %d",
            totalContactos, favoritos, familia, amigos, trabajo
        );
    }
}