package no.law;

import no.law.lawreference.LawReferenceFinder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

public class LawReferenceFinderTest {

    @Test
    void unknownLaw() {
        LawReferenceFinder ref = new LawReferenceFinder();
        Assertions.assertThrows(LawReferenceFinder.LawNotFoundException_LawIdInvalid.class, () ->
                ref.law(
                        "non existing law",
                        LocalDate.of(2015, 1, 1),
                        "konfliktrådsbehandling"
                )
        );
    }

    @Test
    void validLawId_butNonExistingLaw() {
        LawReferenceFinder ref = new LawReferenceFinder();
        Assertions.assertThrows(LawReferenceFinder.LawNotFoundException_LawIdNotFound.class, () ->
                ref.law(
                        "lov 12. juni 1234 nr. 123123123",
                        LocalDate.of(2015, 1, 1),
                        "konfliktrådsbehandling"
                )
        );
    }

    @Test
    void validLaw_noNameGiven() {
        LawReferenceFinder ref = new LawReferenceFinder();
        ref.law(
                "lov 19. mai 2006 nr. 16",
                LocalDate.of(2015, 1, 1),
                null
        );
        Assertions.assertEquals("LOV-2006-05-19-16",
                ref.getLaw().getLawId()
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
        LawReferenceFinder lawReference = new LawReferenceFinder();
        lawReference.law(
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
        Assertions.assertEquals("§ 1. Formål\n\n" +
                "Formålet med lova er å leggje til rette for at offentleg verksemd er open og" +
                " gjennomsiktig, for slik å styrkje informasjons- og ytringsfridommen, den demokratiske deltakinga," +
                " rettstryggleiken for den enkelte, tilliten til det offentlege og kontrollen frå ålmenta. Lova skal" +
                " òg leggje til rette for vidarebruk av offentleg informasjon.", lawRef.get(0).toString());

        // => første ledd
        lawReference.addParagraph("§ 2");
        lawReference.addSection("tredje");
        lawRef = lawReference.getLaw().getMatchingLawRef(lawReference);
        Assertions.assertEquals(1, lawRef.size());
        Assertions.assertEquals(Law.Section.class, lawRef.get(0).getClass());
        Assertions.assertEquals("Lova gjeld ikkje for Stortinget, Riksrevisjonen, Stortingets ombodsmann for" +
                " forvaltninga og andre organ for Stortinget.", lawRef.get(0).toString());

        // :: 'Offentleglova Første ledd' should return all 'første ledd' sections
        lawReference.addParagraph(null);
        lawReference.addSection("første");
        lawRef = lawReference.getLaw().getMatchingLawRef(lawReference);
        Assertions.assertEquals(3, lawRef.size());
        Assertions.assertEquals(Law.Section.class, lawRef.get(0).getClass());
        Assertions.assertEquals(Law.Section.class, lawRef.get(1).getClass());
        Assertions.assertEquals(Law.Section.class, lawRef.get(2).getClass());
        Assertions.assertEquals("Formålet med lova er å leggje til rette for at offentleg verksemd er open og" +
                " gjennomsiktig, for slik å styrkje informasjons- og ytringsfridommen, den demokratiske deltakinga," +
                " rettstryggleiken for den enkelte, tilliten til det offentlege og kontrollen frå ålmenta. Lova skal" +
                " òg leggje til rette for vidarebruk av offentleg informasjon.", lawRef.get(0).toString());
        // lawRef.get(1) is long. Don't bother checking text
        Assertions.assertEquals("Saksdokument, journalar og liknande register for organet er opne for innsyn" +
                " dersom ikkje anna følgjer av lov eller forskrift med heimel i lov. Alle kan krevje innsyn i" +
                " saksdokument, journalar og liknande register til organet hos vedkommande organ.", lawRef.get(2).toString());

        // :: 'Offentleglova § 1 første ledd' should return just the section inside § 1
        lawReference.addParagraph("§ 1");
        lawReference.addSection("første");
        lawRef = lawReference.getLaw().getMatchingLawRef(lawReference);
        Assertions.assertEquals(1, lawRef.size());
        Assertions.assertEquals(Law.Section.class, lawRef.get(0).getClass());
        Assertions.assertEquals("Formålet med lova er å leggje til rette for at offentleg verksemd er open og" +
                " gjennomsiktig, for slik å styrkje informasjons- og ytringsfridommen, den demokratiske deltakinga," +
                " rettstryggleiken for den enkelte, tilliten til det offentlege og kontrollen frå ålmenta. Lova skal" +
                " òg leggje til rette for vidarebruk av offentleg informasjon.", lawRef.get(0).toString());

        // => tredje punktum
    }
}
