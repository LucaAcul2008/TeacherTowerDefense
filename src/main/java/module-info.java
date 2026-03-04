module at.htl.teachertowerdefense {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;

    opens at.htl.teachertowerdefense to javafx.fxml;
    exports at.htl.teachertowerdefense;
}