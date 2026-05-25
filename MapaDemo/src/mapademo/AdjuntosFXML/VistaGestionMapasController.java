package mapademo.AdjuntosFXML;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

// Clases de la librería IPC2026
import upv.ipc.sportlib.MapRegion;
import upv.ipc.sportlib.SportActivityApp;

/**
 * VistaGestionMapasController
 *
 * Controla la vista de gestión de mapas. Gestiona:
 *   - Escenario 7.1: Añadir un nuevo mapa JPG al sistema con su Bounding Box.
 *
 * PUNTOS CLAVE:
 *
 * 1. MÁSCARAS NUMÉRICAS (Bounding Box):
 *    Cada TextField de coordenada tiene un listener en su textProperty que rechaza
 *    cualquier carácter que no sea dígito, punto decimal o signo negativo (−).
 *    El signo solo se permite como primer carácter. Esto impide al usuario introducir
 *    texto o símbolos inválidos desde el teclado, sin bloquear el pegado de texto
 *    (el listener filtra igualmente el texto pegado carácter a carácter).
 *
 * 2. VALIDACIÓN DE RANGO:
 *    Al pulsar "Añadir mapa" se comprueban los rangos geográficos válidos:
 *      - latMin ∈ [−90, 90], latMax ∈ [−90, 90], latMin < latMax
 *      - lonMin ∈ [−180, 180], lonMax ∈ [−180, 180], lonMin < lonMax
 *    Y se avisa al usuario de que debe usar los valores exactos del script Python.
 *
 * 3. PERSISTENCIA:
 *    Se delega en app.addMapRegion() que copia el JPG a maps/ e inserta en la BD.
 */
public class VistaGestionMapasController implements Initializable {

    // ── FXML ──────────────────────────────────────────────────────────────────

    // Lista izquierda
    @FXML private ListView<MapRegion> listMapas;
    @FXML private ImageView previsualizacion;
    @FXML private Button btnEliminarMapa;

    // Formulario centro
    @FXML private TextField txtNombre;
    @FXML private TextField txtRutaImagen;
    @FXML private TextField txtLatMin;
    @FXML private TextField txtLatMax;
    @FXML private TextField txtLonMin;
    @FXML private TextField txtLonMax;

    // Labels de error / resultado
    @FXML private Label errNombre;
    @FXML private Label errImagen;
    @FXML private Label errCoordenadas;
    @FXML private Label lblResultado;

    @FXML private Button btnVolver;

    // ── ESTADO ────────────────────────────────────────────────────────────────

    private final SportActivityApp app = SportActivityApp.getInstance();

    /** Referencia al MainViewController para navegar de vuelta. */
    private mapademo.AdjuntosFXML.MainViewController mainController;

    /** Fichero JPG seleccionado con el FileChooser. */
    private File ficheroImagenSeleccionado;

