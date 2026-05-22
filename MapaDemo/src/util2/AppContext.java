/*
 
/**
 *
 * @author dennis
 */
package util2;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import java.io.File;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.User;
public class AppContext {
   private static AppContext instance;
    
    // Instancia única o simulación de la app (vuestra librería expone los métodos de persistencia)
    private User usuarioLogueado;
    private final ObjectProperty<Activity> actividadActual = new SimpleObjectProperty<>();
    // Propiedad para pasar la distancia/punto seleccionado en la gráfica al mapa
    private final ObjectProperty<Integer> indicePuntoSeleccionado = new SimpleObjectProperty<>(-1);

    private AppContext() {}

    public static AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    public User getUsuarioLogueado() { return usuarioLogueado; }
    public void setUsuarioLogueado(User usuario) { this.usuarioLogueado = usuario; }

    public ObjectProperty<Activity> actividadActualProperty() { return actividadActual; }
    public Activity getActividadActual() { return actividadActual.get(); }
    public void setActividadActual(Activity actividad) { this.actividadActual.set(actividad); }

    public ObjectProperty<Integer> indicePuntoSeleccionadoProperty() { return indicePuntoSeleccionado; }
    public Integer getIndicePuntoSeleccionado() { return indicePuntoSeleccionado.get(); }
    public void setIndicePuntoSeleccionado(Integer indice) { this.indicePuntoSeleccionado.set(indice); }
}