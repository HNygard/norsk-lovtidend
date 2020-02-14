package no.law;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class holds the main text of a law. Plain text.
 */
public class LawText {
    private final String lawName;
    List<Chapter> chapters;

    public LawText(String lawName) {
        this.lawName = lawName;
    }

    public String toString() {
        return lawName + "\n\n"
                + chapters.stream()
                .map(Chapter::toString)
                .collect(Collectors.joining("\n\n"));
    }

    public static class Chapter {
        String name;
        List<Paragraph> paragraphs;

        public Chapter(String name) {
            this.name = name;
        }

        public String toString() {
            return name + "\n\n" + paragraphs.stream()
                    .map(Paragraph::toString)
                    .collect(Collectors.joining("\n\n"));
        }
    }

    public static class Paragraph {
        String name;
        List<Section> sections;

        public String toString() {
            return name + "\n\n" + sections.stream()
                    .map(Section::toString)
                    .collect(Collectors.joining("\n\n"));
        }
    }

    public static class Section {
        String text;

        public Section(String text) {
            this.text = text;
        }

        public String toString() {
            return text;
        }
    }
}
