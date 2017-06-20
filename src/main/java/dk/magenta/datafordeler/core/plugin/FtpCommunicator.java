package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import dk.magenta.datafordeler.core.util.CloseDetectInputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.http.StatusLine;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;


import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.StringJoiner;

/**
 * Created by lars on 08-06-17.
 */
public class FtpCommunicator implements Communicator {

    private String username;
    private String password;
    private boolean useFtps;

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
        int port = uri.getPort() != -1 ? uri.getPort() : 21;
        CloseDetectInputStream data = null;
        try {
            ftpClient.connect(host, port);
            this.checkServerReply(ftpClient, "FTP server "+host+":"+port+" rejected connection");
            ftpClient.login(this.username, this.password);
            this.checkServerReply(ftpClient, "FTP server "+host+":"+port+" rejected login for user " + this.username);
            InputStream inputStream = ftpClient.retrieveFileStream(uri.getPath());
            data = new CloseDetectInputStream(inputStream);
            data.addAfterCloseListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        ftpClient.completePendingCommand();
                        ftpClient.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            throw new DataStreamException(e);
        }
        return data;
    }

    @Override
    public StatusLine send(URI endpoint, String payload) throws IOException {
        throw new NotImplementedException();
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
