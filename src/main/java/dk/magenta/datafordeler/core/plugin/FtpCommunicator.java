package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import dk.magenta.datafordeler.core.util.CloseDetectInputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.http.StatusLine;
import org.springframework.beans.factory.annotation.Value;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;


import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URI;
import java.util.*;

/**
 * Created by lars on 08-06-17.
 *
 * En klasse til at hente filer vha. FTP
 * Efter filer er hentet markeres de også med en filendelse, så de ikke hentes næste gang.
 */
public class FtpCommunicator implements Communicator {

    private String username;
    private String password;
    private boolean useFtps;
    private String DONE_FILE_ENDING = ".done";

    String ftpHttpConnectProxy = "http://localhost:8888";

    public FtpCommunicator(String username, String password) {
        this(username, password, false);
    }

    public FtpCommunicator(String username, String password, boolean useFtps) {
        this.username = username;
        this.password = password;
        this.useFtps = useFtps;
    }

    @Override
    public InputStream fetch(URI uri) throws HttpStatusException, DataStreamException {
        FTPClient ftpClient = this.useFtps ? new FTPSClient(true) : new FTPClient();
        String host = uri.getHost();
        if(ftpHttpConnectProxy != null && ftpHttpConnectProxy != "") {
            try {
                URI proxyURI = new URI(ftpHttpConnectProxy);
                Proxy proxy = new Proxy(
                    Type.HTTP,
                    new InetSocketAddress(proxyURI.getHost(), proxyURI.getPort())
                );
                ftpClient.setProxy(proxy);
            }
            catch(Exception e) {
                throw new DataStreamException("Could not add proxy to FTP connection", e);
            }
        }
        int port = uri.getPort() != -1 ? uri.getPort() : (this.useFtps ? 990 : 21);
        CloseDetectInputStream data = null;
        try {
            // Print out FTP communication
            ftpClient.addProtocolCommandListener(new PrintCommandListener(
                System.out, true
            ));
            ftpClient.connect(host, port);
            this.checkServerReply(ftpClient, "FTP server "+host+":"+port+" rejected connection");
            ftpClient.login(this.username, this.password);
            this.checkServerReply(ftpClient, "FTP server "+host+":"+port+" rejected login for user " + this.username);
            ftpClient.enterLocalPassiveMode();
            //ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);

            String path = uri.getPath();

            List<String> fileNamesUnfiltered = Arrays.asList(ftpClient.listNames(uri.getPath()));
            List<String> fileNames = filter(fileNamesUnfiltered, DONE_FILE_ENDING);

            fileNames.sort(Comparator.naturalOrder());

            InputStream inputStream = null;

            if(fileNames.size() < 1){
                //TODO handle no files
            } else if (fileNames.size() == 1){
                inputStream = ftpClient.retrieveFileStream(fileNames.get(0));
                ftpClient.completePendingCommand();
            } else if (fileNames.size() > 1) {

                //To initialize the SequenceInputStream, 2 InputStreams are needed
                InputStream firstFile  = ftpClient.retrieveFileStream(fileNames.get(0));
                ftpClient.completePendingCommand();

                InputStream secondFile = ftpClient.retrieveFileStream(fileNames.get(1));
                ftpClient.completePendingCommand();

                inputStream = new SequenceInputStream(firstFile,secondFile);
                for (int i = 2; i < fileNames.size(); i++) {
                    inputStream = new SequenceInputStream(inputStream, ftpClient.retrieveFileStream(fileNames.get(i)));
                    ftpClient.completePendingCommand();
                }
            }
            if (inputStream != null) {
                data = new CloseDetectInputStream(inputStream);
                data.addAfterCloseListener(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //markFilesAsDone(ftpClient, uri, fileNames, DONE_FILE_ENDING);
                            ftpClient.disconnect();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (IOException e) {
            throw new DataStreamException(e);
        }
        return data;
    }

    @Override
    public StatusLine send(URI endpoint, String payload) throws IOException {
        throw new NotImplementedException();
    }

    /**
     * Laver en ArrayList<String> med alle de filnavne, der ikke har ending i slutningen af filnavnet
     * @param list En liste af filnavne i String[] form
     * @param ending Filnavnets endelse, på de filer der ikke skal bruges
     * @return
     */
    private List<String> filter(List<String> list, String ending){
        List<String> returnValue = new ArrayList<>();
        for (int i = list.size()-1; i >= 0; i--) {
            if(!list.get(i).endsWith(ending)){
                returnValue.add(list.get(i));
            }
        }
        return returnValue;
    }

    /**
     * Omdøber filer, der er hentet og parset korrekt.
     * @param ftpClient
     * @param uri
     * @param files
     * @param ending
     * @throws IOException
     */
    private void markFilesAsDone(FTPClient ftpClient, URI uri, List<String> files, String ending) throws IOException {

        ftpClient.changeWorkingDirectory(uri.getPath());
        for (String file : files){
            ftpClient.rename(file,file+ending);
        }
    }

    private void checkServerReply(FTPClient ftpClient, String errorMessage) throws DataStreamException {
        int replyCode = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            String[] replyStrings = ftpClient.getReplyStrings();
            StringJoiner stringJoiner = new StringJoiner("\n");
            stringJoiner.add(errorMessage);
            stringJoiner.add("Reply code: " + replyCode);
            if (replyStrings != null) {
                stringJoiner.add("Reply message: " + String.join("\n               ", replyStrings));
            }
            throw new DataStreamException(
                    stringJoiner.toString()
            );
        }
    }
}
