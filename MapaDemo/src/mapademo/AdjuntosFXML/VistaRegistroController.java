/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo.AdjuntosFXML;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import upv.ipc.sportlib.User;
import upv.ipc.sportlib.SportActivityApp;

/**
 * FXML Controller class
 *
 * @author dennis
 */
public class VistaRegistroController implements Initializable {

    @FXML
    private ImageView imagen;
    @FXML
    private Button subirImagen;
    @FXML
    private TextField nombre;
    @FXML
    private TextField apellidos;
    @FXML
    private TextField nickname;
    @FXML
    private TextField correo;
    @FXML
    private PasswordField contraseña;
    @FXML
    private DatePicker fechaNacimiento;
    @FXML
    private Button crearCuenta;
    @FXML
    private Button cancelar;
    private File archivoImagenPerfil = null;
    private String rutaImagenPerfil = "";
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        subirImagen.setOnAction(this::handleSubirImagen);
        crearCuenta.setOnAction(this::handleCrearCuenta);
        cancelar.setOnAction(e -> regresarALogin());        
        // Restricción opcional: inicializar el DatePicker para evitar fechas futuras erróneas
        fechaNacimiento.setValue(LocalDate.now().minusYears(18));
        contraseña.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !contraseña.getText().isEmpty()) {
                if (!User.checkPassword(contraseña.getText())) {
                    contraseña.setStyle("-fx-border-color: red; -fx-background-color: #fee;");
                    mostrarAlerta(Alert.AlertType.WARNING, "Contraseña Débil", 
                        "La contraseña debe contener al menos 6 caracteres, una mayúscula, una minúscula y un número.");
                } else {
                    contraseña.setStyle("-fx-border-color: green; -fx-background-color: #efe;");
                }
            }
        });
    }    
    
    private void handleSubirImagen(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen de Perfil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File selectedFile = fileChooser.showOpenDialog(subirImagen.getScene().getWindow());
        if (selectedFile != null) {
            archivoImagenPerfil = selectedFile;
            Image img = new Image(selectedFile.toURI().toString());
            imagen.setImage(img); // Muestra la previsualización en el ImageView
        }
    }
    private void handleCrearCuenta(ActionEvent event) {
    String userNick = nickname.getText().trim();
    String userMail = correo.getText().trim();
    String userPass = contraseña.getText();
    LocalDate userFecha = fechaNacimiento.getValue();
    
    // Obtener la ruta o cadena de la imagen seleccionada (o enviar cadena vacía si no hay)
    String rutaImagenPerfil = (imagen.getImage() != null) ? imagen.getImage().getUrl() : "";

    // 1. Validaciones previas básicas
    if (userNick.isEmpty() || userMail.isEmpty() || userPass.isEmpty() || userFecha == null) {
        mostrarAlerta(Alert.AlertType.ERROR, "Campos Incompletos", "Por favor, rellena todos los campos obligatorios.");
        return;
    }

    try {
        // 2. Obtenemos la instancia de la aplicación
        SportActivityApp appCore = SportActivityApp.getInstance(); 
        
        // 3. CORRECCIÓN DEFINITIVA: Cambiamos el tipo de 'User' a 'boolean'
        // El método devuelve true/false según si la inserción en sportactivity.db tuvo éxito
        boolean registroExitoso = appCore.registerUser(
            userNick,          // 1. String
            userMail,          // 2. String
            userPass,          // 3. String
            userFecha,         // 4. LocalDate
            rutaImagenPerfil   // 5. String
        );

        // 4. Evaluamos el booleano devuelto por la librería
        if (registroExitoso) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Registro Completado", "El usuario se ha guardado con éxito.");
            regresarALogin();
        } else {
            // Si devuelve false es porque el Nickname o Email ya están registrados en SQLite
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Registro", "El nickname o el correo electrónico ya están en uso por otro atleta.");
        }

    } catch (Exception e) {
        mostrarAlerta(Alert.AlertType.ERROR, "Error del Sistema", "No se pudo conectar o insertar en la base de datos.");
        e.printStackTrace();
    }
}
    private void handleCancelar(ActionEvent event) {
        regresarALogin();
    }
    
    private void regresarALogin() {
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/mapademo/AdjuntosFXML/VistaLogin.fxml"));
            Stage stage = (Stage) cancelar.getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            stage.setTitle("Running la Safor - Iniciar Sesión");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Navegación", "No se pudo cargar la vista de inicio de sesión.");
        }
    }
    
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
