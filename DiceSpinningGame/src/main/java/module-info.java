module com.example.dicespinninggame {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.dicespinninggame to javafx.fxml;
    exports com.example.dicespinninggame;
}