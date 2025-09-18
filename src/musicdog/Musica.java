/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package musicdog;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Musica {
    private String titulo; 
    private String artista;
    private int duracion;
    private String coverPath;
    private String ruta;
    private String genero; 

    public Musica(String titulo, String artista, String coverPath, int duracion, String ruta, String genero) {
        this.coverPath = coverPath;
        this.titulo = titulo;
        this.artista = artista;
        this.duracion = duracion;
        this.ruta = ruta;
        this.genero = genero;
        try {
            init();
        } catch (IOException e) {
            System.err.println("Error during initialization: " + e.getMessage());
        }
    }

    public String getTitulo() {
        return titulo;
    }

    public String getArtista() {
        return artista;
    }

    public int getDuracion() {
        return duracion;
    }

    public String getRuta() {
        return ruta;
    }

    public String getCoverPath() {
        return coverPath;
    }
    
    public String getGenero() {
        return genero;
    }

    @Override
    public String toString() {
        return "Canción: " + titulo + " por: " + artista 
                + " (Cover: " + coverPath + ", Género: " + genero + ")";
    }

    private void init() throws IOException {
        String sanitizedTitle = titulo.replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9_\\-]", "");
        String filePath = "music/" + sanitizedTitle + ".priv";

        try (RandomAccessFile musica = new RandomAccessFile(filePath, "rw")) {
            musica.writeUTF(titulo);
            musica.writeUTF(artista);
            musica.writeInt(duracion);
            musica.writeUTF(coverPath);
            musica.writeUTF(ruta);
            musica.writeUTF(genero); 
        } catch (IOException e){
            System.out.println("No se pudo guardar el archivo de musica!");
        }
    }
}