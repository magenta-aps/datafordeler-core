package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import dk.magenta.datafordeler.core.io.ImportInputStream;
import dk.magenta.datafordeler.core.util.CloseDetectInputStream;
import it.sauronsoftware.ftp4j.*;
import it.sauronsoftware.ftp4j.connectors.HTTPTunnelConnector;
import org.apache.http.StatusLine;
import org.apache.logging.log4j.util.Strings;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by lars on 08-06-17.
 *
 * En klasse til at hente filer vha. FTP
 * Efter filer er hentet markeres de også med en filendelse, så de ikke hentes næste gang.
 */
public class FtpCommunicator implements Communicator {

    public static final String DONE_FILE_ENDING = ".done";

    protected String username;
    protected String password;
    protected boolean useFtps;
    protected String proxyString;
    private Path localCopyFolder;
    private SSLSocketFactory sslSocketFactory;
    private boolean keepFiles = false;

    public FtpCommunicator(String username, String password, boolean useFtps) throws DataStreamException {
        this(username, password, useFtps, null, null, false);
    }
    public FtpCommunicator(String username, String password, boolean useFtps,
        String proxyString, String localCopyFolder, boolean keepFiles) throws DataStreamException {
        this.username = username;
        this.password = password;
        this.useFtps = useFtps;
        this.proxyString = proxyString;

        if (localCopyFolder != null) {
            this.localCopyFolder = Paths.get(localCopyFolder);
        } else {
            try {
                this.localCopyFolder = createTempFolder();
            } catch (IOException e) {
                throw new DataStreamException("Unable to create temporary folder", e);
            }
        }
        System.out.println("localCopyFolder: "+this.localCopyFolder.toString());

        this.keepFiles = keepFiles;
    }

    public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    private static Path createTempFolder() throws IOException {
        File temp = File.createTempFile("dafo-"+UUID.randomUUID().toString(),"");
        temp.delete();
        temp.mkdir();
        return Paths.get(temp.getAbsolutePath());
    }

    protected void setupProxy(FTPClient ftpClient) throws DataStreamException {
        if (!Strings.isEmpty(proxyString)) {
            System.out.println("Setting proxy to "+proxyString);
            try {
                URI proxyURI = new URI(proxyString);
                HTTPTunnelConnector connector = new HTTPTunnelConnector(
                    proxyURI.getHost(), proxyURI.getPort()
                );
                ftpClient.setConnector(connector);
            }
            catch (Exception e) {
                throw new DataStreamException("Could not add proxy to FTP connection", e);
            }
        }
    }

