package no.law;

import no.law.lawreference.NorwegianLawTextName_to_LawId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NorwegianLawTextName_to_LawIdTest {

    @Test
    public void lawName_negative_invalid() {
        Assertions.assertNull(NorwegianLawTextName_to_LawId.law(""));
    }

    @Test
    public void lawName_negative_nullInput() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            NorwegianLawTextName_to_LawId.law(null);
        });
    }

    @Test
    public void lawName_negative_invalidMonth() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            Assertions.assertNull(NorwegianLawTextName_to_LawId.law("lov 20. june 2014 nr. 49"));
        });
    }

    @Test
    public void lawName() {
        Assertions.assertEquals(
                "LOV-2014-06-20-49",
                NorwegianLawTextName_to_LawId.law("lov 20. juni 2014 nr. 49")
        );
    }
}
