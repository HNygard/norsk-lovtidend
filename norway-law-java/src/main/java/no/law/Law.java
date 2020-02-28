package no.law;

import no.law.lawreference.LawReferenceFinder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class holds the main text of a law. Plain text.
 */
public class Law implements LawReference {
    private final String lawId;
    private final String lawName;
    private final String shortName;
    private final LocalDate announcementDate;
    private final Collection<String> allPossibleNamesForLaw;
    List<Chapter> chapters;

    public Law(String lawId, String lawName, String shortName, LocalDate announcementDate) {
        this(lawId, lawName, shortName, new ArrayList<>(), announcementDate);
    }

    public Law(String lawId, String lawName, String shortName, Collection<String> otherNames, LocalDate announcementDate) {
        // Trim . at the end
        if (lawName.endsWith(".")) {
            lawName = lawName.substring(0, lawName.length() - 1);
        }

        this.lawId = lawId;
        this.lawName = lawName;
        this.shortName = shortName;
        this.announcementDate = announcementDate;

        allPossibleNamesForLaw = new ArrayList<>();
        allPossibleNamesForLaw.add(lawName);
        allPossibleNamesForLaw.add(shortName);
        allPossibleNamesForLaw.add(shortName + " (" + announcementDate.getYear() + ")");
        otherNames.forEach(name -> {
            allPossibleNamesForLaw.add(name);
            allPossibleNamesForLaw.add(name + " (" + announcementDate.getYear() + ")");
        });

        // Lov om rett til innsyn i dokument i offentleg verksemd (offentleglova).
        // => rett til innsyn i dokument i offentleg verksemd
        allPossibleNamesForLaw.add(
                this.lawName
                        .replace("Lov om ", "")
                        .replace(" (" + shortName + ")", "")
                        .replace(" (" + shortName.toLowerCase() + ")", "")
        );
    }

    public String getShortName() {
        return shortName;
    }

    public String getFullName() {
        return lawName;
    }

    public LocalDate getAnnounementDate() {
        return announcementDate;
    }

    public String getLawId() {
        return lawId;
    }

    public Collection<String> getPossibleNamesForLaw() {
        return allPossibleNamesForLaw;
    }

    public String toString() {
        return lawName + "\n\n"
                + chapters.stream()
                .map(Chapter::toString)
                .collect(Collectors.joining("\n\n"));
    }

    public String toHtml() {
        return
                (
                    "<div class=\"law\">\n"
                            + addIntent4spaces(
                                "<h1 class=\"law-name\">" + lawName + "</h1>\n\n"
                                + chapters.stream()
                                    .map(Chapter::toHtml)
                                    .collect(Collectors.joining("\n\n"))
                            )
                            + "\n</div>"
                )
                // Remove all white space only lines
                .replaceAll("\\n\\s*\\n", "\n\n");
    }

    @Override
    public boolean isMatchinLawRef(LawReferenceFinder lawRef) {
        return this == lawRef.getLaw();
    }

    @Override
    public List<? extends LawReference> getMatchingLawRef(LawReferenceFinder lawRef) {
        return getMatching(lawRef, chapters, this);
    }

    public static class Chapter implements LawReference {
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

        public String toHtml() {
            return
                    "<div class=\"law-chapter\">\n"
                            + addIntent4spaces(
                                    "<h2 class=\"law-chapter-name\">" + name + "</h2>\n\n"
                                    + "<div class=\"law-chapter-paragraphs\">\n"
                                    + addIntent4spaces(
                                        paragraphs.stream()
                                                .map(Paragraph::toHtml)
                                                .collect(Collectors.joining("\n\n"))
                                    )
                                    + "\n</div>"
                            )
                            + "\n</div>";
        }

        @Override
        public boolean isMatchinLawRef(LawReferenceFinder lawRef) {
            // TODO: implement
            return false;
        }

        @Override
        public List<? extends LawReference> getMatchingLawRef(LawReferenceFinder lawRef) {
            return getMatching(lawRef, paragraphs, this);
        }
    }

    public static class Paragraph implements LawReference {
        String name;
        String title;
        List<Section> sections;

        public String toString() {
            return name + ". " + title + "\n\n" + sections.stream()
                    .map(Section::toString)
                    .collect(Collectors.joining("\n\n"));
        }

        public String toHtml() {
            return "<div class=\"law-chapter-paragraph\">\n"
                    + addIntent4spaces(
                    "<h3 class=\"law-chapter-paragraph-name\">" + name + ". " + title + "</h3>\n\n"
                            + "<div class=\"law-chapter-paragraph-sections\">\n"
                            + addIntent4spaces(
                            sections.stream()
                                    .map(Section::toHtml)
                                    .collect(Collectors.joining("\n\n"))
                            )
                            + "\n</div>"
                    )
                    + "\n</div>";
        }

        @Override
        public boolean isMatchinLawRef(LawReferenceFinder lawRef) {
            if (lawRef.getParagraphRef() != null) {
                return name.equals(lawRef.getParagraphRef());
            }
            return false;
        }

        @Override
        public List<? extends LawReference> getMatchingLawRef(LawReferenceFinder lawRef) {
            return getMatching(lawRef, sections, this);
        }
    }

    public static class Section implements LawReference {
        String text;

        public Section(String text) {
            this.text = text;
        }

        public String toString() {
            return text;
        }

        public String toHtml() {
            return "<div class=\"law-chapter-paragraph-section\">" + text.replaceAll("\n", "<br>\n") + "</div>";
        }

        @Override
        public boolean isMatchinLawRef(LawReferenceFinder lawRef) {
            // TODO: implement
            return false;
        }

        @Override
        public List<? extends LawReference> getMatchingLawRef(LawReferenceFinder lawRef) {
            // TODO: implement
            return Collections.emptyList();
        }
    }

    private static String addIntent4spaces(String input) {
        return "    " + input.replaceAll("\n", "\n    ");
    }

    /**
     * Return all subParts matching lawRef. If non is found, check if currentPart is matching.
     */
    private static List<? extends LawReference> getMatching(
            LawReferenceFinder lawRef,
            Collection<? extends LawReference> subParts,
            LawReference currentPart) {
        List<LawReference> matches = subParts.stream()
                .map(part -> part.getMatchingLawRef(lawRef))
                .filter(subPartsMatches -> !subPartsMatches.isEmpty())
                .flatMap(List::stream)
                .collect(Collectors.toList());
        if (matches.isEmpty()) {
            if (currentPart.isMatchinLawRef(lawRef)) {
                return Collections.singletonList(currentPart);
            }
        }
        return matches;
    }
}
