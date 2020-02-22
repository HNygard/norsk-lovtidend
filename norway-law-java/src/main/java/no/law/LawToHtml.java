package no.law;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class LawToHtml {
    public static void main(String[] args) throws IOException {
        // Parts of Offentleglova
        // Source: https://lovdata.no/dokument/LTI/lov/2006-05-19-16 (The original announcement of the law)
        LawText lawText = new LawText("Lov om rett til innsyn i dokument i offentleg verksemd (offentleglova).");
        lawText.chapters = new ArrayList<>();
        lawText.chapters.add(new LawText.Chapter("Kapittel 1 Innleiande føresegner"));
        lawText.chapters.add(new LawText.Chapter("Kapittel 2 Hovudreglane om innsyn"));

        LawText.Paragraph paragraph1 = new LawText.Paragraph();
        paragraph1.name = "§ 1. Formål";
        paragraph1.sections = new ArrayList<>();
        paragraph1.sections.add(new LawText.Section("Formålet med lova er å leggje til rette for at offentleg verksemd er open og gjennomsiktig, for slik å styrkje informasjons- og ytringsfridommen, den demokratiske deltakinga, rettstryggleiken for den enkelte, tilliten til det offentlege og kontrollen frå ålmenta. Lova skal òg leggje til rette for vidarebruk av offentleg informasjon."));
        lawText.chapters.get(0).paragraphs = new ArrayList<>();
        lawText.chapters.get(0).paragraphs.add(paragraph1);

        LawText.Paragraph paragraph2 = new LawText.Paragraph();
        paragraph2.name = "§ 2. Verkeområdet til lova";
        paragraph2.sections = new ArrayList<>();
        paragraph2.sections.add(new LawText.Section("Lova gjeld for\n" +
                "a)\tstaten, fylkeskommunane og kommunane,\n" +
                "b)\tandre rettssubjekt i saker der dei gjer enkeltvedtak eller utferdar forskrift,\n" +
                "c)\tsjølvstendige rettssubjekt der stat, fylkeskommune eller kommune direkte eller indirekte har ein eigardel som gir meir enn halvparten av røystene i det øvste organet i rettssubjektet, og\n" +
                "d)\tsjølvstendige rettssubjekt der stat, fylkeskommune eller kommune direkte eller indirekte har rett til å velje meir enn halvparten av medlemmene med røysterett i det øvste organet i rettssubjektet.\n" +
                "Bokstavane c og d gjeld ikkje rettssubjekt som hovudsakleg driv næring i direkte konkurranse med og på same vilkår som private. For verksemder som etter offentleg oppkjøp eller liknande kjem inn under bokstavane c eller d, gjeld lova frå og med fjerde månadsskiftet etter den månaden da vilkåra vart oppfylte."));
        paragraph2.sections.add(new LawText.Section("Kongen kan gi forskrift om at lova ikkje skal gjelde for sjølvstendige rettssubjekt eller for visse dokument hos sjølvstendige rettssubjekt som er omfatta av første ledd bokstavane c eller d, dersom det må reknast som nødvendig ut frå omsynet til arten av verksemda, konkurransesituasjonen eller andre særlege tilhøve. Det same gjeld dersom det gjeld unntak frå innsynsretten for det alt vesentlege av dokumenta til verksemda og særlege tungtvegande omsyn tilseier det. Kongen kan gi forskrift om at lova heilt eller delvis skal gjelde for sjølvstendige rettssubjekt som er knytte til stat eller kommune utan å oppfylle vilkåra i første ledd bokstav c eller d, eller som er unnatekne etter første ledd andre punktum."));
        paragraph2.sections.add(new LawText.Section("Lova gjeld ikkje for Stortinget, Riksrevisjonen, Stortingets ombodsmann for forvaltninga og andre organ for Stortinget."));
        paragraph2.sections.add(new LawText.Section("Lova gjeld ikkje for gjeremål som domstolane har etter rettsstellovene. Lova gjeld heller ikkje for gjeremål som andre organ har etter rettsstellovene i eigenskap av rettsstellorgan. Lova gjeld dessutan ikkje for gjeremål som politiet og påtalemakta har etter straffeprosessloven. Kongen kan gi forskrifter om kva lover som skal reknast som rettsstellover, og om at enkelte gjeremål etter rettsstellovene likevel skal vere omfatta av lova."));
        paragraph2.sections.add(new LawText.Section("Lova gjeld for Svalbard dersom ikkje anna blir fastsett av Kongen."));
        paragraph2.sections.add(new LawText.Section("Føresegnene i § 6, § 7 andre ledd, § 8 tredje ledd andre punktum og fjerde og femte ledd og § 30 første ledd tredje punktum og andre ledd gjeld uavhengig av føresegnene i paragrafen her for alle verksemder som er omfatta av EØS-avtalen vedlegg XI nr. 5k (direktiv 2003/98/EF) om vidarebruk av informasjon frå offentleg sektor."));
        lawText.chapters.get(0).paragraphs.add(paragraph2);


        LawText.Paragraph paragraph3 = new LawText.Paragraph();
        paragraph3.name = "§ 3. Hovudregel";
        paragraph3.sections = new ArrayList<>();
        paragraph3.sections.add(new LawText.Section("Saksdokument, journalar og liknande register for organet er opne for innsyn dersom ikkje anna følgjer av lov eller forskrift med heimel i lov. Alle kan krevje innsyn i saksdokument, journalar og liknande register til organet hos vedkommande organ."));
        lawText.chapters.get(1).paragraphs = new ArrayList<>();
        lawText.chapters.get(1).paragraphs.add(paragraph3);

        String offentleglova = lawText.toString();
        System.out.println(offentleglova);

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("./offentleglova.txt"), StandardCharsets.UTF_8))) {
            writer.write(lawText.toString());
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("./offentleglova.html"), StandardCharsets.UTF_8))) {
            writer.write(
                    "<head>\n"
                            + "    <meta charset=\"UTF-8\">\n"
                            + "    <link rel=\"stylesheet\" href=\"law.css\">\n"
                            + "</head>\n\n"
                            + lawText.toHtml()
            );
        }
    }
}
