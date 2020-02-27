package no.law.lawreference;

import no.law.Law;
import no.law.LawRepository;

import java.time.LocalDate;

/**
 * This class holds a reference to a law or a part of a law.
 */
public class LawReference {
    private Law law;

    public static LawReference law(String rawLawId, LocalDate date, String name) {
        String checkedLawId = NorwegianLawTextName_to_LawId.law(rawLawId, date);
        if (checkedLawId == null) {
            throw new LawNotFoundException_LawIdInvalid("Could not find law id/name [" + rawLawId + "] at the time [" + date + "].");
        }

        Law law = LawRepository.getLaw(checkedLawId);
        if (law == null) {
            throw new LawNotFoundException_LawIdNotFound("The law [" + checkedLawId + "] was not found.");
        }

        if (!law.getPossibleNamesForLaw().contains(name)) {
            throw new LawNotFoundException_ControlNameDoesNotMatch(
                    "The law [" + checkedLawId + "] does not have the name or subject [" + name + "].\n" +
                            "Names/subjects of the law:\n" +
                            String.join("\n", law.getPossibleNamesForLaw())
            );
        }

        LawReference lawReference = new LawReference();
        lawReference.law = law;
        return lawReference;
    }

    public void addReference(String paragraphReference, String sectionReference, String sentenceReference) {
    }

    public static class LawNotFoundException_LawIdInvalid extends RuntimeException {
        LawNotFoundException_LawIdInvalid(String message) {
            super(message);
        }
    }

    public static class LawNotFoundException_LawIdNotFound extends RuntimeException {
        LawNotFoundException_LawIdNotFound(String message) {
            super(message);
        }
    }

    public static class LawNotFoundException_ControlNameDoesNotMatch extends RuntimeException {
        LawNotFoundException_ControlNameDoesNotMatch(String message) {
            super(message);
        }
    }
}
