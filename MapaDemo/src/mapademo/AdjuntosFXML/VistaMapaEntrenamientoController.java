/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo.AdjuntosFXML;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.chart.AreaChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import mapademo.Poi;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.Annotation;
import upv.ipc.sportlib.TrackPoint;
import util2.AppContext;

/**
 * FXML Controller class
 *
 * @author dennis
 */
public class VistaMapaEntrenamientoController implements Initializable {

    @FXML
    private ScrollPane ScrollPane;
    @FXML
    private Group Group;
    @FXML
    private ImageView mapa;
    @FXML
    private Button botonSumar;
    @FXML
    private Button botonRestar;
    @FXML
    private Slider Slider;
    @FXML
    private AreaChart<?, ?> grafica;
    private Group zoomGroup;
    private Pane mapPane;
    private Polyline rutaVisual;
    private Circle marcadorIndicador; //
    @FXML
    private Label mousePositionLabel;
    private ContextMenu mapContextMenu;

    /** Indica si el controlador está en modo inserción de POI. */
    private boolean insertionMode = false;

    /** Lista en memoria para almacenar los POIs (sustituye al ListView ausente en esta vista). */
    private ObservableList<Poi> listaPois = FXCollections.observableArrayList();
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
      // ── Configuración del Slider de Zoom ──────────────────────────
        Slider.setMin(0.5);   // zoom mínimo: 50 %
        Slider.setMax(1.5);   // zoom máximo: 150 %
        Slider.setValue(1.0); // valor inicial: 100 %

        // Listener para invocar la función de escalado cuando el slider cambie
        Slider.valueProperty().addListener(
            (observable, oldVal, newVal) -> zoom((Double) newVal)
        );

        // ── Configuración de los botones de la interfaz (+ / -) ───────
        botonSumar.setOnAction(e -> zoomIn(null));
        botonRestar.setOnAction(e -> zoomOut(null));

        // ── Creación del Menú Contextual (Clic derecho) ───────────────
        MenuItem miText   = new MenuItem("📝 Añadir texto (POI)");
        MenuItem miCircle = new MenuItem("⭕ Añadir círculo");
        mapContextMenu = new ContextMenu(miText, miCircle);

        // ── Carga y construcción del mapa inicial ─────────────────────
        // Intenta cargar la imagen por defecto
        buildMap(new File("maps/upv.jpg"));
    }
