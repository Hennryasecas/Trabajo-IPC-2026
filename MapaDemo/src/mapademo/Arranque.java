/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mapademo;

/**
 *
 * @author dennis
 */
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Clase principal de arranque de la aplicación IPC 2026.
 * Inicia cargando la pantalla de inicio de sesión (VistaLogin).
 */
public class Arranque extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Cargamos la vista de Login como punto de entrada
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mapademo/AdjuntosFXML/VistaLogin.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Running la Safor - Iniciar Sesión");
        
        // Opcional: Añadir un icono a la ventana principal de la aplicación si dispones de él
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/logo.png")));
        } catch (Exception e) {
            System.out.println("Icono de la aplicación no encontrado, se usará el por defecto.");
        }
        
        stage.setResizable(false); // Evita que se deforme el Login
        stage.show();
    }

    /**
     * Método main estándar de Java.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
