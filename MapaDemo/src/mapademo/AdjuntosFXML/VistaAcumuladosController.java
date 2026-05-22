/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo.AdjuntosFXML;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import upv.ipc.sportlib.Activity;
import util2.AppContext;

/**
 * FXML Controller class
 *
 * @author dennis
 */
public class VistaAcumuladosController implements Initializable {

    @FXML
    private Label LabelDistancia;
    @FXML
    private Label LabelTiempo;
    @FXML
    private Label LabelDesnivelPositivo;
    @FXML
    private Label LabelDenivelNegativo;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
       // 1. ESCUCHADOR REACTIVO: Se activa automáticamente cuando cambia la actividad en el AppContext
        AppContext.getInstance().actividadActualProperty().addListener((obs, viejaActividad, nuevaActividad) -> {
            if (nuevaActividad != null) {
                actualizarDatosAcumulados(nuevaActividad);
            } else {
                limpiarEtiquetas();
            }
        });

        // 2. Carga inicial por si ya existía una actividad cargada previamente al abrir la vista
        Activity actividadInicial = AppContext.getInstance().getActividadActual();
        if (actividadInicial != null) {
            actualizarDatosAcumulados(actividadInicial);
        } else {
            limpiarEtiquetas();
        }
    }    
    private void actualizarDatosAcumulados(Activity actividad) {
        try {
            // Extraer datos usando los métodos del JAR (revisa si devuelven metros/kilómetros o segundos)
            double distanciaMetros = actividad.getTotalDistance(); 
            double distanciaKm = distanciaMetros / 1000.0;
            
            // El tiempo total suele venir en segundos o mediante una duración
            long tiempoSegundos = actividad.getDuration().toSeconds(); 
            String tiempoFormateado = formatearTiempo(tiempoSegundos);

            // Desniveles acumulados (valores numéricos de la ruta)
            double desnivelPositivo = actividad.getElevationGain();
            double desnivelNegativo = actividad.getElevationLoss();

            // Asignar los textos formateados a tus componentes FXML con dos decimales
            LabelDistancia.setText(String.format("%.2f Km", distanciaKm));
            LabelTiempo.setText(tiempoFormateado);
            LabelDesnivelPositivo.setText(String.format("%.1f m", desnivelPositivo));
            LabelDenivelNegativo.setText(String.format("%.1f m", desnivelNegativo));
            
        } catch (Exception e) {
            System.err.println("Error al calcular los acumulados de la actividad: " + e.getMessage());
            e.printStackTrace();
            limpiarEtiquetas();
        }
        
    }
    private void limpiarEtiquetas() {
        LabelDistancia.setText("0.00 Km");
        LabelTiempo.setText("00:00:00");
        LabelDesnivelPositivo.setText("0.0 m");
        LabelDenivelNegativo.setText("0.0 m");
    }

    /**
     * Convierte un total de segundos en un formato legible HH:MM:SS para el corredor.
     */
    private String formatearTiempo(long totalSegundos) {
        long horas = totalSegundos / 3600;
        long minutos = (totalSegundos % 3600) / 60;
        long segundos = totalSegundos % 60;
        return String.format("%02d:%02d:%02d", horas, minutos, segundos);
    }
   
}
