package application;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.*;

public class Main extends Application {

    private WebEngine engine;
    private Stage primaryStage;

    private String folderPath;
    private String inputFilePath;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        WebView webView = new WebView();
        engine = webView.getEngine();

        engine.setJavaScriptEnabled(true);

        // Load HTML
        engine.load(getClass().getResource("index.html").toExternalForm());

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaApp", this);
            }
        });

        HostServices hostServices = getHostServices();
        engine.locationProperty().addListener((obs, oldLoc, newLoc) -> {
            if (newLoc.startsWith("http")) {
                hostServices.showDocument(newLoc);
                engine.load(oldLoc);
            }
        });

        stage.getIcons().add(new Image(getClass().getResourceAsStream("images/JaxelConverter_Logo32.png")));
        stage.setScene(new Scene(webView, 800, 600));
        stage.setTitle("Jaxel Converter");
        stage.show();
    }

    // file picker
    public void pickInputFile() {
        Platform.runLater(() -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select a file");

            File file = chooser.showOpenDialog(primaryStage);

            if (file != null) {
                inputFilePath = file.getAbsolutePath().replace("\\", "\\\\");

                String name = file.getName().replaceAll("\\.\\w+$", "");

                engine.executeScript("setFile('" + inputFilePath + "')");
                engine.executeScript("window.inputFileName = '" + name + "'");

                log("Selected file: " + inputFilePath);
            }
        });
    }

    // output button
    public void pickOutputFolder() {
        Platform.runLater(() -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select output folder");

            File folder = chooser.showDialog(primaryStage);

            if (folder != null) {
                folderPath = folder.getAbsolutePath();

                Object inputNameObj = engine.executeScript("window.inputFileName");
                String inputName = inputNameObj != null ? inputNameObj.toString() : "output";

                String outputPath = (folderPath + File.separator + inputName + ".vox")
                        .replace("\\", "\\\\");

                engine.executeScript("setFolder('" + outputPath + "')");

                log("Selected folder: " + folderPath);
            }
        });
    }

    public void selectFormat(String ext) {
        if (folderPath == null) return;

        Object inputNameObj = engine.executeScript("window.inputFileName");
        String inputName = inputNameObj != null ? inputNameObj.toString() : "output";

        String outputPath = (folderPath + File.separator + inputName + "." + ext)
                .replace("\\", "\\\\");

        engine.executeScript("setFolder('" + outputPath + "')");

        log("Format selected: " + ext);
    }

    // convert button
    public void convert(String command) {
        new Thread(() -> {
            try {
                File appDir = new File(Main.class.getProtectionDomain()
                        .getCodeSource().getLocation().toURI()).getParentFile();

                File exe = new File(appDir, "voxconvert/vengi-voxconvert.exe");

                if (!exe.exists()) {
                    log("ERROR: exe not found");
                    return;
                }

                log("Running:");
                log(command);

                ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
                builder.directory(exe.getParentFile());
                builder.redirectErrorStream(true);

                Process process = builder.start();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream())
                );

                String line;
                while ((line = reader.readLine()) != null) {
                    String finalLine = line;
                    Platform.runLater(() -> log(finalLine));
                }

                log("Conversion finished!");

            } catch (Exception e) {
                e.printStackTrace();
                log("Error: " + e.getMessage());
            }
        }).start();
    }

    // custom console html
    private void log(String message) {
        String safe = message.replace("\\", "\\\\").replace("'", "\\'");
        Platform.runLater(() ->
                engine.executeScript("consoleLog('" + safe + "')")
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}