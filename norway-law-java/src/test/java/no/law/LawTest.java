package no.law;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;

public class LawTest {

    @Test
    public void toStringTest() {
        Law law = new Law("LOV-1", "Lov om testing", "Offentleglova", LocalDate.now());
        law.chapters = new ArrayList<>();
        law.chapters.add(new Law.Chapter("Kapittel 1"));
        law.chapters.add(new Law.Chapter("Kapittel 2"));

        Law.Paragraph paragraph1 = new Law.Paragraph();
        paragraph1.name = "§ 1";
        paragraph1.sections = new ArrayList<>();
        paragraph1.sections.add(new Law.Section("Første ledd. Mye tekst."));
        paragraph1.sections.add(new Law.Section("Andre ledd. Mye tekst."));
        law.chapters.get(0).paragraphs = new ArrayList<>();
        law.chapters.get(0).paragraphs.add(paragraph1);

        Law.Paragraph paragraph2 = new Law.Paragraph();
        paragraph2.name = "§ 2";
        paragraph2.sections = new ArrayList<>();
        paragraph2.sections.add(new Law.Section("Første ledd. Mye tekst."));
        law.chapters.get(0).paragraphs.add(paragraph2);


        Law.Paragraph paragraph3 = new Law.Paragraph();
        paragraph3.name = "§ 3";
        paragraph3.sections = new ArrayList<>();
        paragraph3.sections.add(new Law.Section("Kapittel 2, første ledd i § 3."));
        law.chapters.get(1).paragraphs = new ArrayList<>();
        law.chapters.get(1).paragraphs.add(paragraph3);

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
                law.toString()
        );
    }
}
