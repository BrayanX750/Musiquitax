/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package musicdog;

import java.io.*;

public class ListaEnlazada {

    Cabecera cabeza;

    
    public ListaEnlazada() {
        cargarListaDeReproduccion();
    }

    public String construirListado() {
    StringBuilder sb = new StringBuilder();
    if (cabeza == null) {
        sb.append("(lista vacía)");
        return sb.toString();
    }
    int i = 0;
    Cabecera tmp = cabeza;
    while (tmp != null) {
        Musica m = tmp.musica;
        sb.append(String.format("[%d] %s - %s | %s | %ds | %s%n",
                i, m.getTitulo(), m.getArtista(), m.getGenero(), m.getDuracion(), m.getRuta()));
        tmp = tmp.siguiente;
        i++;
    }
    return sb.toString();
}
    public void agregarCancion(String titulo, String artista, String rutaCover, int duracion, String ruta, String genero) {
        Musica nuevaCancion = new Musica(titulo, artista, rutaCover, duracion, ruta, genero);
        Cabecera nuevo = new Cabecera(nuevaCancion);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            Cabecera temporal = cabeza;
            while (temporal.siguiente != null) {
                temporal = temporal.siguiente;
            }
            temporal.siguiente = nuevo;
        }
        guardarListaDeReproduccion();
    }


    public Cabecera seleccionarCancion(int indice) {
        Cabecera temporal = cabeza;
        int contador = 0;
        while (temporal != null && contador < indice) {
            temporal = temporal.siguiente;
            contador++;
        }
        return temporal;
    }

    public void eliminarCancion(int indice) {
        if (cabeza == null) {
            return;
        }
        if (indice == 0) {
            cabeza = cabeza.siguiente;
            guardarListaDeReproduccion();
            return;
        }
        Cabecera actual = cabeza;
        int cont = 0;
        while (actual != null && actual.siguiente != null) {
            if (cont == indice - 1) {
                actual.siguiente = actual.siguiente.siguiente;
                guardarListaDeReproduccion();
                return;
            }
            actual = actual.siguiente;
            cont++;
        }
    }

    
    private void agregarCancionDirecta(String titulo, String artista, String rutaCover, int duracion, String ruta, String genero) {
        Musica nuevaCancion = new Musica(titulo, artista, rutaCover, duracion, ruta, genero);
        Cabecera nuevo = new Cabecera(nuevaCancion);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            Cabecera temporal = cabeza;
            while (temporal.siguiente != null) {
                temporal = temporal.siguiente;
            }
            temporal.siguiente = nuevo;
        }
    }

   
    public void guardarListaDeReproduccion() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("listaReproduccion.txt"))) {
            Cabecera actual = cabeza;
            while (actual != null) {
                Musica m = actual.musica;
              
                pw.println(m.getTitulo() + "|" + m.getArtista() + "|" + m.getCoverPath() + "|" + m.getDuracion() + "|" + m.getRuta() + "|" + m.getGenero());
                actual = actual.siguiente;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   
    public void cargarListaDeReproduccion() {
        File archivo = new File("listaReproduccion.txt");
        if (!archivo.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            cabeza = null; 
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split("\\|");
                if (partes.length == 6) {
                    String titulo = partes[0];
                    String artista = partes[1];
                    String rutaCover = partes[2];
                    int duracion = Integer.parseInt(partes[3]);
                    String ruta = partes[4];
                    String genero = partes[5];
                    agregarCancionDirecta(titulo, artista, rutaCover, duracion, ruta, genero);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
