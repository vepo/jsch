package com.jcraft.jsch;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

@Testcontainers
public class SftpTest {

    private static final int PORT = 22;
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private static final String FILE_NAME = "test.txt";
    private static final String REMOTE_PATH = "/upload/";
    @TempDir
    private static File directory;
    private static final GenericContainer<?> sftp = new GenericContainer<>(new ImageFromDockerfile()
                                                                                                    .withDockerfileFromBuilder(builder -> builder.from("atmoz/sftp:latest")
                                                                                                                                                 .run("mkdir -p /home/"
                                                                                                                                                         + USER
                                                                                                                                                         + "/upload; chmod -R 007 /home/"
                                                                                                                                                         + USER)
                                                                                                                                                 .build()))
                                                                                                                                                           // .withFileSystemBind(sftpHomeDirectory.getAbsolutePath(),
                                                                                                                                                           // "/home/"
                                                                                                                                                           // +
                                                                                                                                                           // USER
                                                                                                                                                           // +
                                                                                                                                                           // REMOTE_PATH,
                                                                                                                                                           // BindMode.READ_WRITE)
                                                                                                                                                           // uncomment
                                                                                                                                                           // to
                                                                                                                                                           // mount
                                                                                                                                                           // host
                                                                                                                                                           // directory
                                                                                                                                                           // -
                                                                                                                                                           // not
                                                                                                                                                           // required
                                                                                                                                                           // /
                                                                                                                                                           // recommended
                                                                                                                                                           .withExposedPorts(PORT)
                                                                                                                                                           .withCommand(USER
                                                                                                                                                                   + ":"
                                                                                                                                                                   + PASSWORD
                                                                                                                                                                   + ":1001:::upload");

    @BeforeAll
    public static void staticSetup() throws IOException {
        File sftpTestFile = new File(directory.getAbsolutePath() + "/" + FILE_NAME);
        sftpTestFile.createNewFile();
        // copy your files to the sftp
        sftp.withCopyFileToContainer(MountableFile.forHostPath(sftpTestFile.getPath()),
                                     "/home/" + USER + "/upload/" + sftpTestFile.getName());
        sftp.start();
    }

    @Test
    void sftpTest() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(USER, sftp.getHost(), sftp.getMappedPort(PORT));
        session.connect();

        Channel channel = session.openChannel("sftp");
        channel.connect();
    }

    @AfterAll
    static void afterAll() {
        sftp.stop();
    }
}
