package no.law.lawreference;

import no.law.Law;
import no.law.LawRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NorwegianText_to_LawReference {
    /**
     * The text should only contain one reference.
     */
    public static LawReferenceFinder textToLawReference(String text, LocalDate date) {
        LawReferenceFinder ref = new LawReferenceFinder();
        text = " " + text + " ";
        Pattern pattern = Pattern.compile(" ((§|paragraf)\\s?([0-9]*)) ");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find() && !"".equals(matcher.group(3))) {
            ref.addParagraph("§ " + matcher.group(3));
            text = text.replace(matcher.group(1), "");
        }

        pattern = Pattern.compile(" (([a-zæøåA-ZÆØÅ]*) ledd) ");
        matcher = pattern.matcher(text);
        if (matcher.find()) {
            ref.addSection(matcher.group(2));
            text = text.replace(matcher.group(1), "");
        }


        Map<String, Law> lawName_to_law = new HashMap<>();
        List<Law> sortedLaws = LawRepository.getLaws().stream()
                .filter(law -> law.getAnnounementDate().isBefore(date))
                .sorted(Comparator.comparing(Law::getAnnounementDate)).collect(Collectors.toList());
        for(Law law : sortedLaws) {
            for (String lawName : law.getPossibleNamesForLaw()) {
                // Law name not present. We add in sorted order, so the newest law gets priority
                lawName_to_law.putIfAbsent(lawName, law);
            }
        }

        // Sort the names so that the longest names are first. They will get priority over shorter names.
        List<Map.Entry<String, Law>> sortedEntrySet_longestFirst = lawName_to_law.entrySet().stream()
                .sorted(Comparator.comparingInt(stringLawEntry -> stringLawEntry.getKey().length()))
                .collect(Collectors.toList());
        String textLower = text.toLowerCase();
        for(Map.Entry<String, Law> lawNameEntry : sortedEntrySet_longestFirst) {
            if (textLower.contains(lawNameEntry.getKey().toLowerCase())) {
                // Find the word with the actual case
                String lawNameInInput = text.substring(
                        textLower.indexOf(lawNameEntry.getKey().toLowerCase()),
                        textLower.indexOf(lawNameEntry.getKey().toLowerCase()) + lawNameEntry.getKey().length()
                );
                text = text.replace(lawNameInInput, "");

                ref.law(lawNameInInput, date, null);
                break;
            }
        }

        return ref;
    }
}
