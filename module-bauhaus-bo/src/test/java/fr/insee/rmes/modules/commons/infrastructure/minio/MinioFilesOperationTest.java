package fr.insee.rmes.modules.commons.infrastructure.minio;

import fr.insee.rmes.exceptions.RmesFileException;
import fr.insee.rmes.modules.commons.domain.model.Document;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;

import static org.mockito.Mockito.mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioFilesOperationTest {

    @Mock
    private MinioClient minioClient;

    private MinioFilesOperation minioFilesOperation;

    private static final String BUCKET_NAME = "test-bucket";

    @BeforeEach
    void setUp() {
        minioFilesOperation = new MinioFilesOperation(minioClient, BUCKET_NAME);
    }

    @Test
    void test_read_should_call_minio_client_with_correct_parameters() throws Exception {
        Document document = new Document("test/path", "file.txt");
        GetObjectResponse mockResponse = mock(GetObjectResponse.class);

        doReturn(mockResponse).when(minioClient).getObject(any(GetObjectArgs.class));

        InputStream result = minioFilesOperation.read(document);

        assertThat(result).isEqualTo(mockResponse);

        ArgumentCaptor<GetObjectArgs> captor = ArgumentCaptor.forClass(GetObjectArgs.class);
        verify(minioClient).getObject(captor.capture());
    }

    @Test
    void test_read_should_throw_rmes_file_exception_when_minio_client_throws_exception() throws Exception {
        Document document = new Document("test/path", "file.txt");

        doThrow(new IOException("Minio error"))
            .when(minioClient).getObject(any(GetObjectArgs.class));

        assertThatThrownBy(() -> minioFilesOperation.read(document))
            .isInstanceOf(RmesFileException.class)
            .hasMessageContaining("Error reading file")
            .hasMessageContaining("test/path/file.txt")
            .hasMessageContaining(BUCKET_NAME);
    }

    @Test
    void test_write_should_call_minio_client_with_correct_parameters() throws Exception {
        Document document = new Document("test/path", "file.txt");
        InputStream content = new ByteArrayInputStream("test content".getBytes());

        minioFilesOperation.write(content, document);

        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(captor.capture());
    }

    @Test
    void test_write_should_throw_rmes_file_exception_when_minio_client_throws_exception() throws Exception {
        Document document = new Document("test/path", "file.txt");
        InputStream content = new ByteArrayInputStream("test content".getBytes());

        doThrow(new IOException("Minio error"))
            .when(minioClient).putObject(any(PutObjectArgs.class));

        assertThatThrownBy(() -> minioFilesOperation.write(content, document))
            .isInstanceOf(RmesFileException.class)
            .hasMessageContaining("Error writing file")
            .hasMessageContaining("file.txt")
            .hasMessageContaining(BUCKET_NAME);
    }

    @Test
    void test_copy_should_call_minio_client_with_correct_parameters() throws Exception {
        Document srcDocument = new Document("source/path", "source.txt");
        Document targetDocument = new Document("target/path", "target.txt");

        minioFilesOperation.copy(srcDocument, targetDocument);

        ArgumentCaptor<CopyObjectArgs> captor = ArgumentCaptor.forClass(CopyObjectArgs.class);
        verify(minioClient).copyObject(captor.capture());
    }

    @Test
    void test_copy_should_throw_rmes_file_exception_when_minio_client_throws_exception() throws Exception {
        Document srcDocument = new Document("source/path", "source.txt");
        Document targetDocument = new Document("target/path", "target.txt");

        doThrow(new IOException("Minio error"))
            .when(minioClient).copyObject(any(CopyObjectArgs.class));

        assertThatThrownBy(() -> minioFilesOperation.copy(srcDocument, targetDocument))
            .isInstanceOf(RmesFileException.class)
            .hasMessageContaining("Error copying file")
            .hasMessageContaining("source/path/source.txt")
            .hasMessageContaining("target/path/target.txt")
            .hasMessageContaining(BUCKET_NAME);
    }

    @Test
    void test_exists_should_return_true_when_document_exists() throws Exception {
        Document document = new Document("test/path", "file.txt");
        StatObjectResponse statObjectResponse = mock(StatObjectResponse.class);
        when(statObjectResponse.size()).thenReturn(100L);

        doReturn(statObjectResponse).when(minioClient).statObject(any(StatObjectArgs.class));

        boolean result = minioFilesOperation.exists(document);

        assertThat(result).isTrue();
        verify(minioClient).statObject(any(StatObjectArgs.class));
    }

    @Test
    void test_exists_should_return_false_when_document_does_not_exist() throws Exception {
        Document document = new Document("test/path", "file.txt");

        doThrow(mock(ErrorResponseException.class))
            .when(minioClient).statObject(any(StatObjectArgs.class));

        boolean result = minioFilesOperation.exists(document);

        assertThat(result).isFalse();
    }

    @Test
    void test_exists_should_return_false_when_minio_throws_io_exception() throws Exception {
        Document document = new Document("test/path", "file.txt");

        doThrow(new IOException("Connection error"))
            .when(minioClient).statObject(any(StatObjectArgs.class));

        boolean result = minioFilesOperation.exists(document);

        assertThat(result).isFalse();
    }

    @Test
    void test_exists_with_string_should_always_return_true() {
        boolean result = minioFilesOperation.exists("/any/path");

        assertThat(result).isTrue();
        verifyNoInteractions(minioClient);
    }

    @Test
    void test_delete_should_call_minio_client_with_correct_parameters() throws Exception {
        Document document = new Document("test/path", "file.txt");

        minioFilesOperation.delete(document);

        ArgumentCaptor<RemoveObjectArgs> captor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
        verify(minioClient).removeObject(captor.capture());
    }

    @Test
    void test_delete_should_throw_rmes_file_exception_when_minio_client_throws_exception() throws Exception {
        Document document = new Document("test/path", "file.txt");

        doThrow(new IOException("Minio error"))
            .when(minioClient).removeObject(any(RemoveObjectArgs.class));

        assertThatThrownBy(() -> minioFilesOperation.delete(document))
            .isInstanceOf(RmesFileException.class)
            .hasMessageContaining("Error deleting file")
            .hasMessageContaining("test/path/file.txt")
            .hasMessageContaining(BUCKET_NAME);
    }
}
