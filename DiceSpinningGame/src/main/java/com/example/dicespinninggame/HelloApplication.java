package com.example.dicespinninggame;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Random;

public class HelloApplication extends Application {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int WHEEL_RADIUS = 200;
    private static final int NUM_SECTORS = 8;
    private static final int MAX_ATTEMPTS = 3;
    private static final int LUCKY_NUMBER_COUNT = 3;

    private double rotationAngle = 0;
    private boolean spinning = false;
    private int result = -1;
    private int spinCount = 0;
    private int score = 0;
    private int[] lastResults = new int[LUCKY_NUMBER_COUNT];
    private int luckyNumberIndex = 0;

    private Random random = new Random();

    private final String[] sectorLabels = {"1", "2", "3", "4", "5", "6", "7", "8"};
    private final Color[] sectorColors = {Color.RED, Color.WHITE, Color.RED, Color.WHITE, Color.RED, Color.WHITE, Color.RED, Color.WHITE};

    private ProgressBar progressBar;
    private Text attemptsLeftText;
    private VBox attemptsBox;
    private ProgressBar[] attemptBars;
    private Text[] scoreTexts;
    private Text freeSpinText;
    private Text luckyNumberText;
    private ImageView diceImageView;
    private Text totalScoreText; // New

    private boolean easyMode = true;

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: lightgrey;");

