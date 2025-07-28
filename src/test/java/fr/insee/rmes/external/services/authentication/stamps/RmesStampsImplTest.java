package fr.insee.rmes.external.services.authentication.stamps;

import org.junit.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class RmesStampsImplTest {

    @Test
   public void shouldCheckAbsenceOfDuplicatesInStaticAttribute(){
        Set set = new HashSet();
        set.addAll(RmesStampsImpl.stamps);
        assertTrue(RmesStampsImpl.stamps.size()!=set.size());
    }



}