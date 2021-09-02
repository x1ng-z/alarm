package group.yzhs.alarm.service;


import group.yzhs.alarm.config.FileParseConfig;
import group.yzhs.alarm.constant.AlarmModelEnum;
import group.yzhs.alarm.constant.ProductTypeEnum;
import group.yzhs.alarm.model.Device;
import group.yzhs.alarm.model.ProductionLine;
import group.yzhs.alarm.model.rule.limit.LimitRule;
import group.yzhs.alarm.model.rule.trigger.TriggerRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class XmlService implements BaseResource {
    @Autowired
    private FileParseConfig fileParseConfig;



    @Override
    public List<ProductionLine> Find() {
        List<ProductionLine> pCollects = new ArrayList<>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringElementContentWhitespace(true);

        DocumentBuilder db = null;
        BufferedInputStream bufferedInputStream=null;
        try {
            db = dbf.newDocumentBuilder();
            String filepath = fileParseConfig.getFilelocation();


            Document xmldoc = null;
            bufferedInputStream = new BufferedInputStream(new FileInputStream(new File(filepath)));
            xmldoc = db.parse(bufferedInputStream);
            Element root = xmldoc.getDocumentElement();
            NodeList pLList = root.getElementsByTagName("Production_Line");

            for (int i = 0; i < pLList.getLength(); i++) {
                if (pLList.item(i) instanceof Element) {
                    Node ProductionLine_Node = pLList.item(i);
                    String CompanyNo = ((Element) ProductionLine_Node).getAttribute("PRL_ID");
                    String CompanyName = ((Element) ProductionLine_Node).getAttribute("PRL_NAME");

                    ProductionLine newPL = ProductionLine.builder().name(CompanyName).pNo(CompanyNo).devices(new ArrayList<>()).build();

                    NodeList DeviceNodeList = ProductionLine_Node.getChildNodes();

                    pCollects.add(newPL);

                    for (int j = 0; j < DeviceNodeList.getLength(); j++) {

                        if (DeviceNodeList.item(j) instanceof Element) {
                            Element DeviceNode = ((Element) DeviceNodeList.item(j));
                            String DEV_NAME = DeviceNode.getAttribute("DEV_NAME");
                            String DEV_NO = DeviceNode.getAttribute("DEV_NO");
                            Device device = Device.builder().dNo(DEV_NO).name(DEV_NAME).rules(new ArrayList<>()).build();
                            newPL.getDevices().add( device);

                            NodeList MeasurePointNodeList = DeviceNode.getChildNodes();
                            for (int k = 0; k < MeasurePointNodeList.getLength(); ++k) {

                                if (MeasurePointNodeList.item(k) instanceof Element) {

                                    Element MeasurePointNode = ((Element) MeasurePointNodeList.item(k));

                                    String ALM_LEV = MeasurePointNode.getAttribute("ALM_MOD");
                                    String CHA_RATE = MeasurePointNode.getAttribute("SUB_MOD");
                                    List<AlarmModelEnum> matchmodels = Arrays.stream(AlarmModelEnum.values()).filter(a -> {
                                        return ObjectUtils.nullSafeEquals(a.getCode(), ALM_LEV);
                                    }).collect(Collectors.toList());
                                    if (matchmodels.size() == 1) {
                                        switch (matchmodels.get(0)) {

                                            case ALARMMODEL_TRIG: {
                                                TriggerRule baseRule = new TriggerRule();
                                                baseRule.setSubModel(CHA_RATE);
                                                baseRule.setAlarmModelEnum(AlarmModelEnum.ALARMMODEL_TRIG);
                                                baseRule.setTag(MeasurePointNode.getAttribute("DEV_TAG"));
                                                baseRule.setNotion(MeasurePointNode.getAttribute("DEV_TAGCH"));
                                                baseRule.setTemplate(MeasurePointNode.getAttribute("pushcontent"));
                                                baseRule.setPushWX(Boolean.valueOf(MeasurePointNode.getAttribute("isPushWX")));
                                                baseRule.setAudio(Boolean.valueOf(MeasurePointNode.getAttribute("isAudio")));
                                                Map<String, ProductTypeEnum> productTypeEnumMap=Arrays.stream(ProductTypeEnum.values()).collect(Collectors.toMap(ProductTypeEnum::getCode, (e)->e,(o, n)->n));
                                                baseRule.setProduct(productTypeEnumMap.get(DEV_NO)==null?ProductTypeEnum.PRODUCT_TYPE_SC:productTypeEnumMap.get(DEV_NO));
                                                device.getRules().add(baseRule);
                                                break;
                                            }
                                            case ALARMMODEL_LIM: {
                                                LimitRule limitRule = new LimitRule();
                                                limitRule.setLimitValue(Double.valueOf(MeasurePointNode.getAttribute("LIM_VALUE")));
                                                limitRule.setSubModel(CHA_RATE);
                                                limitRule.setAlarmModelEnum(AlarmModelEnum.ALARMMODEL_LIM);
                                                limitRule.setTag(MeasurePointNode.getAttribute("DEV_TAG"));
                                                limitRule.setNotion(MeasurePointNode.getAttribute("DEV_TAGCH"));
                                                limitRule.setTemplate(MeasurePointNode.getAttribute("pushcontent"));
                                                limitRule.setPushWX(Boolean.valueOf(MeasurePointNode.getAttribute("isPushWX")));
                                                limitRule.setAudio(Boolean.valueOf(MeasurePointNode.getAttribute("isAudio")));
                                                Map<String, ProductTypeEnum> productTypeEnumMap=Arrays.stream(ProductTypeEnum.values()).collect(Collectors.toMap(ProductTypeEnum::getCode, (e)->e,(o, n)->n));
                                                limitRule.setProduct(productTypeEnumMap.get(DEV_NO)==null?ProductTypeEnum.PRODUCT_TYPE_SC:productTypeEnumMap.get(DEV_NO));
                                                device.getRules().add(limitRule);
                                                break;

                                            }

                                            default:

                                        }
                                    }

                                }


                            }

                        }

                    }


                }


            }
            return pCollects;
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage(), e);
        } catch (SAXException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }finally {
            if(!ObjectUtils.isEmpty(bufferedInputStream)){
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage(),e);
                }
            }

        }
        return null;
    }

    @Override
    public void Update(String PRL_ID, String DEV_NO, String DEV_TAG_NO, String attr, String value) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringElementContentWhitespace(true);
        try {

            DocumentBuilder db = dbf.newDocumentBuilder();
            String filepath = System.getProperty("user.dir") + "/conf/Production_Line.xml";
            Document xmldoc = db.parse(new BufferedInputStream(new FileInputStream(new File(filepath))));//LX_Production_Line_one.class.getResourceAsStream("Production_Line1.xml")

            Element root = xmldoc.getDocumentElement();

            NodeList PLList = root.getChildNodes();
            for (int i = 0; i < PLList.getLength(); i++) {
                if (PLList.item(i) instanceof Element) {
                    Element PLElment = (Element) PLList.item(i);
                    if (PLElment.getAttribute("PRL_ID").equals(PRL_ID)) {

                        NodeList DeviceNodeList = PLElment.getChildNodes();
                        for (int j = 0; j < DeviceNodeList.getLength(); j++) {
                            if (DeviceNodeList.item(j) instanceof Element) {

                                Element DeviceElment = (Element) DeviceNodeList.item(j);
                                if (DeviceElment.getAttribute("DEV_NO").equals(DEV_NO)) {

                                    NodeList MeasurePointNodeList = DeviceElment.getChildNodes();
                                    for (int k = 0; k < MeasurePointNodeList.getLength(); k++) {
                                        if (MeasurePointNodeList.item(k) instanceof Element) {

                                            Element MeasurePointElment = (Element) MeasurePointNodeList.item(k);
                                            if (MeasurePointElment.getAttribute("DEV_TAG_NO").equals(DEV_TAG_NO)) {
                                                MeasurePointElment.setAttribute(attr, value);

                                            }
                                        }

                                    }


                                }
                            }


                        }

                    }


                }

            }

            BufferedOutputStream filename = new BufferedOutputStream(new FileOutputStream(new File(filepath)));
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer former = factory.newTransformer();
            former.transform(new DOMSource(xmldoc), new StreamResult(filename));
            filename.flush();
            filename.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public synchronized void Delete() {
        throw new UnsupportedOperationException();

    }
}