        Text titleText = new Text("DICE WHEEL SPINNING GAME");
        titleText.setFont(new Font(30));
        titleText.setFill(Color.GOLD);
        titleText.setStroke(Color.BLUE);
        titleText.setStrokeWidth(1.5);
        titleText.setStyle("-fx-border-color: white; -fx-border-width: 2px; -fx-padding: 10px;");

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);

        attemptsLeftText = new Text("Attempts left: 3");
        attemptsLeftText.setFont(new Font(20));
        attemptsLeftText.setFill(Color.BLACK);

        HBox mainContent = new HBox(20);
        mainContent.setAlignment(Pos.CENTER);

        VBox percentageBox = new VBox(10);
        percentageBox.setStyle("-fx-background-color: brown; -fx-padding: 10px;");
        percentageBox.setAlignment(Pos.CENTER);
        percentageBox.setEffect(new DropShadow());

        Text attemptsTitle = new Text("Attempts");
        attemptsTitle.setFont(new Font(20));
        attemptsTitle.setFill(Color.BLACK);

        Text scoreTitle = new Text("Score");
        scoreTitle.setFont(new Font(20));
        scoreTitle.setFill(Color.GOLD);

        percentageBox.getChildren().addAll(attemptsTitle, scoreTitle);

        attemptBars = new ProgressBar[MAX_ATTEMPTS];
        scoreTexts = new Text[MAX_ATTEMPTS];

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            attemptBars[i] = new ProgressBar(0);
            attemptBars[i].setStyle("-fx-accent: red;");
            attemptBars[i].setPrefWidth(100);

            Text label = new Text();
            switch (i) {
                case 0:
                    label.setText("First:");
                    break;
                case 1:
                    label.setText("Second:");
                    break;
                case 2:
                    label.setText("Third:");
                    break;
                default:
                    break;
            }
            label.setFill(Color.WHITE);
            HBox hbox = new HBox(10);
            hbox.getChildren().addAll(label, attemptBars[i]);

            scoreTexts[i] = new Text("Attempt " + (i + 1) + ": ");
            scoreTexts[i].setFont(new Font(20));
            scoreTexts[i].setFill(Color.GOLD);
            HBox scoreHbox = new HBox(10);
            scoreHbox.getChildren().add(scoreTexts[i]);

            percentageBox.getChildren().addAll(hbox, scoreHbox);
        }

        VBox wheelContainer = new VBox();
        wheelContainer.setAlignment(Pos.CENTER);
        wheelContainer.getChildren().add(canvas);

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-background-color: blue; -fx-padding: 10px;");
        DropShadow buttonShadow = new DropShadow();
        buttonShadow.setOffsetX(3);
        buttonShadow.setOffsetY(3);
        buttonShadow.setColor(Color.DARKBLUE);
        buttonBox.setEffect(buttonShadow);

        Button spinButton = new Button("Spin");
        styleButton(spinButton);
        spinButton.setOnAction(event -> {
            if (spinCount < MAX_ATTEMPTS) {
                spinning = true;
                result = -1; // Reset result
                spinWheel();
            } else {
                resetGame();
                displayTotalScore(); // Display total score after the game is over
            }
        });

        Button replayButton = new Button("Replay");
        styleButton(replayButton);
        replayButton.setOnAction(event -> resetGame());

        Button toggleModeButton = new Button("Toggle Mode");
        styleButton(toggleModeButton);
        toggleModeButton.setOnAction(event -> {
            easyMode = !easyMode;
            toggleModeButton.setText(easyMode ? "Easy Mode" : "Hard Mode");
        });

        buttonBox.getChildren().addAll(spinButton, replayButton, toggleModeButton);

        mainContent.getChildren().addAll(wheelContainer, percentageBox);

        root.getChildren().addAll(titleText, progressBar, attemptsLeftText, mainContent, buttonBox);

        Scene scene = new Scene(root, WIDTH, HEIGHT);

        // Start animation timer
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                render(gc);
                if (spinning) {
                    rotate();
                    if (!isSpinning()) {
                        spinning = false;
                        displayResult();
                        spinCount++;
                        updateProgressBar();
                        if (result == 6) {
                            showFreeSpinMessage();
                        } else {
                            checkForLuckyNumber();
                            showTrialsAlert();
                        }
                    }
                }
            }
        }.start();

        primaryStage.setScene(scene);
        primaryStage.setTitle("Dice Spinning Wheel Game");
        primaryStage.show();
    }

    private void render(GraphicsContext gc) {
        gc.clearRect(0, 0, WIDTH, HEIGHT);
        // Draw thick gold wheel with glowing border
        gc.setEffect(new DropShadow(30, Color.GOLD));
        gc.setFill(Color.GOLD);
        gc.fillOval(WIDTH / 2 - WHEEL_RADIUS, HEIGHT / 2 - WHEEL_RADIUS, WHEEL_RADIUS * 2, WHEEL_RADIUS * 2);
        gc.setEffect(null);

        // Draw segments
        double angleIncrement = 360.0 / NUM_SECTORS;
        for (int i = 0; i < NUM_SECTORS; i++) {
            double startAngle = i * angleIncrement + rotationAngle;
            gc.setFill(sectorColors[i]);
            gc.fillArc(WIDTH / 2 - WHEEL_RADIUS, HEIGHT / 2 - WHEEL_RADIUS, WHEEL_RADIUS * 2, WHEEL_RADIUS * 2, startAngle, angleIncrement, javafx.scene.shape.ArcType.ROUND);

            // Draw sector labels
            gc.setFill(Color.WHITE);
            gc.setFont(new Font(20));
            double textAngle = Math.toRadians(startAngle + angleIncrement / 2);
            double x = WIDTH / 2 + (WHEEL_RADIUS - 50) * Math.cos(textAngle);
            double y = HEIGHT / 2 + (WHEEL_RADIUS - 50) * Math.sin(textAngle);
            gc.fillText(sectorLabels[i], x - 10, y + 10);
        }

        // Draw pointer
        gc.setFill(Color.BLACK);
        gc.fillPolygon(new double[]{WIDTH / 2, WIDTH / 2 - 10, WIDTH / 2 + 10}, new double[]{HEIGHT / 2 - WHEEL_RADIUS - 10, HEIGHT / 2 - WHEEL_RADIUS + 10, HEIGHT / 2 - WHEEL_RADIUS + 10}, 3);

        // Display result as a face of a die
        if (result != -1) {
            int sectorValue = Integer.parseInt(sectorLabels[result]);
            if (sectorValue <= 6) {
                drawDiceFace(gc, WIDTH / 2 - 30, HEIGHT / 2 + WHEEL_RADIUS + 20, 60, sectorValue);
            } else {
                drawLoseMessage(gc, WIDTH / 2 - 30, HEIGHT / 2 + WHEEL_RADIUS + 20, 60);
            }
        }

        // Display free spin message
        if (freeSpinText != null) {
            gc.setFill(Color.RED);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.fillRect(WIDTH / 2 - 200, HEIGHT / 2 + WHEEL_RADIUS + 80, 400, 40);
            gc.strokeRect(WIDTH / 2 - 200, HEIGHT / 2 + WHEEL_RADIUS + 80, 400, 40);
            gc.setFill(Color.WHITE);
            gc.setFont(new Font(16));
            gc.fillText(freeSpinText.getText(), WIDTH / 2 - 90, HEIGHT / 2 + WHEEL_RADIUS + 105);
        }

        // Display lucky number message
        if (luckyNumberText != null) {
            gc.setFill(Color.BLUE);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.fillRect(WIDTH / 2 - 150, HEIGHT / 2 + WHEEL_RADIUS + 150, 300, 40);
            gc.strokeRect(WIDTH / 2 - 150, HEIGHT / 2 + WHEEL_RADIUS + 150, 300, 40);
            gc.setFill(Color.WHITE);
            gc.setFont(new Font(16));
            gc.fillText(luckyNumberText.getText(), WIDTH / 2 - 140, HEIGHT / 2 + WHEEL_RADIUS + 175);
        }

        // Display total score
        if (totalScoreText != null) {
            gc.setFill(Color.BLACK);
            gc.setFont(new Font(20));
            gc.fillText(totalScoreText.getText(), WIDTH / 2 - 50, HEIGHT / 2 + WHEEL_RADIUS + 230);
        }
    }

    private void rotate() {
        rotationAngle += 5; // Change this value to adjust rotation speed
        if (rotationAngle >= 360) {
            rotationAngle -= 360;
        }
    }

    private boolean isSpinning() {
        // Simulate spinning
        return Math.random() > 0.005; // Adjust this threshold for more realistic spinning
    }

    private void spinWheel() {
        spinning = true;
    }

    private void displayResult() {
        result = random.nextInt(NUM_SECTORS);
        double angleIncrement = 360.0 / NUM_SECTORS;
        double targetAngle = (NUM_SECTORS - result) * angleIncrement;
        rotationAngle = targetAngle;

        // Calculate score
        int sectorValue = Integer.parseInt(sectorLabels[result]);
        score += sectorValue;

        // Update last results array
        lastResults[luckyNumberIndex] = sectorValue;
        luckyNumberIndex = (luckyNumberIndex + 1) % LUCKY_NUMBER_COUNT;

        // Display congratulations message if result is 6
        if (sectorValue == 6) {
            showFreeSpinMessage();
        }
    }

    private void showFreeSpinMessage() {
        freeSpinText = new Text("Congratulations! You got a free spin!");
    }

    private void checkForLuckyNumber() {
        Arrays.sort(lastResults);
        if (Arrays.binarySearch(lastResults, 6) >= 0) {
            luckyNumberText = new Text("LUCKY NUMBER ALERT! You got a lucky number!");
        } else {
            luckyNumberText = null;
        }
    }

    private void showTrialsAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Spin Result");
            alert.setHeaderText(null);
            alert.setContentText("Your spin result is: " + sectorLabels[result]);
            alert.showAndWait();
        });
    }

    private void updateProgressBar() {
        double progress = (double) spinCount / MAX_ATTEMPTS;
        progressBar.setProgress(progress);
        attemptsLeftText.setText("Attempts left: " + (MAX_ATTEMPTS - spinCount));

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            if (i < spinCount) {
                attemptBars[i].setProgress(1);
                scoreTexts[i].setText("Attempt " + (i + 1) + ": " + lastResults[i]);
            } else {
                attemptBars[i].setProgress(0);
                scoreTexts[i].setText("Attempt " + (i + 1) + ": ");
            }
        }
    }

    private void resetGame() {
        spinCount = 0;
        score = 0;
        Arrays.fill(lastResults, 0);
        luckyNumberIndex = 0;
        result = -1;
        freeSpinText = null;
        luckyNumberText = null;
        updateProgressBar();
    }

    private void styleButton(Button button) {
        button.setStyle("-fx-background-color: #ffd700; -fx-font-size: 20; -fx-text-fill: #000000;");
        button.setOnMouseEntered(event -> button.setStyle("-fx-background-color: #ffff00; -fx-font-size: 22; -fx-text-fill: #000000;"));
        button.setOnMouseExited(event -> button.setStyle("-fx-background-color: #ffd700; -fx-font-size: 20; -fx-text-fill: #000000;"));
    }

    private void drawDiceFace(GraphicsContext gc, double x, double y, double size, int value) {
        gc.setFill(Color.WHITE);
        gc.fillRect(x, y, size, size);

        gc.setStroke(Color.BLACK);
        gc.strokeRect(x, y, size, size);

        gc.setFill(Color.BLACK);
        double dotSize = size / 6.0;
        double dotOffset = size / 6.0;

        switch (value) {
            case 1:
                drawDot(gc, x + size / 2, y + size / 2, dotSize);
                break;
            case 2:
                drawDot(gc, x + dotOffset, y + dotOffset, dotSize);
                drawDot(gc, x + size - dotOffset, y + size - dotOffset, dotSize);
                break;
            case 3:
                drawDot(gc, x + dotOffset, y + dotOffset, dotSize);
                drawDot(gc, x + size / 2, y + size / 2, dotSize);
                drawDot(gc, x + size - dotOffset, y + size - dotOffset, dotSize);
                break;
            case 4:
                drawDot(gc, x + dotOffset, y + dotOffset, dotSize);
                drawDot(gc, x + size - dotOffset, y + dotOffset, dotSize);
                drawDot(gc, x + dotOffset, y + size - dotOffset, dotSize);
                drawDot(gc, x + size - dotOffset, y + size - dotOffset, dotSize);
                break;
            case 5:
                drawDot(gc, x + dotOffset, y + dotOffset, dotSize);
                drawDot(gc, x + size - dotOffset, y + dotOffset, dotSize);
                drawDot(gc, x + dotOffset, y + size - dotOffset, dotSize);
                drawDot(gc, x + size - dotOffset, y + size - dotOffset, dotSize);
                drawDot(gc, x + size / 2, y + size / 2, dotSize);
                break;
            case 6:
                drawDot(gc, x + dotOffset, y + dotOffset, dotSize);
                drawDot(gc, x + size - dotOffset, y + dotOffset, dotSize);
                drawDot(gc, x + dotOffset, y + size / 2, dotSize);
                drawDot(gc, x + size - dotOffset, y + size / 2, dotSize);
                drawDot(gc, x + dotOffset, y + size - dotOffset, dotSize);
                drawDot(gc, x + size - dotOffset, y + size - dotOffset, dotSize);
                break;
            default:
                break;
        }
    }

    private void drawDot(GraphicsContext gc, double x, double y, double size) {
        gc.fillOval(x - size / 2, y - size / 2, size, size);
    }

    private void drawLoseMessage(GraphicsContext gc, double x, double y, double size) {
        gc.setFill(Color.RED);
        gc.setFont(new Font(30));
        gc.fillText("LOSE", x, y + size / 2);
    }

    private void displayTotalScore() {
        totalScoreText = new Text("Total Score: " + score);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

