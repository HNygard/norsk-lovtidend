package no.law;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;

public class LawTextTest {

    @Test
    public void toStringTest() {
        LawText lawText = new LawText("LOV-1", "Lov om testing", "Offentleglova", LocalDate.now());
        lawText.chapters = new ArrayList<>();
        lawText.chapters.add(new LawText.Chapter("Kapittel 1"));
        lawText.chapters.add(new LawText.Chapter("Kapittel 2"));

        LawText.Paragraph paragraph1 = new LawText.Paragraph();
        paragraph1.name = "§ 1";
        paragraph1.sections = new ArrayList<>();
        paragraph1.sections.add(new LawText.Section("Første ledd. Mye tekst."));
        paragraph1.sections.add(new LawText.Section("Andre ledd. Mye tekst."));
        lawText.chapters.get(0).paragraphs = new ArrayList<>();
        lawText.chapters.get(0).paragraphs.add(paragraph1);

        LawText.Paragraph paragraph2 = new LawText.Paragraph();
        paragraph2.name = "§ 2";
        paragraph2.sections = new ArrayList<>();
        paragraph2.sections.add(new LawText.Section("Første ledd. Mye tekst."));
        lawText.chapters.get(0).paragraphs.add(paragraph2);


        LawText.Paragraph paragraph3 = new LawText.Paragraph();
        paragraph3.name = "§ 3";
        paragraph3.sections = new ArrayList<>();
        paragraph3.sections.add(new LawText.Section("Kapittel 2, første ledd i § 3."));
        lawText.chapters.get(1).paragraphs = new ArrayList<>();
        lawText.chapters.get(1).paragraphs.add(paragraph3);

        Assertions.assertEquals(
                "Lov om testing\n" +
                        "\n" +
                        "Kapittel 1\n" +
                        "\n" +
                        "§ 1\n" +
                        "\n" +
                        "Første ledd. Mye tekst.\n" +
                        "\n" +
                        "Andre ledd. Mye tekst.\n" +
                        "\n" +
                        "§ 2\n" +
                        "\n" +
                        "Første ledd. Mye tekst.\n" +
                        "\n" +
                        "Kapittel 2\n" +
                        "\n" +
                        "§ 3\n" +
                        "\n" +
                        "Kapittel 2, første ledd i § 3.",
                lawText.toString()
        );
    }
}
