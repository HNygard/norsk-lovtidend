package no.law;

import no.law.lawreference.LawReferenceFinder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

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

        // Not added any details yet, so this should match the law.
        List<? extends LawReference> lawRef = lawReference.getLaw().getMatchingLawRef(lawReference);
        Assertions.assertEquals(1, lawRef.size());
        Assertions.assertSame(lawReference.getLaw(), lawRef.get(0));

        // '§ 16 første ledd nytt tredje punktum skal lyde:'
        // => § 16
        lawReference.addParagraph("§ 1");
        lawRef = lawReference.getLaw().getMatchingLawRef(lawReference);
        Assertions.assertEquals(1, lawRef.size());
        Assertions.assertEquals(Law.Paragraph.class, lawRef.get(0).getClass());
        Assertions.assertEquals("§ 1. Hovudregel\n\n" +
                "Formålet med lova er å leggje til rette for at offentleg verksemd er open og" +
                " gjennomsiktig, for slik å styrkje informasjons- og ytringsfridommen, den demokratiske deltakinga," +
                " rettstryggleiken for den enkelte, tilliten til det offentlege og kontrollen frå ålmenta. Lova skal" +
                " òg leggje til rette for vidarebruk av offentleg informasjon.", lawRef.get(0).toString());

        // => første ledd
        // => tredje punktum
        lawReference.addReference("§ 16", "første ledd", "tredje punktum");
    }
}
