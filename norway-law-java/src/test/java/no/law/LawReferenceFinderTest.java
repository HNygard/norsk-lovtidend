package no.law;

import no.law.lawreference.LawReferenceFinder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public class LawReferenceFinderTest {

    @Test
    void unknownLaw() {
        Assertions.assertThrows(LawReferenceFinder.LawNotFoundException_LawIdInvalid.class, () ->
                LawReferenceFinder.law(
                        "non existing law",
                        LocalDate.of(2015, 1, 1),
                        "konfliktrådsbehandling"
                )
        );
    }

    @Test
    void validLawId_butNonExistingLaw() {
        Assertions.assertThrows(LawReferenceFinder.LawNotFoundException_LawIdNotFound.class, () ->
                LawReferenceFinder.law(
                        "lov 12. juni 1234 nr. 123123123",
                        LocalDate.of(2015, 1, 1),
                        "konfliktrådsbehandling"
                )
        );
    }

    @Test
    void validLaw_noNameGiven() {
        Assertions.assertEquals("LOV-2006-05-19-16",
                LawReferenceFinder.law(
                        "lov 19. mai 2006 nr. 16",
                        LocalDate.of(2015, 1, 1),
                        null
                ).getLaw().getLawId()
        );
    }

    @Test
    public void lovReferanse() {
        // Based on:
        // https://lovdata.no/dokument/LTI/lov/2015-06-19-64
        //
        // Law reference (Norwegian text):
        // 'I lov 19. mai 2006 nr. 16 om rett til innsyn i dokument i offentleg verksemd gjøres følgende endringer:'
        LocalDate announcementDate = LocalDate.of(2015, 1, 1);
        LawReferenceFinder lawReference = LawReferenceFinder.law(
                "lov 19. mai 2006 nr. 16",
                announcementDate,
                "rett til innsyn i dokument i offentleg verksemd"
        );


        // '§ 16 første ledd nytt tredje punktum skal lyde:'
        // => § 16
        // => første ledd
        // => tredje punktum
        lawReference.addReference("§ 16", "første ledd", "tredje punktum");
    }
}
