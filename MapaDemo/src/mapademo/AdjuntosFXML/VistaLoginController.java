/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo.AdjuntosFXML;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author dennis
 */
public class VistaLoginController implements Initializable {

    @FXML
    private TextField Nickname;
    @FXML
    private PasswordField Password;
    @FXML
    private Button Buttom;
    @FXML
    private Hyperlink link;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Buttom.setOnAction(this::handleLogin);
        link.setOnAction(this::irARegistro);
    }    
    private void handleLogin(ActionEvent event) {
        String user = Nickname.getText().trim();
        String pass = Password.getText();

        // Aquí deberías validar contra la base de datos o API del caso de uso 'Running la Safor'
        boolean loginValido = !user.isEmpty() && !pass.isEmpty(); 

        if (loginValido) {
            try {
                // Navegamos al contenedor principal de la aplicación
                Parent mainRoot = FXMLLoader.load(getClass().getResource("/mapademo/AdjuntosFXML/MainView.fxml"));
                Stage stage = (Stage) Buttom.getScene().getWindow();
                stage.setScene(new Scene(mainRoot));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void irARegistro(ActionEvent event) {
        try {
            Parent registroRoot = FXMLLoader.load(getClass().getResource("/mapademo/AdjuntosFXML/VistaRegistro.fxml"));
            Stage stage = (Stage) link.getScene().getWindow();
            stage.setScene(new Scene(registroRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
