package no.law;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import no.law.json.Announcement;
import no.law.json.NorskLovtidend;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public class AnnouncementToLaw {
    public static void main(String[] args) throws FileNotFoundException {
        Gson gson = new Gson();
        NorskLovtidend o = gson.fromJson(new JsonReader(new FileReader("./norsk-lovtidend.json")), NorskLovtidend.class);

        List<Announcement> lawsIncludingChangeLaws = o.announcementsPerYear.stream()
                .map(year -> year.itemsLaw)
                .reduce(new ArrayList<>(), (announcements, announcements2) -> {
                    announcements.addAll(announcements2);
                    return announcements;
                });

        List<Announcement> laws = lawsIncludingChangeLaws.stream()
                .filter(law -> law.endrer == null)
                .collect(Collectors.toList());

        laws.forEach(law -> {
            System.out.println("LAW - " + law.url + " - " + law.title);

            lawsIncludingChangeLaws
                    .forEach(announcement -> {
                        if (announcement.endrer != null && announcement.endrer.contains(law.dato)) {
                            System.out.println("- " + announcement.url + " - " + announcement.title);
                        }
                    });
            System.out.println("-----------------------------------------------------");
        });

    }
}
