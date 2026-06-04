package com.audiometer;

import com.audiometer.model.*;
import com.audiometer.serial.SerialService;
import com.audiometer.service.HughsonWestlakeService;
import com.audiometer.ui.AudiogramCanvas;
import com.fazecast.jSerialComm.SerialPort;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;



public class MainApp extends Application {

    private final HughsonWestlakeService testService = new HughsonWestlakeService();
    private final SerialService serialService = new SerialService();

    private Label currentEarLabel;
    private Label currentFrequencyLabel;
    private Label currentDbLabel;
    private Label statusLabel;
    private Label progressLabel;

    private ProgressBar progressBar;
    private AudiogramCanvas audiogramCanvas;

    private Button startTestButton;
    private Button stopTestButton;
    private Button connectButton;
    private Button refreshPortsButton;

    private ComboBox<String> portBox;

    private PauseTransition responseTimer;

    private boolean testRunning = false;
    private boolean waitingForResponse = false;
    private boolean completionAlertShown = false;

    private static final int RESPONSE_TIMEOUT_SECONDS = 10;

    @Override
    public void start(Stage stage) {
        Label title = new Label("Audiometer Control System");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        currentEarLabel = new Label();
        currentFrequencyLabel = new Label();
        currentDbLabel = new Label();
        statusLabel = new Label("Status: Ready");

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(260);
        progressLabel = new Label("Progress: 0 / 12");

        portBox = new ComboBox<>();
        refreshPortsButton = new Button("Refresh Ports");
        connectButton = new Button("Connect");

        startTestButton = new Button("Start Test");
        stopTestButton = new Button("Stop Test");
        stopTestButton.setDisable(true);

        audiogramCanvas = new AudiogramCanvas(650, 420);

        refreshPortList();

        refreshPortsButton.setOnAction(e -> refreshPortList());

        connectButton.setOnAction(e -> connectToSelectedPort());

        serialService.setListener(message -> {
            if (message.equalsIgnoreCase("RESPONSE")) {
                Platform.runLater(this::handleResponseReceived);
            }
        });

        startTestButton.setOnAction(e -> startAutomaticTest());

        stopTestButton.setOnAction(e -> stopAutomaticTest("Test stopped by user."));

        GridPane infoPanel = new GridPane();
        infoPanel.setHgap(12);
        infoPanel.setVgap(12);

        infoPanel.add(new Label("Current Ear:"), 0, 0);
        infoPanel.add(currentEarLabel, 1, 0);

        infoPanel.add(new Label("Current Frequency:"), 0, 1);
        infoPanel.add(currentFrequencyLabel, 1, 1);

        infoPanel.add(new Label("Current Intensity:"), 0, 2);
        infoPanel.add(currentDbLabel, 1, 2);

        infoPanel.add(new Label("COM Port:"), 0, 3);
        infoPanel.add(portBox, 1, 3);

        HBox serialButtons = new HBox(10, refreshPortsButton, connectButton);
        HBox testButtons = new HBox(10, startTestButton, stopTestButton);

        VBox leftPanel = new VBox(
                20,
                title,
                infoPanel,
                progressLabel,
                progressBar,
                serialButtons,
                testButtons,
                statusLabel
        );

        leftPanel.setPadding(new Insets(25));
        leftPanel.setPrefWidth(450);

        BorderPane root = new BorderPane();
        root.setLeft(leftPanel);
        root.setCenter(audiogramCanvas);
        root.setPadding(new Insets(10));

        updateCurrentLabels();

        Scene scene = new Scene(root, 1150, 550);
        stage.setTitle("Audiometer System");
        stage.setScene(scene);
        stage.show();
    }

    private void connectToSelectedPort() {
        String selectedPortDisplay = portBox.getValue();

        if (selectedPortDisplay == null) {
            statusLabel.setText("Please select a COM port.");
            return;
        }

        // Extract just the "COMX" part before the colon
        String actualPort = selectedPortDisplay.split(":")[0].trim();

        // Connect using ONLY the extracted port name
        boolean connected = serialService.connect(actualPort);

        if (connected) {
            statusLabel.setText("Connected to " + actualPort);
            connectButton.setDisable(true);
            refreshPortsButton.setDisable(true);
            portBox.setDisable(true);
        } else {
            statusLabel.setText("Connection failed.");
        }
    }

    private void startAutomaticTest() {
        if (!serialService.isConnected()) {
            statusLabel.setText("Please connect to COM port before starting the test.");
            return;
        }

        if (testService.getState() == TestState.TEST_FINISHED) {
            statusLabel.setText("Test is already finished. Restart the app to run a new test.");
            return;
        }

        testRunning = true;
        completionAlertShown = false;

        startTestButton.setDisable(true);
        stopTestButton.setDisable(false);

        statusLabel.setText("Automatic test started.");
        sendCurrentToneAndWait();
    }

