package subtitle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        List<String> subFormats = new ArrayList<>();
        subFormats.add(".aqt");
        subFormats.add(".cvd");
        subFormats.add(".dks");
        subFormats.add(".jss");
        subFormats.add(".sub");
        subFormats.add(".ttxt");
        subFormats.add(".mpl");
        subFormats.add(".pjs");
        subFormats.add(".psb");
        subFormats.add(".rt");
        subFormats.add(".smi");
        subFormats.add(".ssf");
        subFormats.add(".srt");
        subFormats.add(".ssa");
        subFormats.add(".svcd");
        subFormats.add(".usf");
        subFormats.add(".idx");
        subFormats.add(".divx");

        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        primaryStage.setTitle("Submatcher");
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(
                new Image(Main.class.getResourceAsStream("/drawable/title_icon.png"))
        );
        Scene scene = new Scene(root, 500, 275);
        primaryStage.setScene(scene);
        primaryStage.show();

        TextArea log = (TextArea) scene.lookup("#log_area");

        Button pathButton = (Button) scene.lookup("#path_button");
        pathButton.setOnAction(event -> {
            TextField pathField = (TextField) scene.lookup("#path_subs");
            DirectoryChooser folderChooser = new DirectoryChooser();
            if (pathField.getText() != null && !pathButton.getText().equals("")) {
                File initialDirectory = new File(pathField.getText() + "..");
                if (initialDirectory.isDirectory())
                    folderChooser.setInitialDirectory(initialDirectory);
            }
            folderChooser.setTitle("Select Subtitle folder");
            File selectedDir = folderChooser.showDialog(scene.getWindow());
            if (selectedDir != null) {
                try {
                    String path = selectedDir.getCanonicalPath();
                    if (!path.endsWith(File.separator))
                        path += File.separator;
                    pathField.setText(path);
                    log.clear();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        Button start = (Button) scene.lookup("#start");
        start.setOnAction(event -> {
            log.clear();
            TextField pathField = (TextField) scene.lookup("#path_subs");
            String path = pathField.getText();
            //log.setWrapText(true);
            File subtitlePath = new File(path);

            if (subtitlePath.isDirectory()) {
                List<File> filesArrVids = new ArrayList<>(), filesArrSubs = new ArrayList<>();
                try (Stream<Path> paths = Files.walk(Paths.get(path))) {
                    paths.forEach(filePath -> {
                        if (Files.isRegularFile(filePath)) {
                            File f = filePath.toFile();
                            boolean isAlreadyAdded = false;
                            for (String suffix : subFormats)
                                if (f.getName().endsWith(suffix)) {
                                    filesArrSubs.add(filePath.toFile());
                                    isAlreadyAdded = true;
                                }
                            if (!isAlreadyAdded) {
                                filesArrVids.add(filePath.toFile());
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                filesArrVids.forEach((f) -> {
                    Pattern p = Pattern.compile("S[0-9][0-9]E[0-9][0-9]");
                    for (int i = 0; i < f.getName().length() - 5; i++) {
                        String curMatch = f.getName().substring(i, i + 6);
                        Matcher m = p.matcher(curMatch);

                        if (m.matches()) {
                            filesArrSubs.forEach((sub) -> {
                                if (sub.getName().contains(curMatch)) {
                                    String fileNameWithOutExt = f.getName().replaceFirst("[.][^.]+$", "");
                                    String subExtension = sub.getName().substring(sub.getName().lastIndexOf(".") + 1);
                                    //noinspection ResultOfMethodCallIgnored
                                    sub.renameTo(new File(path + fileNameWithOutExt + "." + subExtension));
                                    log.appendText("Renamed: " + sub.toString() + '\n');
                                }
                            });
                        }
                    }
                });
                log.appendText('\n' + "Renaming Successful. Enjoy watching the show!");
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.open(subtitlePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                log.setText("The directory \"" + path + "\" does not exist. Please choose the correct folder");
            }
        });

    }


    public static void main(String[] args) {
        launch(args);
    }
}
