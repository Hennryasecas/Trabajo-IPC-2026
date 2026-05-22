/*
 
/**
 *
 * @author dennis
 */
package util2;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import java.io.File;

public class AppContext {
    private static AppContext instance;
    
    // Propiedad observable para la ruta/actividad actual
    private final ObjectProperty<File> gpxActual = new SimpleObjectProperty<>();

    private AppContext() {}

    public static AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    public ObjectProperty<File> gpxActualProperty() { return gpxActual; }
    public File getGpxActual() { return gpxActual.get(); }
    public void setGpxActual(File gpx) { this.gpxActual.set(gpx); }
}
