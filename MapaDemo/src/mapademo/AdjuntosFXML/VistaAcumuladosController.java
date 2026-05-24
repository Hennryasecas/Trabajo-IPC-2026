/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo.AdjuntosFXML;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import upv.ipc.sportlib.Activity;

// Usamos el puente automático que NetBeans ya aprendió a leer

import upv.ipc.sportlib.SportActivityApp;

public class VistaAcumuladosController implements Initializable {

    @FXML
    private Label lblTotalKm;
    @FXML
    private Label lblTotalTiempo;
    @FXML
    private Label lblMesActual;

    private SportActivityApp app;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        app = SportActivityApp.getInstance();
        calcularAcumuladosMes();
    }

    private void calcularAcumuladosMes() {
        if (app.getCurrentUser() == null) return;

        LocalDateTime ahora = LocalDateTime.now();
        int mesActual = ahora.getMonthValue();
        int anioActual = ahora.getYear();

        // Poner el nombre del mes en la interfaz
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                          "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        lblMesActual.setText("Resumen de " + meses[mesActual - 1]);

        double sumaMetros = 0;
        long sumaSegundos = 0;

        // Bucle para filtrar y sumar solo los entrenamientos de este mes
        for (Activity actividad : app.getCurrentUser().getActivities()) {
            LocalDateTime fechaActividad = actividad.getStartTime();
            if (fechaActividad != null && 
                fechaActividad.getMonthValue() == mesActual && 
                fechaActividad.getYear() == anioActual) {
                
                sumaMetros += actividad.getTotalDistance();
                if (actividad.getDuration() != null) {
                    sumaSegundos += actividad.getDuration().getSeconds();
                }
            }
        }

        // Pasar metros a kilómetros
        double totalKm = sumaMetros / 1000.0;
        lblTotalKm.setText(String.format("%.2f km", totalKm));

        // Formatear las horas, minutos y segundos totales acumulados
        long horas = sumaSegundos / 3600;
        long minutos = (sumaSegundos % 3600) / 60;
        long segundos = sumaSegundos % 60;
        lblTotalTiempo.setText(String.format("%dh %02dm %02ds", horas, minutos, segundos));
    }
}