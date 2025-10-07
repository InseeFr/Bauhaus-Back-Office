package fr.insee.rmes.infrastructure.stamps;

import fr.insee.rmes.onion.infrastructure.stamps.RmesStampsImpl;
import org.junit.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class RmesStampsImplTest {
    @Test
    public void shouldCheckAbsenceOfDuplicatesInStaticAttribute(){
        Set<String> set = new HashSet<>(RmesStampsImpl.stamps);
        assertNotEquals(RmesStampsImpl.stamps.size(),set.size());
    }
}