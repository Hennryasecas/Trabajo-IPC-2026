/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo.AdjuntosFXML;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.chart.AreaChart;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;

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

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
