package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.TestConfig;
import dk.magenta.datafordeler.core.util.CloseDetectInputStream;
import java.nio.file.FileSystems;
import org.apache.commons.io.FileUtils;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.PasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by lars on 08-06-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = TestConfig.class)
public class FtpCommunicatorTest {

    @Test
    public void testDownload() throws Exception {
        String username = "test";
        String password = "test";
        int port = 2101;
        String contents = "this is a test æøåÆØÅ!";
        File tempFile = File.createTempFile("ftpdownload","test");
        tempFile.createNewFile();

        FileWriter fileWriter = new FileWriter(tempFile);
        fileWriter.write(contents);
        fileWriter.close();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                FtpCommunicatorTest.this.startServer(username, password, port, Collections.singletonList(tempFile));
                FtpCommunicator ftpCommunicator = new FtpCommunicator(username, password, true);
                CloseDetectInputStream inputStream = ftpCommunicator.fetch(new URI("ftp://localhost:"+port+"/" + tempFile.getName()));
                String data = new Scanner(inputStream,"UTF-8").useDelimiter("\\A").next();
                inputStream.close();
                Assert.assertEquals(contents, data);
                return true;
            }
        });
        future.get(10, TimeUnit.SECONDS);
        executorService.shutdownNow();
        this.stopServer();
        tempFile.delete();
    }

    private FtpServer server = null;
    private File usersFile = null;
    private File tempDir = null;

    public void startServer(String username, String password, int port, List<File> files) throws Exception {
        /**
         * Cribbed from https://stackoverflow.com/questions/8969097/writing-a-java-ftp-server#8970126
         */
        if (this.server != null) {
            throw new Exception("Server is already running");
        }
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory factory = new ListenerFactory();
        factory.setPort(port);

        SslConfigurationFactory ssl = new SslConfigurationFactory();
        ssl.setKeystoreFile(new File(ClassLoader.getSystemResource("test.jks").toURI()));
        ssl.setKeystorePassword("password");
        factory.setSslConfiguration(ssl.createSslConfiguration());
        factory.setImplicitSsl(true);

        serverFactory.addListener("default", factory.createListener());
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        this.usersFile = File.createTempFile("ftpusers", ".properties");
        userManagerFactory.setFile(this.usersFile);//choose any. We're telling the FTP-server where to read it's user list
        userManagerFactory.setPasswordEncryptor(new PasswordEncryptor() {//We store clear-text passwords in this example
            @Override
            public String encrypt(String password) {
                return password;
            }
            @Override
            public boolean matches(String passwordToCheck, String storedPassword) {
                return passwordToCheck.equals(storedPassword);
            }
        });
        //Let's add a user, since our myusers.properties files is empty on our first test run
        BaseUser user = new BaseUser();
        user.setName(username);
        user.setPassword(password);

        if(FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            this.tempDir = Files.createTempDirectory(
                null,
                PosixFilePermissions.asFileAttribute(
                    PosixFilePermissions.fromString("rwxrwxrwx")
                )
            ).toFile();
        } else {
            this.tempDir = Files.createTempDirectory(null).toFile();
        }


        for (File sourcefile : files) {
            File destFile = new File(this.tempDir, sourcefile.getName());
            FileUtils.copyFile(sourcefile, destFile);
        }

        user.setHomeDirectory(this.tempDir.toString());
        List<Authority> authorities = new ArrayList<>();
        authorities.add(new WritePermission());
        user.setAuthorities(authorities);
        UserManager um = userManagerFactory.createUserManager();
        try {
            um.save(user);//Save the user to the user list on the filesystem
        } catch (FtpException e1) {
            //Deal with exception as you need
        }
        serverFactory.setUserManager(um);
        this.server = serverFactory.createServer();
        try {
            this.server.start();//Your FTP server starts listening for incoming FTP-connections, using the configuration options previously set
        } catch (FtpException ex) {
        }
    }

    private void stopServer() {
        if (this.server != null) {
            this.server.stop();
            this.server = null;
        }
        if (this.usersFile != null) {
            this.usersFile.delete();
        }
        if (this.tempDir != null) {
            this.tempDir.delete();
        }
    }
}
