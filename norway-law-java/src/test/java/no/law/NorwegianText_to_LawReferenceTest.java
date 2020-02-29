package no.law;

import no.law.lawreference.LawReferenceFinder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static no.law.lawreference.NorwegianText_to_LawReference.textToLawReference;

public class NorwegianText_to_LawReferenceTest {
    @Test
    void refTest() {
        LawReferenceFinder lawRef;
        LocalDate date = LocalDate.of(2010, 1, 1);
        LocalDate date2 = LocalDate.of(2000, 1, 1);

        Assertions.assertEquals(
                new LawReferenceFinder(),
                textToLawReference("", date)
        );

        lawRef = new LawReferenceFinder("LOV-2006-05-19-16");
        Assertions.assertEquals(lawRef, textToLawReference("Offentleglova", date));
        Assertions.assertEquals(lawRef, textToLawReference("Offentleglova (2006)", date));
        Assertions.assertEquals(lawRef, textToLawReference("Offentleglova (2006) some other word", date));

        lawRef = new LawReferenceFinder("LOV-1970-06-19-69");
        Assertions.assertEquals(lawRef, textToLawReference("Offentleglova", date2));
        Assertions.assertEquals(lawRef, textToLawReference("Offentleglova §", date2));
        Assertions.assertEquals(lawRef, textToLawReference("Offentleglova § abc", date2));
        Assertions.assertEquals(lawRef, textToLawReference("Offentleglova some other word", date2));

        lawRef = new LawReferenceFinder("LOV-2006-05-19-16", "§ 1");
        Assertions.assertEquals(lawRef, textToLawReference("Offentleglova (2006) §1", date));
        Assertions.assertEquals(lawRef, textToLawReference("Offentleglova (2006) § 1", date));
        Assertions.assertEquals(lawRef, textToLawReference("Offentleglova (2006) paragraf1", date));
        Assertions.assertEquals(lawRef, textToLawReference("Offentleglova (2006) paragraf 1", date));
        Assertions.assertEquals(lawRef, textToLawReference("Offentleglova (2006) § 1 some other word", date));

        lawRef = new LawReferenceFinder("LOV-2006-05-19-16", "§ 2", "første");
        Assertions.assertEquals(lawRef, textToLawReference("Offentleglova (2006) § 2 første ledd", date));
        Assertions.assertEquals(lawRef, textToLawReference("Offentleglova (2006) § 2 første ledd some other word", date));
    }
}
