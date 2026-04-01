package application;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Main extends Application {

    private WebEngine engine;
    private String folderPath;
    private String inputFilePath;
    
    String inputPath;
    String outputPath;
    
    String escapedOutputPath;
    
    String customArgsInput;
    
    WebView webView;

    @Override
    public void start(Stage stage) {

        webView = new WebView();
        engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);
        
        
        stage.getIcons().add(new Image(getClass().getResourceAsStream("images/JaxelConverter_Logo16.png")));
        stage.getIcons().add(new Image(getClass().getResourceAsStream("images/JaxelConverter_Logo32.png")));
        stage.getIcons().add(new Image(getClass().getResourceAsStream("images/JaxelConverter_Logo64.png")));
        stage.getIcons().add(new Image(getClass().getResourceAsStream("images/JaxelConverter_Logo128.png")));
        
        webView.setContextMenuEnabled(false);

        engine.load(getClass().getResource("index.html").toExternalForm());

        engine.locationProperty().addListener((obs, oldLoc, newLoc) -> {
            try {
                if (newLoc.contains("pickFile")) {
                    pickInputFile(stage);
                } else if (newLoc.contains("pickFolder")) {
                    pickOutputFolder(stage);
                } else if (newLoc.contains("convertCustomArgs")) {
                	convertCustomArgs(newLoc);
                } else {
                    handleFormatSelection(newLoc);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log("Error: " + e.getMessage());
            }
        });

        stage.setScene(new Scene(webView, 800, 600));
        stage.setTitle("Jaxel Converter by: PromasterYTJava");
        stage.show();
        
        HostServices hostServices = getHostServices();

        webView.getEngine().locationProperty().addListener((obs, oldLoc, newLoc) -> {
            if (newLoc.startsWith("http")) {
                hostServices.showDocument(newLoc);

                webView.getEngine().load(oldLoc);
            }
        });
    }

    // File Input
    private void pickInputFile(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select a file");
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            inputFilePath = file.getAbsolutePath();
            log("Selected file: " + inputFilePath);
            inputFilePath = inputFilePath.replace("\\", "\\\\");
            Platform.runLater(() -> engine.executeScript("setFile('" + inputFilePath + "');"));

            String inputFileName = file.getName().replaceAll("\\.\\w+$", "");
            Platform.runLater(() -> engine.executeScript("window.inputFileName = '" + inputFileName + "';"));

            if (folderPath != null) enableFormatButtons();
        }
    }

    // Folder Output
    private void pickOutputFolder(Stage stage) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select a folder");
        File folder = chooser.showDialog(stage);
        if (folder != null) {
            folderPath = folder.getAbsolutePath();
            log("Selected directory: " + folderPath);

            Object inputFileNameObj = engine.executeScript("window.inputFileName");
            String inputFileName = inputFileNameObj != null ? inputFileNameObj.toString() : "output";

            String outputPath = folderPath + File.separator + inputFileName + ".vox";
            escapedOutputPath = outputPath.replace("\\", "\\\\");

            Platform.runLater(() -> engine.executeScript("setFolder('" + escapedOutputPath + "');"));

            if (inputFilePath != null) enableFormatButtons();
        }
    }

    private void enableFormatButtons() {
        Platform.runLater(() -> engine.executeScript("updateFormatButtons()"));
    }

    private void handleFormatSelection(String newLoc) {
        Map<String, String> formatMap = new HashMap<>();
        formatMap.put("slct_obj_file", "obj");
        formatMap.put("slct_vox_file", "vox");
        formatMap.put("slct_vxl_file", "vxl");
        formatMap.put("slct_qb_file", "qb");
        formatMap.put("slct_binvox_file", "binvox");
        formatMap.put("slct_kv6_file", "kv6");
        formatMap.put("slct_v3a_file", "v3a");
        formatMap.put("slct_fbx_file", "fbx");
        formatMap.put("slct_gltf_file", "gltf");
        formatMap.put("slct_stl_file", "stl");
        formatMap.put("slct_ply_file", "ply");

        for (String key : formatMap.keySet()) {
            if (newLoc.contains(key)) {
                String ext = formatMap.get(key);

                Object inputFileNameObj = engine.executeScript("window.inputFileName");
                String inputFileName = inputFileNameObj != null ? inputFileNameObj.toString() : "output";

                String outputPath = folderPath + File.separator + inputFileName + "." + ext;
                String FormatoutputPath = outputPath.replace("\\", "\\\\");
                Platform.runLater(() -> {
                    engine.executeScript("setFolder('" + FormatoutputPath + "');");
                    engine.executeScript("consoleLog('File Format Selected: " + ext + "');");
                });
                break;
            }
         }
    }
    
    private void convertCustomArgs(String newLoc) throws Exception {

        Platform.runLater(() -> {
            try {
                Object jsResult = engine.executeScript("document.getElementById('customArgsInput').value");
                String cmd = (jsResult != null) ? jsResult.toString().trim() : "";

                if (cmd.isEmpty()) {
                    log("Please enter valid arguments!");
                    return;
                }

                String firstWord = cmd.split("\\s+")[0];

                if (!firstWord.startsWith("vengi-voxconvert")) {
                    log("Invalid argument type");
                    return;
                }

                File appDir = new File(Main.class.getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .toURI())
                        .getParentFile();

                File voxDir = new File(appDir, "voxconvert");
                File exe = new File(voxDir, "vengi-voxconvert.exe");

                if (!exe.exists()) {
                    log("ERROR: exe not found");
                    return;
                }

                log("Running command:");
                log(cmd);

                ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", cmd);
                builder.directory(voxDir);
                builder.redirectErrorStream(true);

                Process process = builder.start();

                new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String finalLine = line;
                            Platform.runLater(() -> log(finalLine));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();

                log("Conversion started!");

            } catch (Exception e) {
                e.printStackTrace();
                log("Error: " + e.getMessage());
            }
        });
    }

    // Log to html console cuz rizz
    private void log(String message) {
        Platform.runLater(() -> {
            try {
                if (engine != null) {
                	String escapedMessage = message.replace("\\", "\\\\").replace("'", "\\'");
                	Platform.runLater(() -> {
                	    webView.getEngine().executeScript("consoleLog('" + escapedMessage + "')");
                	});
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}