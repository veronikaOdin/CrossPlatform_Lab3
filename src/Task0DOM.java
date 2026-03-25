import org.w3c.dom.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.*;

// Клас для збереження інфи про ім'я
class BabyName implements Comparable<BabyName> {
    String name, gender;
    int count, rank;

    public BabyName(String name, String gender, int count, int rank) {
        this.name = name;
        this.gender = gender;
        this.count = count;
        this.rank = rank;
    }

    // Сортуємо по рейтингу
    @Override
    public int compareTo(BabyName other) {
        return Integer.compare(this.rank, other.rank);
    }
}

public class Task0DOM {
    private static final String INPUT_FILE = "Popular_Baby_Names_NY.xml";
    private static final String OUTPUT_FILE = "Top_Asian_Names.xml";
    private static final String TARGET_ETHNICITY = "ASIAN AND PACIFIC ISLANDER";
    private static final int LIMIT = 15;

    public static void main(String[] args) {
        try {
            // 1. Витягуємо дані
            SAXParserFactory saxFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxFactory.newSAXParser();
            NameFilterHandler handler = new NameFilterHandler(TARGET_ETHNICITY);
            saxParser.parse(new File(INPUT_FILE), handler);

            // Прибираємо дублікати і сортуємо
            List<BabyName> allNames = handler.getNames();
            Set<String> uniqueCheck = new HashSet<>();
            List<BabyName> topNames = new ArrayList<>();

            for (BabyName bn : allNames) {
                if (uniqueCheck.add(bn.name)) {
                    topNames.add(bn);
                }
            }
            Collections.sort(topNames);
            if (topNames.size() > LIMIT) {
                topNames = topNames.subList(0, LIMIT);
            }

            // 2. Створюємо новий XML файл (DOM)
            writeToXML(topNames, OUTPUT_FILE);
            System.out.println("=== Новий XML файл успішно створено: " + OUTPUT_FILE + " ===\n");

            // 3. Читаємо новий файл і виводимо на екран (DOM)
            System.out.println("=== Читання створеного файлу через DOM парсер ===");
            readFromXML(OUTPUT_FILE);

        } catch (Exception e) {
            System.out.println("Помилка: " + e.getMessage());
        }
    }

    // Запис у файл
    private static void writeToXML(List<BabyName> names, String fileName) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();

        Element rootElement = doc.createElement("TopBabyNames");
        doc.appendChild(rootElement);

        for (BabyName bn : names) {
            Element nameEntry = doc.createElement("NameEntry");

            Element nm = doc.createElement("Name");
            nm.appendChild(doc.createTextNode(bn.name));
            nameEntry.appendChild(nm);

            Element gndr = doc.createElement("Gender");
            gndr.appendChild(doc.createTextNode(bn.gender));
            nameEntry.appendChild(gndr);

            Element cnt = doc.createElement("Count");
            cnt.appendChild(doc.createTextNode(String.valueOf(bn.count)));
            nameEntry.appendChild(cnt);

            Element rnk = doc.createElement("Rank");
            rnk.appendChild(doc.createTextNode(String.valueOf(bn.rank)));
            nameEntry.appendChild(rnk);

            rootElement.appendChild(nameEntry);
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(fileName));
        transformer.transform(source, result);
    }

    // Читання з файлу
    private static void readFromXML(String fileName) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new File(fileName));
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("NameEntry");
        for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String name = element.getElementsByTagName("Name").item(0).getTextContent();
                String gender = element.getElementsByTagName("Gender").item(0).getTextContent();
                String count = element.getElementsByTagName("Count").item(0).getTextContent();
                String rank = element.getElementsByTagName("Rank").item(0).getTextContent();

                System.out.printf("Ранг: %-3s | Ім'я: %-10s | Гендер: %-6s | Кількість: %s\n", rank, name, gender, count);
            }
        }
    }
}

// SAX Обробник (фільтр)
class NameFilterHandler extends DefaultHandler {
    private List<BabyName> names = new ArrayList<>();
    private String targetEthnicity;
    private String currentElement = "";
    private String nm, gndr, eth;
    private int cnt, rnk;

    public NameFilterHandler(String targetEthnicity) {
        this.targetEthnicity = targetEthnicity;
    }

    public List<BabyName> getNames() { return names; }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentElement = qName;
        if (qName.equals("row") && attributes.getLength() > 0) {
            nm = gndr = eth = "";
            cnt = rnk = 0;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String value = new String(ch, start, length).trim();
        if (value.isEmpty()) return;

        switch (currentElement) {
            case "nm": nm = value; break;
            case "gndr": gndr = value; break;
            case "ethcty": eth = value; break;
            case "cnt": cnt = Integer.parseInt(value); break;
            case "rnk": rnk = Integer.parseInt(value); break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("row") && targetEthnicity.equalsIgnoreCase(eth) && nm != null && !nm.isEmpty()) {
            names.add(new BabyName(nm, gndr, cnt, rnk));
            nm = "";
        }
        currentElement = "";
    }
}