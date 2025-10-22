package fr.insee.rmes.infrastructure.stamps;

import fr.insee.rmes.config.auth.security.JwtProperties;
import fr.insee.rmes.config.auth.security.UserDecoder;
import fr.insee.rmes.onion.infrastructure.stamps.RmesStampsImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class RmesStampsImplTest {
    
    @Test
    public void shouldCheckAbsenceOfDuplicatesInStamps(){
        // Given
        UserDecoder mockUserDecoder = Mockito.mock(UserDecoder.class);
        JwtProperties jwtProperties = new JwtProperties();

        RmesStampsImpl rmesStamps = new RmesStampsImpl(mockUserDecoder, jwtProperties);
        
        // When
        List<String> stamps = rmesStamps.getStamps();
        Set<String> uniqueStamps = new HashSet<>(stamps);
        
        // Then
        assertNotEquals(stamps.size(), uniqueStamps.size(), "Il devrait y avoir des doublons dans la liste des timbres");
    }

}