package no.law;

import no.law.lawreference.LawReferenceFinder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static no.law.lawreference.NorwegianText_to_LawReference.textToLawReference;

public class NorwegianText_to_LawReferenceTest {
    @Test
    void refTest() {
        LocalDate date = LocalDate.of(2010, 1, 1);

        Assertions.assertEquals(
                new LawReferenceFinder(),
                textToLawReference("", date)
        );

        LawReferenceFinder lawRef = new LawReferenceFinder("LOV-2006-05-19-16");
        Assertions.assertEquals(lawRef, textToLawReference("Offentleglova", date));
        Assertions.assertEquals(lawRef, textToLawReference("Offentleglova (2006)", date));

        lawRef = new LawReferenceFinder("LOV-2006-05-19-16", "§ 1");
        Assertions.assertEquals(lawRef, textToLawReference("Offentleglova (2006) §1", date));
        Assertions.assertEquals(lawRef, textToLawReference("Offentleglova (2006) § 1", date));
        Assertions.assertEquals(lawRef, textToLawReference("Offentleglova (2006) paragraf1", date));
        Assertions.assertEquals(lawRef, textToLawReference("Offentleglova (2006) paragraf 1", date));
    }
}
