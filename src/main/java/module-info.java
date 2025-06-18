module com.dawciobiel.pacman {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    requires kotlin.stdlib;
    requires javafx.media;
    requires java.desktop;
    requires annotations;

    opens com.dawciobiel.pacman to javafx.fxml;
    exports com.dawciobiel.pacman;
    exports com.dawciobiel.pacman.utils;
    opens com.dawciobiel.pacman.utils to javafx.fxml;
    exports com.dawciobiel.pacman.sound;
    opens com.dawciobiel.pacman.sound to javafx.fxml;

    exports com.dawciobiel.pacman.entities;
    exports com.dawciobiel.pacman.game;
    exports com.dawciobiel.pacman.input;
    exports com.dawciobiel.pacman.level;
    exports com.dawciobiel.pacman.rendering;
}