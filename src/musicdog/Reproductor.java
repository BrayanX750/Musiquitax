/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package musicdog;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Map;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javazoom.jlgui.basicplayer.*;

public class Reproductor extends JFrame implements BasicPlayerListener {

    private final ListaEnlazada lista = new ListaEnlazada();
    private Musica cancionActual;
    private File archivoActual;

    private BasicPlayer player;
    private boolean reproduciendo = false, enPausa = false;
    private long duracionMs = -1, totalBytes = -1;
    private final Map<String,Object> audioInfo = new HashMap<>();
    private JLabel lblCover, lblTitulo, lblArtista, lblTiempo, lblTotal;
    private JSlider sldProgreso;
    private JButton btnPlayPause, btnStop;
    private boolean arrastrando = false;

    private static final Color COLOR_BG = new Color(18,18,18);
    private static final Color COLOR_PANEL = new Color(24,24,24);
    private static final Color COLOR_CARD = new Color(32,32,32);
    private static final Color COLOR_ACCENT = new Color(29,185,84);
    private static final Color COLOR_TEXT = new Color(235,235,235);
    private static final Color COLOR_SUBTEXT = new Color(160,160,160);

    public Reproductor() {
        setTitle("Reproductor de Música");
        setSize(820, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(COLOR_BG);
        addWindowListener(new WindowAdapter(){ @Override public void windowClosing(WindowEvent e){ detener(); }});

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_PANEL);
        header.setBorder(new EmptyBorder(14,18,14,18));
        JLabel titulo = new JLabel("Reproductor");
        titulo.setForeground(COLOR_TEXT);
        titulo.setFont(new Font("Verdana", Font.BOLD, 20));
        JButton btnBiblioteca = crearBotonHeader("Abrir biblioteca");
        btnBiblioteca.addActionListener(e -> abrirVentanaBiblioteca());
        header.add(titulo, BorderLayout.WEST);
        header.add(btnBiblioteca, BorderLayout.EAST);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(COLOR_BG);
        JPanel card = new JPanel(new BorderLayout(14,14));
        card.setBackground(COLOR_CARD);
        card.setBorder(new EmptyBorder(18,18,18,18));

        lblCover = new JLabel("Sin portada", SwingConstants.CENTER);
        lblCover.setPreferredSize(new Dimension(260,260));
        lblCover.setOpaque(true);
        lblCover.setBackground(new Color(40,40,40));
        lblCover.setForeground(COLOR_SUBTEXT);
        lblCover.setBorder(BorderFactory.createLineBorder(new Color(55,55,55)));

        JPanel info = new JPanel();
        info.setBackground(COLOR_CARD);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        lblTitulo = new JLabel("Sin canción seleccionada");
        lblTitulo.setForeground(COLOR_TEXT);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
        lblArtista = new JLabel("—");
        lblArtista.setForeground(COLOR_SUBTEXT);
        lblArtista.setFont(new Font("Arial", Font.PLAIN, 14));
        info.add(lblTitulo);
        info.add(Box.createRigidArea(new Dimension(0,6)));
        info.add(lblArtista);

        card.add(lblCover, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);
        center.add(card, new GridBagConstraints());

        JPanel playerBar = new JPanel(new BorderLayout(10,10));
        playerBar.setBackground(COLOR_PANEL);
        playerBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1,0,0,0, new Color(40,40,40)),
                new EmptyBorder(12,16,12,16)
        ));

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        controles.setBackground(COLOR_PANEL);
        btnPlayPause = crearBotonControl("▶ Play");
        btnStop = crearBotonControl("■ Stop");
        btnPlayPause.addActionListener(e -> {
            if (reproduciendo && !enPausa) pausar();
            else if (enPausa) continuar();
            else abrirVentanaBiblioteca();
        });
        btnStop.addActionListener(e -> {
            detener();
            sldProgreso.setValue(0);
            lblTiempo.setText("00:00");
            btnPlayPause.setText("▶ Play");
        });
        controles.add(btnPlayPause);
        controles.add(btnStop);

        JPanel progreso = new JPanel(new BorderLayout());
        progreso.setBackground(COLOR_PANEL);
        lblTiempo = new JLabel("00:00");
        lblTiempo.setForeground(COLOR_SUBTEXT);
        lblTiempo.setFont(new Font("Consolas", Font.PLAIN, 12));
        lblTotal = new JLabel("00:00", SwingConstants.RIGHT);
        lblTotal.setForeground(COLOR_SUBTEXT);
        lblTotal.setFont(new Font("Consolas", Font.PLAIN, 12));

        sldProgreso = new JSlider(0,0,0);
        sldProgreso.setBackground(COLOR_PANEL);
        sldProgreso.setForeground(COLOR_ACCENT);
        sldProgreso.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e){ arrastrando = true; }
            @Override public void mouseReleased(MouseEvent e){
                arrastrando = false;
                if (archivoActual != null && sldProgreso.isEnabled()) irA(sldProgreso.getValue());
            }
        });
        sldProgreso.addChangeListener(e -> { if (arrastrando) lblTiempo.setText(formato(sldProgreso.getValue())); });

        progreso.add(lblTiempo, BorderLayout.WEST);
        progreso.add(sldProgreso, BorderLayout.CENTER);
        progreso.add(lblTotal, BorderLayout.EAST);

        playerBar.add(controles, BorderLayout.WEST);
        playerBar.add(progreso, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(playerBar, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void abrirVentanaBiblioteca() {
        JFrame f = new JFrame("Biblioteca");
        f.setSize(720, 560);
        f.setLocationRelativeTo(this);
        f.getContentPane().setBackground(COLOR_BG);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(10,10));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(14,14,14,14));

        JPanel top = new JPanel(new BorderLayout(10,10));
        top.setBackground(COLOR_PANEL);
        top.setBorder(new EmptyBorder(10,12,10,12));

        JLabel t = new JLabel("Tu biblioteca");
        t.setForeground(COLOR_TEXT);
        t.setFont(new Font("Arial", Font.BOLD, 16));

        JTextField tfBuscar = new JTextField();
        tfBuscar.setBackground(new Color(36,36,36));
        tfBuscar.setForeground(COLOR_TEXT);
        tfBuscar.setCaretColor(COLOR_TEXT);
        tfBuscar.setBorder(BorderFactory.createEmptyBorder(6,8,6,8));

        top.add(t, BorderLayout.WEST);
        top.add(tfBuscar, BorderLayout.CENTER);

        DefaultListModel<Musica> modelo = new DefaultListModel<>();
        JList<Musica> listaUI = new JList<>(modelo);
        listaUI.setBackground(COLOR_CARD);
        listaUI.setSelectionBackground(new Color(45,45,45));
        listaUI.setFixedCellHeight(78);
        listaUI.setCellRenderer(new RendererCancion());
        JScrollPane scroll = new JScrollPane(listaUI);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottom.setBackground(COLOR_PANEL);
        bottom.setBorder(new EmptyBorder(10,12,10,12));

        JButton btnAgregar = crearBotonAccent("Agregar");
        JButton btnEliminar = crearBotonSimple("Eliminar");
        JButton btnSeleccionar = crearBotonAccent("Reproducir");

        bottom.add(btnAgregar);
        bottom.add(btnEliminar);
        bottom.add(btnSeleccionar);

        cargarModeloEn(modelo);

        tfBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e){ filtrarEn(modelo, tfBuscar.getText()); }
            public void removeUpdate(DocumentEvent e){ filtrarEn(modelo, tfBuscar.getText()); }
            public void changedUpdate(DocumentEvent e){ filtrarEn(modelo, tfBuscar.getText()); }
        });

        listaUI.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Musica m = listaUI.getSelectedValue();
                    if (m != null) { reproducirCancionDesdeBiblioteca(m); f.dispose(); }
                }
            }
        });
        listaUI.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    Musica m = listaUI.getSelectedValue();
                    if (m != null) { reproducirCancionDesdeBiblioteca(m); f.dispose(); }
                }
            }
        });

        btnSeleccionar.addActionListener(e -> {
            Musica m = listaUI.getSelectedValue();
            if (m == null) { JOptionPane.showMessageDialog(f, "Selecciona una canción."); return; }
            reproducirCancionDesdeBiblioteca(m);
            f.dispose();
        });

        btnEliminar.addActionListener(e -> {
            int idx = listaUI.getSelectedIndex();
            if (idx < 0) { JOptionPane.showMessageDialog(f, "Selecciona una canción."); return; }
            Musica s = modelo.get(idx);
            if (cancionActual != null && s.getTitulo().equals(cancionActual.getTitulo())) detener();
            lista.eliminarCancion(idx);
            cargarModeloEn(modelo);
        });

        btnAgregar.addActionListener(e -> abrirDialogoAgregar(() -> cargarModeloEn(modelo)));

        root.add(top, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        f.add(root);
        f.setVisible(true);
    }

    private void cargarModeloEn(DefaultListModel<Musica> modelo){
        modelo.clear();
        Cabecera c = lista.cabeza;
        while (c != null) { modelo.addElement(c.musica); c = c.siguiente; }
    }
    private void filtrarEn(DefaultListModel<Musica> modelo, String q){
        q = q.toLowerCase();
        modelo.clear();
        Cabecera c = lista.cabeza;
        while (c != null) {
            Musica m = c.musica;
            String s = (m.getTitulo()+" "+m.getArtista()+" "+m.getGenero()).toLowerCase();
            if (s.contains(q)) modelo.addElement(m);
            c = c.siguiente;
        }
    }
    private void reproducirCancionDesdeBiblioteca(Musica m){
        cancionActual = m;
        archivoActual = new File(m.getRuta());
        lblTitulo.setText(m.getTitulo());
        lblArtista.setText(m.getArtista() + " • " + m.getGenero());
        if (m.getCoverPath() != null && !m.getCoverPath().isEmpty()){
            Image img = new ImageIcon(m.getCoverPath()).getImage().getScaledInstance(260,260, Image.SCALE_SMOOTH);
            lblCover.setIcon(new ImageIcon(img));
            lblCover.setText(null);
        } else {
            lblCover.setIcon(null);
            lblCover.setText("Sin portada");
        }
        sldProgreso.setMaximum(Math.max(0, m.getDuracion()));
        sldProgreso.setValue(0);
        lblTiempo.setText("00:00");
        lblTotal.setText(formato(m.getDuracion()));
        sldProgreso.setEnabled(true);
        reproducir(archivoActual);
    }

    private interface OnAdded { void run(); }
    private void abrirDialogoAgregar(OnAdded callback) {
        JFrame f = new JFrame("Agregar canción");
        f.setSize(420, 560);
        f.setLocationRelativeTo(this);
        f.getContentPane().setBackground(COLOR_PANEL);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel p = new JPanel();
        p.setBackground(COLOR_PANEL);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(14,14,14,14));

        JLabel t = new JLabel("Agregar canción");
        t.setAlignmentX(CENTER_ALIGNMENT);
        t.setForeground(COLOR_TEXT);
        t.setFont(new Font("Arial", Font.BOLD, 16));
        p.add(t); p.add(Box.createRigidArea(new Dimension(0,10)));

        JTextField tfNom = campoTexto();
        JTextField tfArt = campoTexto();
        JTextField tfGen = campoTexto();
        JTextField tfDur = campoTexto();

        p.add(filaForm("Título:", tfNom));
        p.add(filaForm("Artista:", tfArt));
        p.add(filaForm("Género:", tfGen));

        final String[] rutaImg = {null};
        final String[] rutaMp3 = {null};

        JPanel filaImg = filaForm("Portada:", new JLabel());
        JButton bImg = crearBotonSimple("Elegir imagen");
        bImg.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imágenes (PNG, JPG)","png","jpg","jpeg"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                rutaImg[0] = fc.getSelectedFile().getAbsolutePath();
                JOptionPane.showMessageDialog(this, "Imagen: " + fc.getSelectedFile().getName());
            }
        });
        filaImg.add(bImg);
        p.add(filaImg);

        p.add(filaForm("Duración (seg):", tfDur));

        JPanel filaMp3 = filaForm("Archivo MP3:", new JLabel());
        JButton bMp3 = crearBotonSimple("Elegir MP3");
        bMp3.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("MP3 (*.mp3)","mp3"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                rutaMp3[0] = fc.getSelectedFile().getAbsolutePath();
                JOptionPane.showMessageDialog(this, "MP3: " + fc.getSelectedFile().getName());
            }
        });
        filaMp3.add(bMp3);
        p.add(filaMp3);

        JButton ok = crearBotonAccent("Agregar");
        ok.setAlignmentX(CENTER_ALIGNMENT);
        ok.addActionListener(e -> {
            String nom = tfNom.getText().trim();
            String art = tfArt.getText().trim();
            String gen = tfGen.getText().trim();
            String d   = tfDur.getText().trim();
            int dur;
            try { dur = Integer.parseInt(d); }
            catch(Exception ex){ JOptionPane.showMessageDialog(this,"Duración inválida."); return; }
            if (nom.isEmpty()||art.isEmpty()||gen.isEmpty()||rutaImg[0]==null||rutaMp3[0]==null){
                JOptionPane.showMessageDialog(this,"Completa todos los campos."); return;
            }
            lista.agregarCancion(nom, art, rutaImg[0], dur, rutaMp3[0], gen);
            if (callback != null) callback.run();
            f.dispose();
        });

        p.add(Box.createRigidArea(new Dimension(0,10)));
        p.add(ok);

        f.add(p);
        f.setVisible(true);
    }

    private JTextField campoTexto(){
        JTextField tf = new JTextField(20);
        tf.setBackground(new Color(36,36,36));
        tf.setForeground(COLOR_TEXT);
        tf.setCaretColor(COLOR_TEXT);
        tf.setBorder(BorderFactory.createEmptyBorder(6,8,6,8));
        return tf;
    }
    private JPanel filaForm(String etiqueta, JComponent comp){
        JPanel r = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        r.setBackground(COLOR_PANEL);
        JLabel l = new JLabel(etiqueta);
        l.setForeground(COLOR_TEXT);
        r.add(l); r.add(comp);
        return r;
    }

    private void reproducir(File f){
        detener();
        if (f==null || !f.exists()){ JOptionPane.showMessageDialog(this,"El archivo no existe."); return; }
        try{
            player = new BasicPlayer();
            player.addBasicPlayerListener(this);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
            player.open(bis);
            try { player.setGain(0.95); } catch(Exception ignored){}
            try { player.setPan(0.0);   } catch(Exception ignored){}
            player.play();
            reproduciendo = true; enPausa = false;
            btnPlayPause.setText("⏸ Pause");
            sldProgreso.setEnabled(true);
        }catch(Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,"No se pudo reproducir el archivo.","Error",JOptionPane.ERROR_MESSAGE);
        }
    }
    private void pausar(){
        if (player==null) return;
        try{ player.pause(); enPausa = true; reproduciendo = false; btnPlayPause.setText("▶ Play"); }
        catch(BasicPlayerException ex){ ex.printStackTrace(); }
    }
    private void continuar(){
        if (player==null) return;
        try{ player.resume(); enPausa = false; reproduciendo = true; btnPlayPause.setText("⏸ Pause"); }
        catch(BasicPlayerException ex){ ex.printStackTrace(); }
    }
    private void detener(){
        if (player!=null){
            try { player.stop(); } catch(Exception ignore){}
            player.removeBasicPlayerListener(this);
            player = null;
        }
        reproduciendo=false; enPausa=false;
        duracionMs=-1; totalBytes=-1;
    }
    private void irA(int segundos){
        if (player==null || archivoActual==null) return;
        try{
            int total = getDuracionSeg();
            int s = Math.max(0, Math.min(segundos, total));
            long objetivoMs = s * 1000L;
            long bytes = msToBytes(objetivoMs);
            if (bytes >= 0){
                player.seek(bytes);
                sldProgreso.setValue(s);
                lblTiempo.setText(formato(s));
                if (!reproduciendo) continuar();
            }
        }catch(BasicPlayerException ex){ ex.printStackTrace(); }
    }
    private int getDuracionSeg(){
        if (duracionMs>0) return (int)(duracionMs/1000L);
        return (cancionActual!=null)? cancionActual.getDuracion() : 0;
    }
    private long msToBytes(long ms){
        if (duracionMs>0 && totalBytes>0) return (totalBytes*ms)/duracionMs;
        long fb = (archivoActual!=null)? archivoActual.length() : -1;
        long d  = Math.max(1, (long)getDuracionSeg()*1000L);
        if (fb>0) return (fb*ms)/d;
        return -1;
    }

    @Override
    public void opened(Object stream, Map props) {
        audioInfo.clear();
        if (props!=null) audioInfo.putAll(props);
        Object dur = audioInfo.get("duration");
        if (dur instanceof Long){
            duracionMs = (Long) dur;
            int s = (int)(duracionMs/1000);
            sldProgreso.setMaximum(s);
            lblTotal.setText(formato(s));
        } else {
            duracionMs = (cancionActual!=null)? cancionActual.getDuracion()*1000L : -1;
            sldProgreso.setMaximum(getDuracionSeg());
            lblTotal.setText(formato(getDuracionSeg()));
        }
        Object len = audioInfo.get("mp3.length.bytes");
        if (len instanceof Long) totalBytes = (Long)len;
        else {
            Object l2 = audioInfo.get("audio.length.bytes");
            totalBytes = (l2 instanceof Long)? (Long)l2 : (archivoActual!=null? archivoActual.length():-1);
        }
    }
    @Override
    public void progress(int bytesread, long microseconds, byte[] pcmdata, Map props) {
        if (!arrastrando){
            int s = (int)(microseconds/1_000_000L);
            s = Math.max(0, Math.min(s, getDuracionSeg()));
            sldProgreso.setValue(s);
            lblTiempo.setText(formato(s));
        }
    }
    @Override
    public void stateUpdated(BasicPlayerEvent e) {
        int code = e.getCode();
        if (code == BasicPlayerEvent.EOM){
            reproduciendo=false; enPausa=false;
            btnPlayPause.setText("▶ Play");
            sldProgreso.setValue(getDuracionSeg());
            lblTiempo.setText(formato(getDuracionSeg()));
        } else if (code == BasicPlayerEvent.PAUSED){
            enPausa=true; reproduciendo=false; btnPlayPause.setText("▶ Play");
        } else if (code == BasicPlayerEvent.RESUMED || code == BasicPlayerEvent.PLAYING){
            enPausa=false; reproduciendo=true; btnPlayPause.setText("⏸ Pause");
        } else if (code == BasicPlayerEvent.STOPPED){
            enPausa=false; reproduciendo=false; btnPlayPause.setText("▶ Play");
        }
    }
    @Override public void setController(BasicController c) {}

    private JButton crearBotonHeader(String txt){
        JButton b = new JButton(txt);
        b.setBackground(COLOR_ACCENT);
        b.setForeground(Color.BLACK);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    private JButton crearBotonControl(String txt){
        JButton b = new JButton(txt);
        b.setBackground(new Color(45,45,45));
        b.setForeground(COLOR_TEXT);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    private JButton crearBotonAccent(String txt){
        JButton b = new JButton(txt);
        b.setBackground(COLOR_ACCENT);
        b.setForeground(Color.BLACK);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    private JButton crearBotonSimple(String txt){
        JButton b = new JButton(txt);
        b.setBackground(new Color(45,45,45));
        b.setForeground(COLOR_TEXT);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    private static String formato(int s){
        int m = Math.max(0, s)/60;
        int ss = Math.max(0, s)%60;
        return String.format("%02d:%02d", m, ss);
    }

    private class RendererCancion extends JPanel implements ListCellRenderer<Musica> {
        private final JLabel cover = new JLabel("Sin portada", SwingConstants.CENTER);
        private final JLabel t = new JLabel();
        private final JLabel sub = new JLabel();

        RendererCancion(){
            setLayout(new BorderLayout(10,10));
            setBorder(new EmptyBorder(10,12,10,12));
            setBackground(COLOR_CARD);
            cover.setPreferredSize(new Dimension(56,56));
            cover.setOpaque(true);
            cover.setBackground(new Color(40,40,40));
            cover.setForeground(COLOR_SUBTEXT);
            cover.setBorder(BorderFactory.createLineBorder(new Color(55,55,55)));
            t.setFont(new Font("Arial", Font.BOLD, 14));
            t.setForeground(COLOR_TEXT);
            sub.setFont(new Font("Arial", Font.PLAIN, 12));
            sub.setForeground(COLOR_SUBTEXT);
            JPanel txt = new JPanel();
            txt.setOpaque(false);
            txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
            txt.add(t); txt.add(sub);
            add(cover, BorderLayout.WEST);
            add(txt, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Musica> list, Musica m, int index, boolean isSelected, boolean cellHasFocus) {
            if (m.getCoverPath()!=null && !m.getCoverPath().isEmpty()){
                Image img = new ImageIcon(m.getCoverPath()).getImage().getScaledInstance(56,56, Image.SCALE_SMOOTH);
                cover.setIcon(new ImageIcon(img));
                cover.setText(null);
            } else {
                cover.setIcon(null);
                cover.setText("Sin portada");
            }
            t.setText(m.getTitulo());
            sub.setText(m.getArtista() + " • " + m.getGenero() + " • " + formato(m.getDuracion()));
            setBackground(isSelected ? new Color(50,50,50) : COLOR_CARD);
            return this;
        }
    }

    public static void main(String[] args) {
        new Reproductor();
    }
}
