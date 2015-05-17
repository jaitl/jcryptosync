package com.jcryptosync.controllers.login;

import com.jcryptosync.QuickPreferences;
import com.jcryptosync.container.primarykey.PrimaryKeyManager;
import com.jcryptosync.controllers.LoginSceneFactory;
import com.jcryptosync.controllers.StageFactory;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CreateKeyController extends BaseLoginController {


    @Override
    protected void changeControllerAction(ActionEvent event) {
        Node node=(Node) event.getSource();
        Stage stage=(Stage) node.getScene().getWindow();

        Scene scene = LoginSceneFactory.createLoginScene(getClass().getClassLoader());

        stage.setScene(scene);
    }

    @Override
    protected void selectKeyAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выбор расположения ключа");

        Path initDir = QuickPreferences.getPathToKey().getParent();

        if(initDir != null) {
            if (Files.exists(initDir)) {
                fileChooser.setInitialDirectory(initDir.toFile());
            }
        }

        File key = fileChooser.showSaveDialog(null);

        if(key != null) {
            pathToKey.setText(key.getPath());
            QuickPreferences.setPathToKey(key.getPath());
        }
    }

    @Override
    protected void selectContainerAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выбор расположения контейнера");

        Path initDir = QuickPreferences.getPathToContainer().getParent();

        if(initDir != null) {
            if (Files.exists(initDir)) {
                fileChooser.setInitialDirectory(initDir.toFile());
            }
        }

        File container = fileChooser.showSaveDialog(null);

        if(container != null) {
            pathToContainer.setText(container.getPath());
            QuickPreferences.setPathToContainer(container.getPath());
        }
    }

    @Override
    protected void executeAction(ActionEvent event) {
        clearErrors();

        if(checkFields()) {
            try {
                PrimaryKeyManager keyManager = new PrimaryKeyManager();
                keyManager.saveNewCryptKeyToFile(firstPassword.getText(), Paths.get(pathToKey.getText()));

                Stage stage = StageFactory.createContainerStage(keyManager.getClass().getClassLoader());
                stage.show();

                QuickPreferences.setPathToContainer(pathToContainer.getText());
                QuickPreferences.setPathToKey(pathToKey.getText());

                        ((Node) (event.getSource())).getScene().getWindow().hide();

            } catch (IOException e) {
                setError("Ошибка при сохранении ключа", pathToKey);
            }
        }
    }

    @Override
    protected boolean checkFields() {

        if(!super.checkFields())
            return false;

        if(!firstPassword.getText().equals(secondPassword.getText())) {
            setError("Пароли не совпадают", secondPassword);
            return false;
        }

        return true;
    }


    @Override
    public void prepareDialog() {
        super.prepareDialog();

        isNewContainer.setManaged(false);
        isNewContainer.setVisible(false);

        showSecondPassword();
        createButton.setText("Отменить");
        enterButton.setText("Создать");
    }

    private void showSecondPassword() {
        secondPasswordContainer.setManaged(true);
        secondPassword.setManaged(true);
        secondPassword.setVisible(true);
        secondPasswordLabel.setManaged(true);
        secondPasswordLabel.setVisible(true);
    }
}