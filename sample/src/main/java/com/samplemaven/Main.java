package com.samplemaven;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.scene.control.ScrollPane;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.ISO87APackager;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Main extends Application {

    private Stage primaryStage;
    private Scene inputScene, sendDataScene;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Create the first (input) scene
        createInputScene();

        // Set initial scene
        primaryStage.setTitle("ISO Message Sender");
        primaryStage.setScene(inputScene);
        primaryStage.show();
    }

    private void createInputScene() {
        // Create the grid layout for the fields
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        // Create input fields for ISO fields (Field 2 to Field 128 for simplicity)
        Label[] labels = new Label[127];
        TextField[] textFields = new TextField[127];

        // Dynamically create 127 fields
        for (int i = 0; i < 127; i++) {
            labels[i] = new Label("Field " + (i + 2) + ":");
            textFields[i] = new TextField();
            grid.add(labels[i], 0, i);
            grid.add(textFields[i], 1, i);
        }

        // Create a horizontal layout for Host and Port input fields
        Label hostLabel = new Label("Host:");
        TextField hostField = new TextField();
        hostField.setPromptText("Enter Host");

        Label portLabel = new Label("Port:");
        TextField portField = new TextField();
        portField.setPromptText("Enter Port");

        HBox hostPortBox = new HBox(10, hostLabel, hostField, portLabel, portField);
        hostPortBox.setAlignment(Pos.CENTER);
        hostPortBox.setPadding(new Insets(10, 0, 10, 0));

        // Add a send button with space around it
        Button sendButton = new Button("Send");

        // Create a VBox layout to place the send button above the form
        VBox vbox = new VBox(20);  // Increased space between button and fields
        vbox.getChildren().addAll(hostPortBox, sendButton, grid);  // Add the host/port input, send button, and grid to the VBox

        // Create a ScrollPane to make the grid scrollable
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);  // Make the content fit to the width of the scroll pane
        scrollPane.setFitToHeight(true);  // Make the content fit to the height of the scroll pane

        // Set up the button action
        sendButton.setOnAction(e -> {
            try {
                // Get the host and port values
                String host = hostField.getText().trim();
                int port = Integer.parseInt(portField.getText().trim());

                if (host.isEmpty() || port <= 0) {
                    System.out.println("Invalid host or port!");
                    return;
                }

                // Set up the ISO message
                ISO87APackager packager = new ISO87APackager();
                ISOMsg isoMsg = new ISOMsg();
                isoMsg.setPackager(packager);

                // Set MTI
                isoMsg.setMTI("0200");

                // Map input fields to ISO message fields
                for (int i = 0; i < 127; i++) {
                    String value = textFields[i].getText();
                    if (!value.isEmpty()) {
                        isoMsg.set(i + 2, value);  // Field 2 to Field 128
                    }
                }

                // Pack the ISO message
                byte[] packedMessage = isoMsg.pack();

                // Set the new scene immediately (to show the sent/received data)
                Text sentText = new Text("Sent Data:\n" + Utils.formatData(packedMessage));
                Text receivedText = new Text("Received Data:\nNo response yet...");

                // Create back button
                Button backButton = new Button("Back");
                backButton.setOnAction(backEvent -> primaryStage.setScene(inputScene));

                // Layout for the send data scene
                VBox sendDataLayout = new VBox(20);
                sendDataLayout.getChildren().addAll(backButton, sentText, receivedText);

                sendDataScene = new Scene(sendDataLayout, 600, 400);
                primaryStage.setScene(sendDataScene);

                // Create and run a task to handle socket communication in a background thread
                Task<Void> sendMessageTask = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        // Send the message to the server and receive the response
                        String response = sendMessageToHost(host, port, packedMessage);

                        // Update the UI with the received data (run on the JavaFX Application Thread)
                        javafx.application.Platform.runLater(() -> {
                            // Update the received data text after response
                            receivedText.setText("Received Data:\n" + (response != null ? Utils.formatData(response.getBytes()) : "No response"));
                        });

                        return null;
                    }
                };

                // Run the task in a separate thread
                new Thread(sendMessageTask).start();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Set the input scene
        inputScene = new Scene(scrollPane, 600, 800);
    }

    private String sendMessageToHost(String host, int port, byte[] packedMessage) {
        try (Socket socket = new Socket(host, port)) {
            // Send message to the server
            OutputStream os = socket.getOutputStream();
            os.write(packedMessage);
            os.flush();

            // Receive response from the server (if any)
            InputStream is = socket.getInputStream();
            byte[] responseBytes = is.readAllBytes();
            return new String(responseBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
