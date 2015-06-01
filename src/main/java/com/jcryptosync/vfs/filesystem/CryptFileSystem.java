package com.jcryptosync.vfs.filesystem;

import com.jcryptosync.data.MetaData;
import com.jcryptosync.data.preferences.ContainerPreferences;
import com.jcryptosync.vfs.webdav.AbstractFile;
import com.jcryptosync.vfs.webdav.CryptFile;
import com.jcryptosync.vfs.webdav.Folder;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CryptFileSystem {

    private static final CryptFileSystem instance = new CryptFileSystem();
    protected static Logger log = Logger.getLogger(CryptFileSystem.class);

    private MetaData metaData;
    private ChangeEvents changeEvents;

    private CryptFileSystem() {
        metaData = MetaData.getInstance();

        if(metaData.getCountFiles() == 0) {
            Folder root = new Folder("Root", null);
            metaData.addFile(root);
            metaData.saveRootFolderId(root.getUniqueId());
        }
    }

    public static CryptFileSystem getInstance() {
        return instance;
    }

    public void createNewFile(CryptFile cryptFile, InputStream is) {


        String clientId = ContainerPreferences.getInstance().getClientId();
        cryptFile.getVector().increaseSynchronization(clientId);

        if(cryptFile.getLength() > 0) {
            cryptFile.getVector().increaseModification(clientId);
        }

        FileOperations.cryptFile(cryptFile, is);

        metaData.addFile(cryptFile);

        log.debug("added new file: " + cryptFile.getName());

        if(changeEvents != null) {
            changeEvents.changeFile(cryptFile);
        }
    }

    public void createNewFolder(Folder newFolder) {
        metaData.addFile(newFolder);
        log.debug("added folder file: " + newFolder.getName());

        if(changeEvents != null)
            changeEvents.changeFolder(newFolder);
    }

    public void getFileContent(CryptFile cryptFile, OutputStream os) {

        if(cryptFile.getLength() > 0)
            FileOperations.decryptFile(cryptFile, os);

        log.debug("get file content: " + cryptFile.getName());
    }

    public void updateFile(CryptFile cryptFile, InputStream is) {
        FileOperations.updateFile(cryptFile, is);

        log.debug("file updated: " + cryptFile.getName());

        cryptFile.setModDate(new Date());

        String clientId = ContainerPreferences.getInstance().getClientId();
        cryptFile.getVector().increaseModification(clientId);

        metaData.updateFile(cryptFile);

        if(changeEvents != null)
            changeEvents.changeFile(cryptFile);
    }

    public void copyFile(CryptFile cryptFile, Folder folder, String newName) {
        CryptFile newFile = FileOperations.copyFile(cryptFile, newName);
        newFile.setParentId(folder.getUniqueId());

        metaData.addFile(newFile);

        if(changeEvents != null)
            changeEvents.changeFile(newFile);
    }

    public void deleteFolder(Folder folder) {
        folder.setModDate(new Date());
        folder.setDeleted(true);
        metaData.updateFile(folder);

        log.debug("folder deleted: " + folder.getName());

        if(changeEvents != null)
            changeEvents.changeFolder(folder);
    }

    public void deleteFile(CryptFile cryptFile) {
        cryptFile.setModDate(new Date());
        cryptFile.setDeleted(true);

        String clientId = ContainerPreferences.getInstance().getClientId();
        cryptFile.getVector().increaseModification(clientId);

        FileOperations.deleteFile(cryptFile);

        metaData.updateFile(cryptFile);

        log.debug("file deleted: " + cryptFile.getName());

        if(changeEvents != null)
            changeEvents.changeFile(cryptFile);
    }

    public void renameFile(AbstractFile file, String name) {
        file.setModDate(new Date());

        log.debug(String.format("file rename from %s to %s ", file.getName(), name));

        file.setName(name);

        if(file instanceof CryptFile) {
            CryptFile cryptFile = (CryptFile) file;
            String clientId = ContainerPreferences.getInstance().getClientId();
            cryptFile.getVector().increaseModification(clientId);

            if(changeEvents != null && cryptFile.getLength() > 0)
                changeEvents.changeFile(cryptFile);
        } else {
            if(changeEvents != null)
                changeEvents.changeFolder((Folder) file);
        }

        metaData.updateFile(file);
    }

    public void moveFile(AbstractFile file, Folder folder, String name) {
        file.setModDate(new Date());

        file.setParentId(folder.getUniqueId());
        file.setName(name);

        if(file instanceof CryptFile) {
            CryptFile cryptFile = (CryptFile) file;
            String clientId = ContainerPreferences.getInstance().getClientId();
            cryptFile.getVector().increaseModification(clientId);

            if(changeEvents != null)
                changeEvents.changeFile(cryptFile);
        } else {
            if(changeEvents != null)
                changeEvents.changeFolder((Folder) file);
        }

        metaData.updateFile(file);

        log.debug(String.format("file moved: %s", name));
    }

    public Folder getRoot() {
        return (Folder) metaData.getFileById(metaData.getRootFolderId());
    }

    public List<AbstractFile> getChildren(Folder folder) {
            List<AbstractFile> children = metaData.getCollectionFiles().stream()
                    .filter(f -> folder.getUniqueId().equals(f.getParentId()))
                    .filter(f -> !f.isDeleted())
                    .collect(Collectors.toList());

            return children;
    }

    public AbstractFile getChild(Folder folder, String name) {

        Optional<AbstractFile> file = getChildren(folder).stream()
                .filter(f -> f.getName().equals(name))
                .filter(f -> !f.isDeleted())
                .findFirst();

        return file.orElse(null);
    }

    public void setChangeEvents(ChangeEvents changeEvents) {
        this.changeEvents = changeEvents;
    }

    public interface ChangeEvents {
        void changeFile(CryptFile file);
        void changeFolder(Folder folder);
    }
}
