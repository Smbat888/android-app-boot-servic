package com.example.work.bytetoxmlparserapp;


import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class XMLDocParser {

    private byte [] mData;

    public XMLDocParser(byte [] response) {
        mData = trimByes(response);
    }

    public String getXMLDocument() {
        try {
            final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            final Element root = doc.createElement(XMLDocConstants.FSSTATUS);
            doc.appendChild(root);

            final Element lifeTime = doc.createElement(XMLDocConstants.LIFE_TIME);
            root.appendChild(lifeTime);
            lifeTime.setTextContent(getLifeTime());

            final Element docsKind = doc.createElement(XMLDocConstants.DOCS_KIND);
            root.appendChild(docsKind);
            docsKind.setTextContent(getDocsKind());

            final Element docsData = doc.createElement(XMLDocConstants.DOCS_DATA);
            root.appendChild(docsData);
            docsData.setTextContent(getDocsData());

            final Element shiftMode = doc.createElement(XMLDocConstants.SHFT_MODE);
            root.appendChild(shiftMode);
            shiftMode.setTextContent(getShiftMode());

            final Element warmFlag = doc.createElement(XMLDocConstants.WARM_FLAG);
            root.appendChild(warmFlag);
            warmFlag.setTextContent(getWarmFlag());

            final Element dateTime = doc.createElement(XMLDocConstants.DATE_TIME);
            root.appendChild(dateTime);
            dateTime.setTextContent(getDateTime());

            final Element deviceID = doc.createElement(XMLDocConstants.DEVICE_ID);
            root.appendChild(deviceID);
            deviceID.setTextContent(getDeviceID());

            final Element docsCode = doc.createElement(XMLDocConstants.DOCS_CODE);
            root.appendChild(docsCode);
            docsCode.setTextContent(getDocsCode());

            // create Transformer object
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(new DOMSource(doc), result);

            // return XML string
            return writer.toString();

        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getDocsCode() {
        // TODO - check
        final byte[] arr = { mData[30], mData[31], mData[32], mData[33] };
        return String.valueOf(fromByteArray(arr));
    }

    private String getDeviceID() {
        final byte[] bytes = {mData[14], mData[15], mData[16], mData[17], mData[18], mData[19], mData[20],
                mData[21], mData[22], mData[23], mData[24], mData[25], mData[26], mData[27],
                mData[28], mData[29]};
        return new String(bytes);
    }

    private String getWarmFlag() {
        return String.valueOf(mData[8]);
    }

    private String getShiftMode() {
        return String.valueOf(mData[7]);
    }

    private String getDocsData() {
        return String.valueOf(mData[6]);
    }

    private String getDocsKind() {
        return String.valueOf(mData[5]);
    }

    private String getLifeTime() {
        return String.valueOf(mData[4]);
    }

    /* Helper Methods */

    private byte [] trimByes(byte[] response) {
        // TODO - add logic to trim unnecssary bytes from first
        return response;
    }

    private String getDateTime() {
        // TODO - check
        final String binary = toBin(mData[9]) + toBin(mData[10]) +
                toBin(mData[11]) + toBin(mData[12]) + toBin(mData[13]);
        final long unixSeconds = stringToInt(binary);
        final Date date = new Date(unixSeconds*1000L);
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(date);
    }

    private String toBin(byte numDec){
        final int num = unsignedToBytes(numDec);
        if(num == 0) {
            return "00000000";
        }
        String result = Integer.toBinaryString(num);
        int cont = 8 - result.length() % 8;
        if(cont != 8){
            for(int i = 0; i < cont; i++){
                result = "0" + result;
            }
        }
        return result;
    }

    private long stringToInt(String bin){
        long result = 0;
        bin = bin.trim();
        for(int i = bin.length() - 1; i >= 0; i--){
            String aux = String.valueOf(bin.charAt(i));
            result += Integer.parseInt(aux) * Math.pow(2, (bin.length() - 1 - i));
        }
        return result;
    }

    private static int unsignedToBytes(byte b) {
        return b & 0xFF;
    }

    private int fromByteArray(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }
}
