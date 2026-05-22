/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo.AdjuntosFXML;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.chart.AreaChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
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
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
       // 1. Configurar los controles de Zoom vinculados al Slider
        setupZoomControls();

        // 2. ESCUCHADOR REACTIVO: Cuando cambie la actividad en tu AppContext real,
        // el mapa se redibuja automáticamente de forma instantánea.
        AppContext.getInstance().actividadActualProperty().addListener((obs, viejaActividad, nuevaActividad) -> {
            if (nuevaActividad != null) {
                cargarYAnotarActividad(nuevaActividad);
            }
        });

        // 3. Carga inicial por si ya se seleccionó una actividad antes de abrir esta vista
        Activity actividadInicial = AppContext.getInstance().getActividadActual();
        if (actividadInicial != null) {
            cargarYAnotarActividad(actividadInicial);
        }
    }    
private void setupZoomControls() {
        // Enlazar de forma bidireccional la escala del Group con el valor del Slider
        Slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            Group.setScaleX(newVal.doubleValue());
            Group.setScaleY(newVal.doubleValue());
        });

        botonSumar.setOnAction(e -> {
            if (Slider.getValue() < Slider.getMax()) {
                Slider.setValue(Slider.getValue() + 0.1);
            }
        });

        botonRestar.setOnAction(e -> {
            if (Slider.getValue() > Slider.getMin()) {
                Slider.setValue(Slider.getValue() - 0.1);
            }
        });
    }
    /**
     * Coordina el zoom combinando los botones (+ / -) con la posición del Slider de JavaFX.
     */
   private void cargarYAnotarActividad(Activity actividad) {
        try {
            // Limpiar componentes dinámicos del Group para no acumular marcas de sesiones anteriores
            // Mantenemos únicamente el ImageView del mapa de fondo
            Group.getChildren().removeIf(nodo -> nodo != mapa);

            // Obtener las anotaciones reales asociadas a la actividad usando tu estructura de datos
            List<Annotation> anotaciones = actividad.getAnnotations();

            if (anotaciones != null) {
                for (Annotation anotacion : anotaciones) {
                    dibujarAnotacionEnMapa(anotacion);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al procesar las anotaciones de la actividad: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Traduce los datos puros de una Annotation del JAR a nodos visuales de JavaFX.
     */
    private void dibujarAnotacionEnMapa(Annotation anotacion) {
        // Extraer las propiedades nativas expuestas por vuestro archivo .jar
        String texto = anotacion.getText();
        String colorHex = anotacion.getColor(); // Ejemplo: "#FF0000"
        double grosor = anotacion.getStrokeWidth();
        
        // Convertir el String Hexadecimal de la base de datos a un objeto Color de JavaFX
        Color colorJavaFX = Color.web(colorHex);

        // NOTA DE IMPLEMENTACIÓN:
        // Las coordenadas reales de los puntos asociados a la anotación (AnnotationPoint) se deben proyectar
        // convirtiendo la Latitud y Longitud a píxeles X/Y relativos al tamaño de tu ImageView 'mapa'.
        // Aquí simulamos el pintado de un punto de interés representativo:
        
        double pixelX = 150.0; // Cálculo correspondiente mediante vuestra función de proyección (ej. GeoUtils)
        double pixelY = 200.0; 

        // Ejemplo básico: Si la anotación contiene texto, creamos una etiqueta visual flotante
        if (texto != null && !texto.isEmpty()) {
            Label etiquetaNota = new Label(texto);
            etiquetaNota.setTextFill(colorJavaFX);
            etiquetaNota.setStyle("-fx-font-weight: bold; -fx-background-color: rgba(255,255,255,0.7); -fx-padding: 2;");
            etiquetaNota.setLayoutX(pixelX);
            etiquetaNota.setLayoutY(pixelY - 15); // Desplazar un poco hacia arriba del punto
            
            Group.getChildren().add(etiquetaNota);
        }

        // Dibujar el marcador gráfico sobre el Group
        Circle marcador = new Circle(pixelX, pixelY, grosor + 3);
        marcador.setFill(colorJavaFX);
        marcador.setStroke(Color.WHITE);
        marcador.setStrokeWidth(1.5);

        // Añadir el nodo al Group para que escale correctamente cuando el usuario use el ScrollPane y el Zoom
        Group.getChildren().add(marcador);
    }
}