    protected FTPClient performConnect(URI uri) throws IOException, DataStreamException {
        FTPClient ftpClient = new FTPClient();
        if (this.useFtps) {
            ftpClient.setSecurity(FTPClient.SECURITY_FTPS);
        }
        setupProxy(ftpClient);
        if (this.sslSocketFactory != null) {
            ftpClient.setSSLSocketFactory(this.sslSocketFactory);
        }

        try {
            if (uri.getPort() > 0) {
                ftpClient.connect(uri.getHost(), uri.getPort());
            } else {
                ftpClient.connect(uri.getHost());
            }

            ftpClient.login(this.username, this.password);

            return ftpClient;
        } catch (FTPIllegalReplyException | FTPException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Fetches all yet-unfetched files from the folder denoted by the uri,
     * and marks the fetched files as fetched when the returned InputStream is closed.
     */
    @Override
    public ImportInputStream fetch(URI uri) throws HttpStatusException, DataStreamException {
        try {
            FTPClient ftpClient = performConnect(uri);

            ftpClient.changeDirectory(uri.getPath());
            String[] list = ftpClient.listNames();
            List<String> remotePaths = Arrays.asList(list);
            remotePaths.sort(Comparator.naturalOrder());
            List<String> downloadPaths = this.filterFilesToDownload(remotePaths);

            ArrayList<File> currentFiles = new ArrayList<>();
            for (String path : downloadPaths) {
                String fileName = path.substring(path.lastIndexOf('/') + 1);
                File outputFile = Files.createFile(Paths.get(localCopyFolder.toString(), fileName)).toFile();
                ftpClient.download(path, outputFile);
                currentFiles.add(outputFile);
            }

            this.onBeforeBuildStream(ftpClient, currentFiles, uri, downloadPaths);
            InputStream inputStream = this.buildChainedInputStream(currentFiles);

            if (inputStream != null) {
                CloseDetectInputStream inputCloser = new CloseDetectInputStream(inputStream);
                inputCloser.addAfterCloseListener(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            onStreamClose(ftpClient, currentFiles, uri, downloadPaths);
                        } finally {
                            try {
                                ftpClient.disconnect(true);
                            } catch (IOException | FTPIllegalReplyException | FTPException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                return new ImportInputStream(inputCloser, currentFiles);
            } else {
                return new ImportInputStream(new ByteArrayInputStream("".getBytes()));
            }
        } catch (FTPException | FTPIllegalReplyException | FTPAbortedException | FTPDataTransferException | FTPListParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new DataStreamException(e);
        }
        return null;
    }

    protected void onBeforeBuildStream(FTPClient ftpClient, List<File> localFiles, URI uri, List<String> remoteFiles) {
    }

    protected void onStreamClose(FTPClient ftpClient, List<File> localFiles, URI uri, List<String> remoteFiles) {
        this.deleteLocalFiles(localFiles);
        this.markRemotefilesDone(ftpClient, uri, remoteFiles);
        this.markLocalFilesDone(localFiles);
    }

    @Override
    public StatusLine send(URI endpoint, String payload) throws IOException {
        throw new NotImplementedException();
    }

    protected Set<String> getLocalFilenameList() throws IOException {
        Set<String> knownFiles = new HashSet<>();
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(localCopyFolder);
        for(Path path : directoryStream) {
            if(Files.isRegularFile(path)) {
                knownFiles.add(path.getFileName().toString());
            }
        }
        return knownFiles;
    }

    protected InputStream buildChainedInputStream(List<File> files) throws IOException {
        List<File> filesToProcess = new ArrayList<>(files);
        filesToProcess.sort(Comparator.naturalOrder());

        InputStream inputStream = null;
        for (File file : filesToProcess) {
            if (!file.getName().endsWith(DONE_FILE_ENDING)) {
                Path filePath = Paths.get(file.toURI());
                InputStream newInputStream = Files.newInputStream(filePath);
                if (inputStream == null) {
                    inputStream = newInputStream;
                } else {
                    inputStream = new SequenceInputStream(inputStream, newInputStream);
                }
            }
        }
        return inputStream;
    }

    protected List<String> filterFilesToDownload(List<String> paths) throws IOException {
        Set<String> knownFiles = getLocalFilenameList();
        List<String> result = new ArrayList<>();
        for(String path : paths) {
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            if (!knownFiles.contains(fileName) && !knownFiles.contains(fileName + DONE_FILE_ENDING)) {
                result.add(path);
            }
        }
        return result;
    }

    private void deleteLocalFiles(List<File> localFiles) {
        if (!this.keepFiles) {
            for (File file : localFiles) {
                file.delete();
            }
        }
    }

    private void markLocalFilesDone(List<File> localFiles) {
        for (File file : localFiles) {
            String filename = file.getName();
            if (!filename.endsWith(DONE_FILE_ENDING)) {
                String doneFileName = filename + DONE_FILE_ENDING;
                try {
                    Files.move(
                            Paths.get(file.toURI()),
                            Paths.get(file.getParent(), doneFileName)
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void markRemotefilesDone(FTPClient ftpClient, URI folder, List<String> remoteFiles) {
        try {
            ftpClient.changeDirectory(folder.getPath());
            for (String file : remoteFiles){
                ftpClient.rename(file,file + DONE_FILE_ENDING);
            }
        } catch (IOException | FTPIllegalReplyException | FTPException e) {
            e.printStackTrace();
        }
    }
}
