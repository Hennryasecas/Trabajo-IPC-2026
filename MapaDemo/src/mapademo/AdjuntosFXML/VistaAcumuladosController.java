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
        // TODO
        AppContext.getInstance().gpxActualProperty().addListener((obs, viejoFile, nuevoFile) -> {
            if (nuevoFile != null) {
                calcularYMostrarMetricas(nuevoFile);
            }
        });

        // Cargar datos por si ya había un fichero previamente seleccionado
        File archivoPreexistente = AppContext.getInstance().getGpxActual();
        if (archivoPreexistente != null) {
            calcularYMostrarMetricas(archivoPreexistente);
        }
    }    
    private void calcularYMostrarMetricas(File gpxFile) {
        // Aquí invocarás los métodos de la librería que procesa los GPX para extraer los datos reales
        // Ejemplo ficticio de actualización:
        LabelDistancia.setText("12.4 km");
        LabelTiempo.setText("01:05:32");
        LabelDesnivelPositivo.setText("+320 m");
        LabelDenivelNegativo.setText("-315 m");
    }
}
