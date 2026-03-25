import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class Task0SAX {
    public static void main(String[] args) {
        try {
            File xmlFile = new File("Popular_Baby_Names_NY.xml");
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            BabyNameHandler handler = new BabyNameHandler();
            System.out.println("=== Фрагмент документу ===");
            saxParser.parse(xmlFile, handler);

            System.out.println("\n=== Всі унікальні теги в документі ===");
            for (String tag : handler.getTags()) {
                System.out.println("- " + tag);
            }

            System.out.println("\n=== Всі національні групи (етноси) ===");
            for (String ethnicity : handler.getEthnicities()) {
                System.out.println("- " + ethnicity);
            }

        } catch (Exception e) {
            System.out.println("Файл не знайдено або помилка: " + e.getMessage());
        }
    }
}

class BabyNameHandler extends DefaultHandler {
    private Set<String> tags = new HashSet<>();
    private Set<String> ethnicities = new HashSet<>();
    private boolean bEthnicity = false;
    private int printLimit = 45;

    public Set<String> getTags() { return tags; }
    public Set<String> getEthnicities() { return ethnicities; }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tags.add(qName);
        if (qName.equalsIgnoreCase("ethcty")) {
            bEthnicity = true;
        }
        if (printLimit > 0) {
            System.out.print("<" + qName + ">");
        }
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        String value = new String(ch, start, length).trim();
        if (bEthnicity && !value.isEmpty()) {
            ethnicities.add(value);
            bEthnicity = false;
        }
        if (printLimit > 0 && !value.isEmpty()) {
            System.out.print(value);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (printLimit > 0) {
            System.out.println("</" + qName + ">");
            printLimit--;
        }
    }
}