    private void sendCurrentToneAndWait() {
        if (!testRunning) {
            return;
        }

        if (testService.getState() == TestState.TEST_FINISHED) {
            finishTest();
            return;
        }

        int frequency = testService.getCurrentFrequency();
        int db = testService.getCurrentDb();
        Ear ear = testService.getCurrentEar();

        serialService.sendToneCommand(frequency, db, ear.toString());

        waitingForResponse = true;

        statusLabel.setText(
                "Tone playing: " + frequency + " Hz, " + db + " dB HL, " + ear
                        + " | Waiting " + RESPONSE_TIMEOUT_SECONDS + "s for RESPONSE..."
        );

        responseTimer = new PauseTransition(Duration.seconds(RESPONSE_TIMEOUT_SECONDS));
        responseTimer.setOnFinished(e -> handleResponseTimeout());
        responseTimer.play();
    }

    private void handleResponseReceived() {
        if (!testRunning || !waitingForResponse) {
            return;
        }

        waitingForResponse = false;

        if (responseTimer != null) {
            responseTimer.stop();
        }

        serialService.sendStopCommand();

        statusLabel.setText("RESPONSE received. STOP sent.");

        processPatientAnswer(true);

        if (testRunning && testService.getState() != TestState.TEST_FINISHED) {
            sendCurrentToneAndWait();
        }
    }

    private void handleResponseTimeout() {
        if (!testRunning || !waitingForResponse) {
            return;
        }

        waitingForResponse = false;

        serialService.sendStopCommand();

        statusLabel.setText("No RESPONSE in 10 seconds. Marked as not heard. STOP sent.");

        processPatientAnswer(false);

        if (testRunning && testService.getState() != TestState.TEST_FINISHED) {
            sendCurrentToneAndWait();
        }
    }

    private void processPatientAnswer(boolean heard) {
        HearingThreshold threshold = testService.processResponse(heard);

        if (threshold != null) {
            AudiogramPoint point = new AudiogramPoint(
                    threshold.getEar(),
                    threshold.getFrequency(),
                    threshold.getThresholdDb()
            );

            audiogramCanvas.addPoint(point);
            statusLabel.setText("Threshold found: " + threshold);
        }

        updateCurrentLabels();

        if (testService.getState() == TestState.TEST_FINISHED) {
            finishTest();
        }
    }

    private void finishTest() {
        testRunning = false;
        waitingForResponse = false;

        if (responseTimer != null) {
            responseTimer.stop();
        }

        serialService.sendStopCommand();

        startTestButton.setDisable(true);
        stopTestButton.setDisable(true);

        updateCurrentLabels();

        statusLabel.setText("Test finished. Audiogram completed.");

        if (!completionAlertShown) {
            completionAlertShown = true;

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Test Completed");
            alert.setHeaderText("Audiometry Test Finished");
            alert.setContentText("All frequencies for both ears have been tested.");
            alert.show();
        }
    }

    private void stopAutomaticTest(String reason) {
        testRunning = false;
        waitingForResponse = false;

        if (responseTimer != null) {
            responseTimer.stop();
        }

        if (serialService.isConnected()) {
            serialService.sendStopCommand();
        }

        startTestButton.setDisable(false);
        stopTestButton.setDisable(true);

        statusLabel.setText(reason);
    }

    private void refreshPortList() {
        portBox.getItems().clear();

        for (SerialPort port : serialService.getAvailablePorts()) {
            String portName = port.getSystemPortName();             // e.g., "COM3"
            String description = port.getDescriptivePortName();     // e.g., "Bluetooth Serial Port (COM3)"

            // Add the formatted string to the dropdown
            portBox.getItems().add(portName + ": " + description);
        }

        if (!portBox.getItems().isEmpty()) {
            portBox.setValue(portBox.getItems().get(0));
        }
    }

    private void updateCurrentLabels() {
        int completed = testService.getCompletedThresholdCount();
        int total = testService.getTotalThresholdCount();

        progressBar.setProgress((double) completed / total);
        progressLabel.setText("Progress: " + completed + " / " + total);

        if (testService.getState() == TestState.TEST_FINISHED) {
            currentEarLabel.setText("-");
            currentFrequencyLabel.setText("Completed");
            currentDbLabel.setText("-");
            audiogramCanvas.setHighlightedFrequency(8000);
            return;
        }

        currentEarLabel.setText(testService.getCurrentEar().toString());
        currentFrequencyLabel.setText(testService.getCurrentFrequency() + " Hz");
        currentDbLabel.setText(testService.getCurrentDb() + " dB HL");

        audiogramCanvas.setHighlightedFrequency(testService.getCurrentFrequency());
    }

    @Override
    public void stop() {
        if (responseTimer != null) {
            responseTimer.stop();
        }

        if (serialService.isConnected()) {
            serialService.sendStopCommand();
        }

        serialService.disconnect();
    }

    public static void main(String[] args) {
        launch();
    }
}