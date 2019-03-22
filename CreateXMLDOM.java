package com.billpay.main;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CreateXMLDOM {

	public static void main(String[] args) {
		DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder icBuilder;
		try {
			icBuilder = icFactory.newDocumentBuilder();
			Document doc = icBuilder.newDocument();

			Element mainRootElement = doc.createElementNS("", "RFPBatchFile");
			mainRootElement.setAttribute("xmlns:n1", "http://ap.com/xsd/message/iso20022/RFPBatchInput");
			mainRootElement.setAttribute("xmlns:ba", "urn:iso:std:iso:20022:tech:xsd:head.001.001.01");
			mainRootElement.setAttribute("xmlns:rain2", "urn:iso:std:iso:20022:tech:xs:rain.002.001.01");
			mainRootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			mainRootElement.setAttribute("xsi:schemaLocation", "http://ap.com/xsd/message/iso20022/RFPBatchInput");

			doc.appendChild(mainRootElement);

			// append child elements to root element
			mainRootElement.appendChild(
					getHdr(doc, "RFPInputFromDistributor", "BHARAT12345", "RPPS", "Zapp", "2019-03-11T00:00:00Z"));

			mainRootElement.appendChild(
					getLogclFile(doc, "RTLRespFromDist", "BHARAT12345", "RPPS", "Zapp", "2019-03-11T00:00:00Z"));

			mainRootElement.appendChild(
					getTrlr(doc, "RFPInputFromDistributor", "BHARAT12345", "RPPS", "Zapp", "2019-03-11T00:00:00Z"));

			// output DOM XML to console
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);
			StreamResult console = new StreamResult(System.out);
			transformer.transform(source, console);

			System.out.println("\nXML DOM Created Successfully..");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// get hdr
	private static Node getHdr(Document doc, String FileTp, String FileRef, String SndgPty, String RcptPty,
			String FileBusDt) {
		Element hdr = doc.createElement("n1:Hdr");

		hdr.appendChild(getCompanyElements(doc, hdr, "n1:FileTp", FileTp));
		hdr.appendChild(getCompanyElements(doc, hdr, "n1:FileRef", FileRef));
		hdr.appendChild(getCompanyElement(doc, hdr, "n1:SndgPty"));
		hdr.appendChild(getCompanyElement(doc, hdr, "n1:RcptPty"));

		hdr.appendChild(getCompanyElements(doc, hdr, "n1:FileBusDt", FileBusDt));

		// Create sub element for sending party

		Element sndgPtyid = getNewNodes(doc, "n1:Id", "RPPS");
		Element sndgPtytP = getNewNodes(doc, "n1:Tp", "DSTR");

		NodeList hdrCldNods = hdr.getChildNodes();
		hdrCldNods.item(2).appendChild(sndgPtyid);

		hdrCldNods.item(2).appendChild(sndgPtytP);

		// Create sub element for recipient party

		Element rcptPtyid = getNewNodes(doc, "n1:Id", "Zapp");
		Element rcptPtytP = getNewNodes(doc, "n1:Tp", "SCHM");

		NodeList hdrCldNodsFinal = hdr.getChildNodes();
		hdrCldNodsFinal.item(3).appendChild(rcptPtyid);

		hdrCldNodsFinal.item(3).appendChild(rcptPtytP);

		return hdr;
	}

	// get logicalfile
	private static Node getLogclFile(Document doc, String FileTp, String MsgRef, String SndgPty, String RcptPty,
			String FileBusDt) {
		Element LogclFile = doc.createElement("n1:LogclFile");

		// Create logical file header
		Element LogclHdr = (Element) LogclFile.appendChild(getCompanyElements(doc, LogclFile, "n1:LogclHdr", ""));

		LogclHdr.appendChild(getCompanyElements(doc, LogclHdr, "n1:LogclFileRef", MsgRef));
		LogclHdr.appendChild(getCompanyElements(doc, LogclHdr, "n1:MsgTp", FileTp));
		LogclHdr.appendChild(getCompanyElements(doc, LogclHdr, "n1:SndgPty", ""));
		LogclHdr.appendChild(getCompanyElements(doc, LogclHdr, "n1:Acqrr", ""));
		LogclHdr.appendChild(getCompanyElements(doc, LogclHdr, "n1:Issr", ""));
		LogclHdr.appendChild(getCompanyElement(doc, LogclHdr, "n1:RcptPty"));
		// LogclFile.appendChild(getCompanyElement(doc, LogclFile, "n1:RcptPty"));

		// LogclFile.appendChild(getCompanyElements(doc, LogclFile, "n1:FileBusDt",
		// FileBusDt));

		// Create sub element for sending party

		Element sndgPtyid = getNewNodes(doc, "n1:Id", "000104");
		Element sndgPtytP = getNewNodes(doc, "n1:Tp", "DSTR");

		NodeList hdrCldNods = LogclHdr.getChildNodes();
		hdrCldNods.item(3).appendChild(sndgPtyid);

		hdrCldNods.item(3).appendChild(sndgPtytP);

		// Create sub element for Acquire

		Element Acqrrid = getNewNodes(doc, "n1:Id", "250051");
		Element AcqrrtP = getNewNodes(doc, "n1:Tp", "ACQR");

		NodeList AcqrrCldNods = LogclHdr.getChildNodes();
		AcqrrCldNods.item(4).appendChild(Acqrrid);

		AcqrrCldNods.item(4).appendChild(AcqrrtP);

		// Create sub element for Issuer

		Element issrId = getNewNodes(doc, "n1:Id", "000027");
		Element issrTp = getNewNodes(doc, "n1:Tp", "FINI");

		NodeList issrIdCldNods = LogclHdr.getChildNodes();
		issrIdCldNods.item(5).appendChild(issrId);

		issrIdCldNods.item(5).appendChild(issrTp);

		// Create sub element for recipient party.

		Element recptId = getNewNodes(doc, "n1:Id", "Zapp");
		Element recptTp = getNewNodes(doc, "n1:Tp", "SCHM");

		NodeList recptCldNods = LogclHdr.getChildNodes();
		recptCldNods.item(6).appendChild(recptId);

		recptCldNods.item(6).appendChild(recptTp);

		// create logical file message

		Element LogclMsg = (Element) LogclFile.appendChild(getCompanyElements(doc, LogclFile, "n1:Message", ""));

//      LogclMsg.appendChild(getCompanyElements(doc, LogclMsg, "n1:LogclFileRef", FileTp));
//      LogclMsg.appendChild(getCompanyElements(doc, LogclMsg, "n1:MsgTp", FileTp));
//      LogclMsg.appendChild(getCompanyElements(doc, LogclMsg, "n1:SndgPty", FileTp));
//      LogclMsg.appendChild(getCompanyElements(doc, LogclMsg, "n1:Acqrr", FileTp));
//      LogclMsg.appendChild(getCompanyElements(doc, LogclMsg, "n1:Issr", FileRef));
//      LogclMsg.appendChild(getCompanyElement(doc, LogclMsg, "n1:RcptPty"));
//		

		// create logical file tailer

		Element LogclTrailer = (Element) LogclFile.appendChild(getCompanyElements(doc, LogclFile, "n1:LogclTrlr", ""));

//       LogclTrailer.appendChild(getCompanyElements(doc, LogclHdr, "n1:LogclFileRef", FileTp));
//       LogclTrailer.appendChild(getCompanyElements(doc, LogclHdr, "n1:MsgTp", FileTp));
//       LogclTrailer.appendChild(getCompanyElements(doc, LogclHdr, "n1:SndgPty", FileTp));
//       LogclTrailer.appendChild(getCompanyElements(doc, LogclHdr, "n1:Acqrr", FileTp));
//       LogclTrailer.appendChild(getCompanyElements(doc, LogclHdr, "n1:Issr", FileRef));
//       LogclTrailer.appendChild(getCompanyElement(doc, LogclHdr, "n1:RcptPty"));
//		

		return LogclFile;
	}

	private static Node getTrlr(Document doc, String FileTp, String FileRef, String SndgPty, String RcptPty,
			String FileBusDt) {
		Element Trlr = doc.createElement("n1:Trlr");
		Trlr.appendChild(getCompanyElements(doc, Trlr, "n1:Trlr", FileTp));

		return Trlr;
	}

	// utility method to create text node
	private static Node getCompanyElements(Document doc, Element element, String name, String value) {
		Element node = doc.createElement(name);
		node.appendChild(doc.createTextNode(value));
		return node;
	}

	// utility method to create text node
	private static Node getCompanyElement(Document doc, Element element, String name) {
		Element node = doc.createElement(name);
		// node.appendChild(doc.createTextNode(value));
		return node;
	}

	private static Element getNewNodes(Document doc, String element, String value) {
		Element node = doc.createElement(element);
		node.appendChild(doc.createTextNode(value));
		return node;
	}
}
