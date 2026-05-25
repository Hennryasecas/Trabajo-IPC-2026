/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo.AdjuntosFXML;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

// Importaciones oficiales de la librería proporcionada para el proyecto

import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;

public class VistaHistorialController implements Initializable {

    @FXML
    private TableView<Activity> tableHistorial;
    @FXML
    private TableColumn<Activity, LocalDateTime> colFecha;
    @FXML
    private TableColumn<Activity, String> colTipo;
    @FXML
    private TableColumn<Activity, Double> colDistancia;
    @FXML
    private TableColumn<Activity, Duration> colTiempo;
    @FXML
    private TableColumn<Activity, Double> colRitmo;
    @FXML
    private TableColumn<Activity, Double> colCalorias;

    @FXML
    private Label lblContadorSesiones;

    private SportActivityApp app;
    private ObservableList<Activity> listaActividades;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        app = SportActivityApp.getInstance();
        
        // Vincular las columnas con los métodos de la librería
        colFecha.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDistancia.setCellValueFactory(new PropertyValueFactory<>("totalDistance"));
        colTiempo.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colRitmo.setCellValueFactory(new PropertyValueFactory<>("averagePace"));
        colCalorias.setCellValueFactory(new PropertyValueFactory<>("elevationGain")); 

        // Formatear Fecha (dd/MM/yyyy HH:mm)
        colFecha.setCellFactory(column -> new TableCell<Activity, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        // Formatear Distancia (metros a km)
        colDistancia.setCellFactory(column -> new TableCell<Activity, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f km", item / 1000.0));
                }
            }
        });

        // Formatear Tiempo (hh:mm:ss)
        colTiempo.setCellFactory(column -> new TableCell<Activity, Duration>() {
            @Override
            protected void updateItem(Duration item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    long s = item.getSeconds();
                    setText(String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60));
                }
            }
        });

        // Formatear Tipo con Badge CSS redondo
        colTipo.setCellFactory(column -> new TableCell<Activity, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.toUpperCase());
                    getStyleClass().add("badge-completed"); 
                }
            }
        });

        // --- DETECTOR DE DOBLE CLIC PARA COORDINACIÓN CON COMPAÑEROS ---
        tableHistorial.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Activity seleccionada = tableHistorial.getSelectionModel().getSelectedItem();
                if (seleccionada != null) {
                    System.out.println("Doble clic en: " + seleccionada.getName());
                    
                    // Aquí se conecta con el método de Dennis para cambiar de pantalla
                    // cargarVistaCentro("/fxml/VistaMapaEntrenamiento.fxml");
                }
            }
        });

        cargarDatos();
    }

    private void cargarDatos() {
        if (app.getCurrentUser() != null) {
            listaActividades = FXCollections.observableArrayList(app.getCurrentUser().getActivities());
            tableHistorial.setItems(listaActividades);
            lblContadorSesiones.setText("Total: " + listaActividades.size() + " sesiones de entrenamiento");
        }
    }
}