private void zoomIn(ActionEvent event) {
        double sliderVal = Slider.getValue();
        Slider.setValue(sliderVal + 0.1);
    }

    /** Reduce el zoom en 0.1 unidades. */
    private void zoomOut(ActionEvent event) {
        double sliderVal = Slider.getValue();
        Slider.setValue(sliderVal - 0.1);
    }

    /** Aplica el factor de escala al contenedor Group. */
    private void zoom(double scaleValue) {
        // Guardamos la posición del scroll antes de escalar
        double scrollH = ScrollPane.getHvalue();
        double scrollV = ScrollPane.getVvalue();

        // Aplicamos el zoom escalando el Group en ambos ejes
        if (Group != null) {
            Group.setScaleX(scaleValue);
            Group.setScaleY(scaleValue);
        }

        // Restauramos la posición del scroll para mantener la estabilidad visual
        ScrollPane.setHvalue(scrollH);
        ScrollPane.setVvalue(scrollV);
    }

    // =========================================================
    //  CONSTRUCCIÓN DINÁMICA DEL MAPA
    // =========================================================

    /**
     * Reestructura los nodos del ScrollPane para permitir la superposición de elementos.
     * @param imgFile Fichero de la imagen del mapa.
     */
    private void buildMap(File imgFile) {
        if (!imgFile.exists()) {
            ScrollPane.setContent(new Label("Imagen no encontrada: " + imgFile.getPath()));
            return;
        }

        // Cargamos la nueva imagen
        Image img = new Image(imgFile.toURI().toString());
        double W = img.getWidth();
        double H = img.getHeight();

        // Configurar el ImageView que ya tenemos inyectado por FXML
        mapa.setImage(img);
        mapa.setFitWidth(W);
        mapa.setFitHeight(H);

        // Creamos el mapPane (lienzo) y le metemos el mapa de fondo
        mapPane = new Pane();
        mapPane.setPrefSize(W, H);
        mapPane.setMinSize(W, H);
        mapPane.setMaxSize(W, H);
        mapPane.getChildren().add(mapa);

        // Asignamos los eventos de clic al lienzo del mapa
        mapPane.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                // Clic derecho -> Menú contextual
                onMapRightClick(e.getX(), e.getY());
            } else if (e.getButton() == MouseButton.PRIMARY && insertionMode) {
                // Modo inserción activado -> Añadir POI directo con clic izquierdo
                insertionMode = false;
                mapPane.setStyle(""); // Restauramos el cursor
                addPoi(e.getX(), e.getY());
            }
        });

        // Evento opcional para rastrear el movimiento del ratón
        mapPane.setOnMouseMoved(this::showPosition);

        // Limpiamos el Group original de SceneBuilder y añadimos nuestro panel interactivo
        Group.getChildren().clear();
        Group.getChildren().add(mapPane);

        // Sincronizamos el zoom actual del slider
        double currentZoom = Slider.getValue();
        Group.setScaleX(currentZoom);
        Group.setScaleY(currentZoom);

        // Aseguramos que el Group raíz esté asignado al ScrollPane
        ScrollPane.setContent(Group);
    }

    // =========================================================
    //  GESTIÓN DE MENÚ CONTEXTUAL Y EVENTOS DE RATÓN
    // =========================================================

    /** Muestra el menú contextual en la posición exacta del clic. */
    private void onMapRightClick(double x, double y) {
        mapContextMenu.hide();

        final double clickX = x;
        final double clickY = y;
        mapContextMenu.getItems().get(0).setOnAction(e -> addPoi(clickX, clickY));
        mapContextMenu.getItems().get(1).setOnAction(e -> addCircle(clickX, clickY));

        // Mostrar en coordenadas de pantalla
        mapContextMenu.show(
            mapPane.getScene().getWindow(),
            mapPane.localToScreen(x, y).getX(),
            mapPane.localToScreen(x, y).getY()
        );
    }

    /** Muestra o registra las coordenadas actuales del ratón (Manejador de MouseEvent). */
    private void showPosition(MouseEvent event) {
        // Formatea el texto de la posición (puedes usarlo en un Tooltip o Label de UI)
        String textoPosicion = "sceneX: " + (int) event.getSceneX() +
                               ", sceneY: " + (int) event.getSceneY() + "\n" +
                               "     X: " + (int) event.getX() +
                               ",      Y: " + (int) event.getY();
        mousePositionLabel.setText(textoPosicion);
    }

    // =========================================================
    //  MÉTODOS DE INSERCIÓN DE ELEMENTOS (POIs y Formas)
    // =========================================================

    /** Abre el diálogo interactivo para crear un nuevo POI de texto en el mapa. */
    private void addPoi(double x, double y) {
        Dialog<Poi> poiDialog = new Dialog<>();
        poiDialog.setTitle("Nuevo POI");
        poiDialog.setHeaderText("Introduce un nuevo POI");

        // Configuración de botones
        ButtonType okButton = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        poiDialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Nombre del POI");

        VBox vbox = new VBox(10, new Label("Nombre:"), nameField);
        poiDialog.getDialogPane().setContent(vbox);

        // Conversor de resultados al modelo Poi
        poiDialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                return new Poi(nameField.getText().trim(), x, y);
            }
            return null;
        });

        Optional<Poi> result = poiDialog.showAndWait();

        if (result.isPresent()) {
            Poi poi = result.get();
            poi.setPosition(new Point2D(x, y));

            // Guardamos el POI en nuestra lista en memoria
            listaPois.add(poi);

            // Dibujamos el texto del código de POI de manera absoluta sobre el lienzo
            Text text = new Text(poi.getCode());
            text.setX(x);
            text.setY(y);
            // Opcional: estilizar el texto para que resalte en el mapa
            text.setFill(Color.BLACK);
            mapPane.getChildren().add(text);
        }
    }

    /** Dibuja un círculo rojo de radio 10px sobre el mapa. */
    private void addCircle(double x, double y) {
        Circle circle = new Circle(10, Color.RED); 
        circle.setCenterX(x);
        circle.setCenterY(y);
        mapPane.getChildren().add(circle); 
    }

    // =========================================================
    //  FUNCIONALIDADES SUPLENTES / UTILIDADES
    // =========================================================

    /** Permite activar externamente el modo de inserción directa por botón. */
    public void activarModoInsercion() {
        this.insertionMode = true;
        if (mapPane != null) {
            // Cambia el cursor a una cruz para indicar el modo inserción
            mapPane.setStyle("-fx-cursor: crosshair;"); 
        }
    }

    /** Cambiar dinámicamente el mapa de fondo mediante FileChooser. */
    @FXML
    private void cambiarMapaDesdeArchivo(ActionEvent event) throws IOException {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File(".")); 

        File imgFile = fc.showOpenDialog(Slider.getScene().getWindow());

        if (imgFile != null) {
            buildMap(imgFile); 
            listaPois.clear(); // Limpiamos los POIs del mapa anterior
        }
    }

    /** Centra de manera animada el mapa sobre las coordenadas de un POI concreto. */
    public void centrarEnPoi(Poi poi) {
        if (poi == null || mapPane == null) return;

        double mapWidth  = mapPane.getWidth()  * Group.getScaleX();
        double mapHeight = mapPane.getHeight() * Group.getScaleY();

        double poiX = poi.getPosition().getX() * Group.getScaleX();
        double poiY = poi.getPosition().getY() * Group.getScaleY();

        double viewW = ScrollPane.getViewportBounds().getWidth();
        double viewH = ScrollPane.getViewportBounds().getHeight();

        double scrollH = (poiX - viewW / 2) / (mapWidth  - viewW);
        double scrollV = (poiY - viewH / 2) / (mapHeight - viewH);

        scrollH = Math.max(0, Math.min(1, scrollH));
        scrollV = Math.max(0, Math.min(1, scrollV));

        // Animación de desplazamiento suave de 500ms
        final Timeline timeline = new Timeline();
        final KeyValue kv1 = new KeyValue(ScrollPane.hvalueProperty(), scrollH);
        final KeyValue kv2 = new KeyValue(ScrollPane.vvalueProperty(), scrollV);
        final KeyFrame kf  = new KeyFrame(Duration.millis(500), kv1, kv2);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }

    /** Muestra el diálogo de información de la asignatura ("Acerca de"). */
    @FXML
    private void mostrarAcercaDe(ActionEvent event) {
        Alert mensaje = new Alert(Alert.AlertType.INFORMATION);
        mensaje.setTitle("Acerca de");
        mensaje.setHeaderText("IPC - 2026");
        mensaje.setContentText("Aplicación de Entrenamiento - Running la Safor UPV");
        mensaje.showAndWait();
    }
}