    // ── INICIALIZACIÓN ────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        aplicarMascarasNumericas();
        cargarListaMapas();
        configurarSeleccionLista();
    }

    // ── MÁSCARAS NUMÉRICAS ────────────────────────────────────────────────────

    /**
     * Escenario 7.1 — Máscaras numéricas en los inputs del Bounding Box.
     *
     * Estrategia:
     *   Añadir un ChangeListener al textProperty de cada TextField.
     *   Cuando el nuevo valor no cumple el patrón numérico se revierte al valor anterior.
     *
     * Patrón permitido: −?(0-9)*(\.?(0-9)*)
     *   → Signo opcional solo al inicio.
     *   → Dígitos, un punto decimal opcional seguido de más dígitos.
     *   → Se permite el string vacío (campo en blanco).
     *
     * El listener se aplica a los cuatro campos de coordenada.
     */
    private void aplicarMascarasNumericas() {
        for (TextField campo : List.of(txtLatMin, txtLatMax, txtLonMin, txtLonMax)) {
            campo.textProperty().addListener(crearListenerMascara(campo));
        }
    }

    /**
     * Crea el ChangeListener de máscara numérica para un TextField concreto.
     * Si el nuevo texto no cumple el patrón, lo revierte al valor anterior.
     *
     * @param campo TextField al que se aplicará la máscara.
     * @return Listener listo para añadir al textProperty.
     */
    private ChangeListener<String> crearListenerMascara(TextField campo) {
        return (observable, valorAnterior, valorNuevo) -> {
            if (valorNuevo == null || valorNuevo.isEmpty()) return;

            // Patrón: signo opcional + dígitos + punto opcional + dígitos
            // Permite estados intermedios durante la escritura: "−", "3.", "−0."
            if (!esTextoNumericoValido(valorNuevo)) {
                // Revertir sin volver a disparar el listener
                campo.setText(valorAnterior);
            }
        };
    }

    /**
     * Comprueba si un String cumple el patrón de número decimal con signo opcional.
     * Se permiten estados intermedios de escritura (ej. "−", "3.", "−0.").
     *
     * @param texto Texto a validar.
     * @return true si el texto es un número decimal válido o estado intermedio.
     */
    private boolean esTextoNumericoValido(String texto) {
        // Regex: signo opcional, dígitos opcionales, punto opcional, dígitos opcionales
        // Ejemplos válidos: "", "−", "39", "39.", "39.334", "−0.505371"
        return texto.matches("^-?\\d*\\.?\\d*$");
    }

    // ── LISTA DE MAPAS ────────────────────────────────────────────────────────

    /** Carga la lista de MapRegion registradas en la BD y las muestra en el ListView. */
    private void cargarListaMapas() {
        List<MapRegion> regiones = app.getMapRegions();
        ObservableList<MapRegion> items = FXCollections.observableArrayList(regiones);
        listMapas.setItems(items);

        // Mostrar solo el nombre en cada celda
        listMapas.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(MapRegion region, boolean empty) {
                super.updateItem(region, empty);
                setText(empty || region == null ? null : region.getName());
            }
        });
    }

    /**
     * Configura el listener de selección del ListView.
     * Al seleccionar un mapa: muestra la previsualización y activa el botón eliminar.
     */
    private void configurarSeleccionLista() {
        listMapas.getSelectionModel().selectedItemProperty().addListener(
            (obs, anterior, seleccionada) -> {
                if (seleccionada == null) {
                    previsualizacion.setImage(null);
                    btnEliminarMapa.setDisable(true);
                } else {
                    cargarPrevisualizacion(seleccionada);
                    // Solo se puede eliminar si la región no está en uso
                    boolean sinUso = app.getUnusedMapRegions().contains(seleccionada);
                    btnEliminarMapa.setDisable(!sinUso);
                }
            }
        );
    }

    /** Carga la imagen del mapa seleccionado en el ImageView de previsualización. */
    private void cargarPrevisualizacion(MapRegion region) {
        try {
            File fichero = new File(region.getImagePath());
            if (fichero.exists()) {
                previsualizacion.setImage(new Image(fichero.toURI().toString()));
            } else {
                previsualizacion.setImage(null);
            }
        } catch (Exception ex) {
            previsualizacion.setImage(null);
        }
    }

    // ── SELECCIÓN DE IMAGEN ───────────────────────────────────────────────────

    /**
     * Abre un FileChooser para que el usuario seleccione el fichero JPG del mapa.
     * Solo acepta ficheros .jpg y .jpeg.
     */
    @FXML
    private void handleSeleccionarImagen() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar imagen del mapa");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Imágenes JPG", "*.jpg", "*.jpeg")
        );
        // Abrir en el directorio del proyecto si existe
        File dirMaps = new File("maps");
        if (dirMaps.exists()) chooser.setInitialDirectory(dirMaps);

        File seleccionado = chooser.showOpenDialog(txtRutaImagen.getScene().getWindow());
        if (seleccionado != null) {
            ficheroImagenSeleccionado = seleccionado;
            txtRutaImagen.setText(seleccionado.getAbsolutePath());
            ocultarError(errImagen);
        }
    }

    // ── AÑADIR MAPA ───────────────────────────────────────────────────────────

    /**
     * Escenario 7.1 — Valida el formulario y llama a app.addMapRegion().
     *
     * Orden de validación:
     *   1. Nombre no vacío.
     *   2. Fichero JPG seleccionado.
     *   3. Los cuatro campos de coordenada son números parseables.
     *   4. Rangos geográficos válidos (lat ∈ [−90,90], lon ∈ [−180,180]).
     *   5. latMin < latMax y lonMin < lonMax.
     *
     * Si todo es correcto: llama a addMapRegion, recarga la lista y limpia el formulario.
     */
    @FXML
    private void handleAnadirMapa() {
        ocultarTodosLosErrores();
        boolean valido = true;

        // ── 1. Nombre ──
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            mostrarError(errNombre, "El nombre de la región es obligatorio.");
            valido = false;
        }

        // ── 2. Imagen ──
        if (ficheroImagenSeleccionado == null || !ficheroImagenSeleccionado.exists()) {
            mostrarError(errImagen, "Selecciona un fichero JPG válido.");
            valido = false;
        }

        // ── 3 y 4. Coordenadas ──
        double latMin = 0, latMax = 0, lonMin = 0, lonMax = 0;
        boolean coordsOk = true;

        try { latMin = Double.parseDouble(txtLatMin.getText().trim()); }
        catch (NumberFormatException e) { coordsOk = false; }

        try { latMax = Double.parseDouble(txtLatMax.getText().trim()); }
        catch (NumberFormatException e) { coordsOk = false; }

        try { lonMin = Double.parseDouble(txtLonMin.getText().trim()); }
        catch (NumberFormatException e) { coordsOk = false; }

        try { lonMax = Double.parseDouble(txtLonMax.getText().trim()); }
        catch (NumberFormatException e) { coordsOk = false; }

        if (!coordsOk) {
            mostrarError(errCoordenadas,
                "Todos los campos de coordenada deben contener números decimales válidos.");
            valido = false;
        } else {
            // ── 5. Rangos y orden ──
            StringBuilder sbError = new StringBuilder();

            if (latMin < -90 || latMin > 90)
                sbError.append("Lat. mínima debe estar entre −90 y 90.\n");
            if (latMax < -90 || latMax > 90)
                sbError.append("Lat. máxima debe estar entre −90 y 90.\n");
            if (latMin >= latMax)
                sbError.append("Lat. mínima debe ser menor que la máxima.\n");
            if (lonMin < -180 || lonMin > 180)
                sbError.append("Lon. mínima debe estar entre −180 y 180.\n");
            if (lonMax < -180 || lonMax > 180)
                sbError.append("Lon. máxima debe estar entre −180 y 180.\n");
            if (lonMin >= lonMax)
                sbError.append("Lon. mínima debe ser menor que la máxima.\n");

            if (sbError.length() > 0) {
                mostrarError(errCoordenadas, sbError.toString().trim());
                valido = false;
            }
        }

        if (!valido) return;

        // ── Llamada a la librería ──
        MapRegion nueva = app.addMapRegion(
            nombre,
            ficheroImagenSeleccionado,
            latMin, latMax,
            lonMin, lonMax
        );

        if (nueva != null) {
            mostrarResultadoExito("✓ Mapa \"" + nombre + "\" añadido correctamente.");
            cargarListaMapas();
            handleLimpiarFormulario();
        } else {
            mostrarResultadoError("✗ No se pudo añadir el mapa. Comprueba los datos e inténtalo de nuevo.");
        }
    }

    // ── ELIMINAR MAPA ─────────────────────────────────────────────────────────

    /**
     * Elimina la región seleccionada si no está en uso.
     * Pide confirmación antes de borrar.
     */
    @FXML
    private void handleEliminarMapa() {
        MapRegion seleccionada = listMapas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) return;

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar mapa");
        confirmacion.setHeaderText("¿Eliminar el mapa \"" + seleccionada.getName() + "\"?");
        confirmacion.setContentText(
            "Se eliminarán el registro y el fichero de imagen asociado.\n" +
            "Esta acción no se puede deshacer."
        );

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                boolean eliminado = app.removeMapRegion(seleccionada);
                if (eliminado) {
                    cargarListaMapas();
                    previsualizacion.setImage(null);
                    btnEliminarMapa.setDisable(true);
                    mostrarResultadoExito("✓ Mapa eliminado correctamente.");
                } else {
                    mostrarResultadoError("✗ No se pudo eliminar el mapa (puede estar en uso por alguna actividad).");
                }
            }
        });
    }

    // ── LIMPIAR FORMULARIO ────────────────────────────────────────────────────

    /** Restaura todos los campos del formulario a su estado inicial. */
    @FXML
    private void handleLimpiarFormulario() {
        txtNombre.clear();
        txtRutaImagen.clear();
        txtLatMin.clear();
        txtLatMax.clear();
        txtLonMin.clear();
        txtLonMax.clear();
        ficheroImagenSeleccionado = null;
        ocultarTodosLosErrores();
    }

    // ── NAVEGACIÓN ────────────────────────────────────────────────────────────

    /**
     * Inyecta la referencia al MainViewController.
     * Llamar justo después de loader.getController() en MainViewController.
     */
    public void setMainController(mapademo.AdjuntosFXML.MainViewController mc) {
        this.mainController = mc;
    }

    @FXML
    private void handleVolver() {
        if (mainController != null) {
            mainController.mostrarHistorial();
        }
    }

    // ── UTILIDADES DE UI ─────────────────────────────────────────────────────

    private void mostrarError(Label label, String mensaje) {
        label.setText(mensaje);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void ocultarError(Label label) {
        label.setText("");
        label.setVisible(false);
        label.setManaged(false);
    }

    private void ocultarTodosLosErrores() {
        ocultarError(errNombre);
        ocultarError(errImagen);
        ocultarError(errCoordenadas);
        ocultarError(lblResultado);
    }

    private void mostrarResultadoExito(String mensaje) {
        lblResultado.setText(mensaje);
        lblResultado.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        lblResultado.setVisible(true);
        lblResultado.setManaged(true);
    }

    private void mostrarResultadoError(String mensaje) {
        lblResultado.setText(mensaje);
        lblResultado.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        lblResultado.setVisible(true);
        lblResultado.setManaged(true);
    }
}
