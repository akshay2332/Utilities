import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Akshay Rane on 10/24/2016.
 */
public class XMLParser {
    private static XPath xPath = XPathFactory.newInstance().newXPath();
    public static final Logger logger = LoggerFactory.getLogger(XMLParser.class);

    public static void main(String args[]) {
        File inputFile = new File("C:/userData/a.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            logger.info("Hello");

             /*Give me the list of tags and their corressponding other values that you want like the attributes or elemnts that you want to find in the response
             Map<String,Object> tagElements
             if you want all the elements give null value in the List<String>
             */

            Map<String, Object> tagElementsToBeFound = new HashMap<String, Object>();
            List<String> elementList = new ArrayList<>();
            elementList.add("errormessage");
            elementList.add("returncode");
            elementList.add("errorcode");
            tagElementsToBeFound.put("rc", elementList);
            List<String> elementList1 = new ArrayList<>();
            elementList1.add("requestid");
            elementList1.add("sessionid");
            tagElementsToBeFound.put("mci", elementList1);
            List<String> elementList2 = new ArrayList<>();
            elementList2.add("codacctno");
            elementList2.add("currbalance");
            elementList2.add("custfullname");
            elementList2.add("rdopendate");
            elementList2.add("rdmatdate");
            elementList2.add("rateint");

            tagElementsToBeFound.put("acctdtls", elementList2);

            List<String> elementList3 = new ArrayList<>();
            elementList3.add("UserNameHoldingPattern");
            elementList3.add("CustRel");

            tagElementsToBeFound.put("CustDtls", elementList3);

            List<String> elementList4 = new ArrayList<>();

            elementList4.add("Custid");
            elementList4.add("signdata");
            elementList4.add("servertime");
            elementList4.add("mobile");

            tagElementsToBeFound.put("customerinfo", elementList4);

            tagElementsToBeFound.put("acctdtls", null);

            Map<String, Object> getValues = getResult(doc, tagElementsToBeFound);

            for (String s : getValues.keySet()) {
                System.out.println(s + "  value=" + getValues.get(s));
            }


        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static Map<String, Object> getResult(Document document, Map<String, Object> tagsToBeFound) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        for (String key : tagsToBeFound.keySet()) {
            String expression = "//*[name()='" + key + "']";
            System.out.println("expression" + expression);
            try {
                NodeList nodeList = (NodeList) XMLParser.xPath.compile(expression).evaluate(document, XPathConstants.NODESET);

                if (nodeList.getLength() == 1) {
                    List<Object> list = new ArrayList<Object>();
                    Node node = nodeList.item(0);
                    if (node != null && node.hasAttributes()) {
                        System.out.println("node.hasAttributes()" + node.hasAttributes());
                        List<String> attrList = (List<String>) tagsToBeFound.get(key);
                        Map<String, String> attrValue = new HashMap<>();
                        if (attrList != null) {

                            System.out.println("attrList" + attrList.size());
                            for (String s : attrList) {
                                System.out.println("Key=" + s);
                                String textContent = node.getAttributes().getNamedItem(s).getTextContent();
                                if (textContent != null)
                                    attrValue.put(s, textContent);
                            }
                        } else {
                            NamedNodeMap attrMap = node.getAttributes();
                            for (int m = 0; m < attrMap.getLength(); m++) {
                                attrValue.put(attrMap.item(m).getNodeName(), attrMap.item(m).getNodeValue());
                            }
                        }
                        if (!attrValue.isEmpty())
                            list.add(attrValue);
                        if (list.size() != 0) {
                            resultMap.put(key, list);
                        }
                    } else if (node != null && node.hasChildNodes()) {
                        System.out.println("node.hasChildNodes()" + node.hasChildNodes());
                        List<String> attrList = (List<String>) tagsToBeFound.get(key);
                        Map<String, String> attrValue = new HashMap<>();
                        if (attrList != null) {
                            System.out.println("attrList" + attrList.size());

                            if (Node.ELEMENT_NODE == node.getNodeType()) {
                                Element element = (Element) node;
                                for (String s : attrList) {
                                    NodeList node1 = element.getElementsByTagName(s);
                                    /*  System.out.println("element.getElementsByTagName(s))"+node1.item(0).getTextContent());*/
                                    if (node1 != null && node1.getLength() == 1) {
                                        attrValue.put(s, node1.item(0).getTextContent());
                                    } else if (node1 != null) {
                                        Map<String, String> stringMap = new HashMap<>();
                                        for (int j = 0; j < node1.getLength(); j++) {
                                            stringMap.putAll(findElementNode(node1.item(j), s));
                                        }
                                        if (!stringMap.isEmpty()) {
                                            attrValue.putAll(stringMap);
                                        }
                                    }
                                }
                            }

                            if (!attrValue.isEmpty())
                                list.add(attrValue);

                            if (list.size() != 0) {
                                resultMap.put(key, list);
                            }
                        } else {
                            String text = node.getTextContent();
                            if (text != null) {
                                resultMap.put(key, text);
                            }
                        }
                    }
                } else {
                    System.out.println("node.length()" + nodeList.getLength());
                    //multi node
                    List<Object> list = new ArrayList<Object>();
                    for (int i = 0; i < nodeList.getLength(); i++) {

                        Node node = nodeList.item(i);
                        if (node != null && node.hasAttributes()) {
                            System.out.println("node.hasAttributes()" + node.hasAttributes());
                            List<String> attrList = (List<String>) tagsToBeFound.get(key);
                            Map<String, String> attrValue = new HashMap<>();
                            if (attrList != null) {
                                for (String s : attrList) {
                                    System.out.println("Key node.hasAttributes()=" + s);
                                    String textContent = node.getAttributes().getNamedItem(s).getTextContent();
                                    if (textContent != null) {
                                        attrValue.put(s, textContent);
                                        list.add(attrValue);
                                    }

                                }

                            } else {
                                NamedNodeMap attrMap = node.getAttributes();
                                for (int m = 0; m < attrMap.getLength(); m++) {
                                    attrValue.put(attrMap.item(m).getNodeName(), attrMap.item(m).getNodeValue());
                                    list.add(attrValue);
                                }
                            }

                            if (list.size() != 0)
                                resultMap.put(key, list);
                        } else if (node != null && node.hasChildNodes()) {
                            System.out.println("node.node.hasChildNodes()" + node.hasChildNodes());
                            List<String> attrList = (List<String>) tagsToBeFound.get(key);
                            if (attrList != null) {
                                Map<String, String> attrValue = new HashMap<>();
                                if (node.getNodeType() == Node.ELEMENT_NODE) {
                                    Element element = (Element) node;
                                    for (String s : attrList) {
                                        NodeList node1 = element.getElementsByTagName(s);
                                        if (node1 != null && node1.getLength() == 1) {
                                            attrValue.put(s, node1.item(0).getTextContent());
                                        } else if (node1 != null) {
                                            Map<String, String> stringMap = new HashMap<>();
                                            for (int j = 0; j < node1.getLength(); j++) {
                                                stringMap.putAll(findElementNode(node1.item(j), s));
                                            }
                                            if (!stringMap.isEmpty()) {
                                                attrValue.putAll(stringMap);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
        }


        return resultMap;
    }

    public static NodeList fingTag(String tagName, Document doc) throws XPathExpressionException {
        String expression = "//*[name()='" + tagName + "']";
        return (NodeList) XMLParser.xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
    }

    public static Map<String, String> getTagAttribute(Node node, List<String> attrList) {
        Map<String, String> returnMap = new HashMap<String, String>();
        for (String s : attrList) {
            System.out.println("Key=" + s);
            String textContent = node.getAttributes().getNamedItem(s).getTextContent();

            if (textContent != null) {
                returnMap.put(s, textContent);
            }

        }
        return returnMap;
    }

    public static Map<String, String> findElementNode(Node node, String attrList) {
        Map<String, String> map = new HashMap<String, String>();
        if (node != null && node.hasAttributes()) {
            List<String> list = new ArrayList<>();
            list.add(attrList);
            Map<String, String> childMap = getTagAttribute(node, list);
            if (!childMap.isEmpty()) {
                map.putAll(childMap);
            }
        } else if (node != null && node.hasChildNodes()) {
            NodeList childList = node.getChildNodes();
            for (int k = 0; k < childList.getLength(); k++) {
                Map<String, String> childMap = findElementNode(childList.item(k), attrList);
                if (childMap != null && !childMap.isEmpty())
                    map.putAll(childMap);
            }
        } else if (node != null) {
            map.put(attrList, node.getTextContent());
        }
        return map;
    }
}
