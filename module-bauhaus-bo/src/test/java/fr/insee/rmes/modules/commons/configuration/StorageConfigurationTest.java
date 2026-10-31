package fr.insee.rmes.modules.commons.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.insee.rmes.modules.commons.healthcheck.DependencyProbe;
import fr.insee.rmes.modules.commons.infrastructure.minio.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;

/** La sonde MinIO du healthcheck vérifie que le bucket des documents est joignable. */
class StorageConfigurationTest {

    private final MinioClient minioClient = mock(MinioClient.class);

    private final DependencyProbe probe = new StorageConfiguration()
            .minioProbe(minioClient, new MinioProperties("http://minio:9000", "user", "secret", "bauhaus"));

    @Test
    void shouldBeNamedMinio() {
        assertThat(probe.name()).isEqualTo("MinIO");
    }

    @Test
    void shouldPassWhenTheBucketExists() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        assertThatCode(() -> probe.check().run()).doesNotThrowAnyException();
    }

    @Test
    void shouldFailWhenTheBucketIsMissing() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        assertThatThrownBy(() -> probe.check().run()).hasMessageContaining("bauhaus");
    }

    @Test
    void shouldFailWhenMinioIsUnreachable() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenThrow(new IllegalStateException("refused"));

        assertThatThrownBy(() -> probe.check().run()).isInstanceOf(IllegalStateException.class);
    }
}
