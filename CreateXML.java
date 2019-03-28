package com.billpay.main;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.billpay.main.DAOImpl;
import com.billpay.main.PaymentFetchQueries;

import com.billpay.main.PaymentRuntimeException;

public class CreateXML {

	public static int lfleCtr;
	public static int pLdCtr;
	public static String returnValue;
	static int cintvar = 0;
	public static int  globalVar=0;
	static Set<String> fiIds = new HashSet<String>();
	static List<MandateMerchantDetails> mmList = new ArrayList<MandateMerchantDetails>();

	public static void main(String[] args) {

		DAOImpl objDao = new DAOImpl();
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(PaymentFetchQueries.PAYMENT_DATA.getSQL());

		

		// Create main header

		DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder icBuilder;
		try {
			icBuilder = icFactory.newDocumentBuilder();
			Document doc = icBuilder.newDocument();

			Element mainRootElement = doc.createElementNS("", "n1:RFPBatchFile");
			mainRootElement.setAttribute("xmlns:n1", "http://ap.com/xsd/message/iso20022/RFPBatchInput");
			mainRootElement.setAttribute("xmlns:ba", "urn:iso:std:iso:20022:tech:xsd:head.001.001.01");
			mainRootElement.setAttribute("xmlns:rain2", "urn:iso:std:iso:20022:tech:xs:rain.002.001.01");
			mainRootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			mainRootElement.setAttribute("xsi:schemaLocation", "http://ap.com/xsd/message/iso20022/RFPBatchInput");

			doc.appendChild(mainRootElement);

			// append child elements to root element

			mainRootElement.appendChild(createHdr(doc, "RFPInputFromDistributor", UtilityMethods.staticgetUniueNum(),
					"RPPS", "Zapp", UtilityMethods.getCurrentDate()));

			try (ResultSet dbRecord = objDao.fetchPaymentData(queryBuilder.toString())) {

				while (dbRecord.next()) {

					MandateMerchantDetails file = new MandateMerchantDetails();

					assertNotNull("DB record null", dbRecord);
					assertNotNull("FIID column null", dbRecord.getString("FIID"));

					assertNotNull("DB record null", dbRecord);
					fiIds.add(dbRecord.getString("FIID"));
					file.fiId = dbRecord.getString("FIID");
					file.mid = dbRecord.getString("MERCHANT_IDENTIFICATION");
					file.acqId = dbRecord.getString("ACQUIRER_ID");
					file.distId = dbRecord.getString("DISTRIBUTORID");
					file.mndtId = dbRecord.getString("MANDATE_IDENTIFIER");
					file.mlrId = dbRecord.getString("MANDATE_LINK_RETRIEVAL_ID");
					file.baId = dbRecord.getString("BILLER_ACCOUNT_IDENTIFIER");

					mmList.add(file);

				}

				System.out.println("Total fileid returened from DB " + fiIds);
				lfleCtr = fiIds.size();
				for (String fileIs : fiIds) {
					System.out.println(fileIs + "-----------------");
					cintvar = 0;
					

					for (MandateMerchantDetails mmd : mmList) {
						

						if (mmd.fiId.equals(fileIs)) {

							cintvar = cintvar + 1;

						}

					}
					globalVar=globalVar+1;
					mainRootElement.appendChild(createLogclFile(doc, fileIs, cintvar,globalVar, "RTLRespFromDist", "",
							"RPPS", "Zapp", ""));

				}

				mainRootElement.appendChild(createTrlr(doc, "RFPInputFromDistributor", "", "RPPS", "Zapp",
						""));

				// output DOM XML to console
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				DOMSource source = new DOMSource(doc);
				StreamResult console = new StreamResult(System.out);
				transformer.transform(source, console);

				DOMSource domSource = new DOMSource(doc);
				StreamResult streamResult = new StreamResult(new File("d:\\RFPBatchFile.xml"));

				transformer.transform(domSource, streamResult);

				System.out.println("\nXML DOM Created Successfully..");
			}

		} catch (Exception e) {
			new PaymentRuntimeException("Issue with execution");

		}

	}

	// get hdr

	private static Node createHdr(Document doc, String FileTp, String FileRef, String SndgPty, String RcptPty,
			String FileBusDt) {
		Element hdr = doc.createElement("n1:Hdr");

		hdr.appendChild(getElements(doc, hdr, "n1:FileTp", FileTp));
		hdr.appendChild(getElements(doc, hdr, "n1:FileRef", FileRef));
		hdr.appendChild(getElement(doc, hdr, "n1:SndgPty"));
		hdr.appendChild(getElement(doc, hdr, "n1:RcptPty"));

		hdr.appendChild(getElements(doc, hdr, "n1:FileBusDt", FileBusDt));

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
	
	private static Node createLogclFile(Document doc, String fileIs, int i,int globalVar, String FileTp, String MsgRef,
			String SndgPty, String RcptPty, String FileBusDt) {
		Element LogclFile = doc.createElement("n1:LogclFile");

		// Create logical file header
		Element LogclHdr = (Element) LogclFile.appendChild(getElements(doc, LogclFile, "n1:LogclHdr", ""));

		LogclHdr.appendChild(getElements(doc, LogclHdr, "n1:LogclFileRef", UtilityMethods.staticgetUniueNum()));
		LogclHdr.appendChild(getElements(doc, LogclHdr, "n1:MsgTp", FileTp));
		LogclHdr.appendChild(getElements(doc, LogclHdr, "n1:SndgPty", ""));
		LogclHdr.appendChild(getElements(doc, LogclHdr, "n1:Acqrr", ""));
		LogclHdr.appendChild(getElements(doc, LogclHdr, "n1:Issr", ""));
		LogclHdr.appendChild(getElement(doc, LogclHdr, "n1:RcptPty"));

		// Create sub element for sending party

		Element sndgPtyid = getNewNodes(doc, "n1:Id", mmList.get(globalVar).distId);
		Element sndgPtytP = getNewNodes(doc, "n1:Tp", "DSTR");

		NodeList hdrCldNods = LogclHdr.getChildNodes();
		hdrCldNods.item(3).appendChild(sndgPtyid);

		hdrCldNods.item(3).appendChild(sndgPtytP);

		// Create sub element for Acquire

		Element Acqrrid = getNewNodes(doc, "n1:Id", mmList.get(globalVar).acqId);
		Element AcqrrtP = getNewNodes(doc, "n1:Tp", "ACQR");

		NodeList AcqrrCldNods = LogclHdr.getChildNodes();
		AcqrrCldNods.item(4).appendChild(Acqrrid);

		AcqrrCldNods.item(4).appendChild(AcqrrtP);

		// Create sub element for Issuer

		Element issrId = getNewNodes(doc, "n1:Id", mmList.get(globalVar).fiId);
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

		Element LogclMsg = (Element) LogclFile.appendChild(getElements(doc, LogclFile, "n1:Message", ""));

		for (pLdCtr = 0; pLdCtr < i; pLdCtr++) {

			Element RspnMessage = (Element) LogclMsg.appendChild(getElements(doc, LogclMsg, "n1:RspnMessage", ""));
			RspnMessage.appendChild(getElements(doc, LogclMsg, "n1:MsgRef", UtilityMethods.staticgetUniueNum()));

			creataPayLoad(RspnMessage, LogclMsg, doc, "RTLRespFromDist", "BHARAT12345", "RPPS", "Zapp",
					"2019-03-11T00:00:00Z");

		}

		// create logical file tailer

		Element LogclTrailer = (Element) LogclFile.appendChild(getElements(doc, LogclFile, "n1:LogclTrlr", ""));

		Element n1_NbOfTxs = (Element) LogclTrailer
				.appendChild(getElements(doc, LogclHdr, "n1:NbOfTxs", Integer.toString(cintvar)));
		return LogclFile;
	}

	public static void creataPayLoad(Element RspnMessage, Element LogclMsg, Document doc, String FileTp, String MsgRef,
			String SndgPty, String RcptPty, String FileBusDt)

	{
		Element RspnPayload = (Element) RspnMessage.appendChild(getElements(doc, LogclMsg, "n1:RspnPayload", ""));
		Element AppHdr = (Element) RspnPayload.appendChild(getElements(doc, LogclMsg, "ba:AppHdr", ""));

		// App header from
		Element Fr = (Element) AppHdr.appendChild(getElements(doc, LogclMsg, "ba:Fr", ""));
		Element OrgId = (Element) Fr.appendChild((getElements(doc, LogclMsg, "ba:OrgId", "")));
		Element Id = (Element) OrgId.appendChild((getElements(doc, LogclMsg, "ba:Id", "")));
		Element OrgId1 = (Element) Id.appendChild((getElements(doc, LogclMsg, "ba:OrgId", "")));
		Element Othr = (Element) OrgId1.appendChild((getElements(doc, LogclMsg, "ba:Othr", "")));
		Element Id1 = (Element) Othr.appendChild((getElements(doc, LogclMsg, "ba:Id", mmList.get(globalVar).distId)));

		// App header to
		Element To = (Element) AppHdr.appendChild(getElements(doc, LogclMsg, "ba:To", ""));
		Element OrgIdTo = (Element) To.appendChild((getElements(doc, LogclMsg, "ba:OrgId", "")));
		Element IdTo = (Element) OrgIdTo.appendChild((getElements(doc, LogclMsg, "ba:Id", "")));
		Element OrgId1To = (Element) IdTo.appendChild((getElements(doc, LogclMsg, "ba:OrgId", "")));
		Element OthrTo = (Element) OrgId1To.appendChild((getElements(doc, LogclMsg, "ba:Othr", "")));
		Element Id1To = (Element) OthrTo.appendChild((getElements(doc, LogclMsg, "ba:Id", "Zapp")));

		// Misc child under app header

		Element BizMsgIdr = (Element) AppHdr.appendChild(getElements(doc, LogclMsg, "ba:BizMsgIdr", "RTLRespFromDist"));
		Element MsgDefIdr = (Element) AppHdr.appendChild(getElements(doc, LogclMsg, "ba:MsgDefIdr", "rain.002.001.01"));
		Element BizSvc = (Element) AppHdr.appendChild(getElements(doc, LogclMsg, "ba:BizSvc", "RTLRespFromDist"));
		Element CreDt = (Element) AppHdr.appendChild(getElements(doc, LogclMsg, "ba:CreDt", UtilityMethods.getCurrentDate()));

		// Rain2 document section

		Element rain2Document = (Element) RspnPayload.appendChild(getElements(doc, LogclMsg, "rain2:Document", ""));
		Element rain2Prsntmnt = (Element) rain2Document.appendChild(getElements(doc, LogclMsg, "rain2:Prsntmnt", ""));
		Element rain2Hdr = (Element) rain2Prsntmnt.appendChild(getElements(doc, LogclMsg, "rain2:Hdr", ""));
		Element rain2PrsntmntRes = (Element) rain2Prsntmnt
				.appendChild(getElements(doc, LogclMsg, "rain2:PrsntmntRes", ""));

		// rain2Header children

		Element rain2MsgFctn = (Element) rain2Hdr.appendChild(getElements(doc, LogclMsg, "rain2:MsgFctn", "OPIN"));
		Element rain2PrtcolVrsn = (Element) rain2Hdr.appendChild(getElements(doc, LogclMsg, "rain2:PrtcolVrsn", "001"));
		Element rain2XchgId = (Element) rain2Hdr.appendChild(getElements(doc, LogclMsg, "rain2:XchgId", "001"));
		Element rain2CreDtTm = (Element) rain2Hdr
				.appendChild(getElements(doc, LogclMsg, "rain2:CreDtTm",UtilityMethods.getCurrentDate()));
		Element rain2InitgPty = (Element) rain2Hdr.appendChild(getElements(doc, LogclMsg, "rain2:InitgPty", ""));
		Element rain2RcptPty = (Element) rain2Hdr.appendChild(getElements(doc, LogclMsg, "rain2:RcptPty", ""));

		// rain2InitgPty children

		Element rain2Id = (Element) rain2InitgPty.appendChild(getElements(doc, LogclMsg, "rain2:Id",  mmList.get(globalVar).distId));
		Element rain2Tp = (Element) rain2InitgPty.appendChild(getElements(doc, LogclMsg, "rain2:Tp", "DSTR"));

		// rain2RcptPty children

		Element rain2RcptPtyrain2Id = (Element) rain2RcptPty
				.appendChild(getElements(doc, LogclMsg, "rain2:Id", "ZAPP"));
		Element rain2RcptPtyrain2Tp = (Element) rain2RcptPty
				.appendChild(getElements(doc, LogclMsg, "rain2:Tp", "SCHM"));

		// rain2:Tracblt children

		Element rain2Tracblt = (Element) rain2Hdr.appendChild(getElements(doc, LogclMsg, "rain2:Tracblt", ""));
		Element rain2RlayId = (Element) rain2Tracblt.appendChild(getElements(doc, LogclMsg, "rain2:RlayId", ""));

		// rain2RlayId children

		Element rain2RlayIdrain2Id = (Element) rain2RlayId
				.appendChild(getElements(doc, LogclMsg, "rain2:Id", UtilityMethods.staticgetUniueNum()));
		Element rain2RlayIdrain2Tp = (Element) rain2RlayId.appendChild(getElements(doc, LogclMsg, "rain2:Tp", "DSTR"));

		// rain2PrsntmntRes children
		Element rain2Envt = (Element) rain2PrsntmntRes.appendChild(getElements(doc, LogclMsg, "rain2:Envt", ""));
		Element rain2Tx = (Element) rain2PrsntmntRes.appendChild(getElements(doc, LogclMsg, "rain2:Tx", ""));

		// rain2Envt children

		Element rain2Acqrr = (Element) rain2Envt.appendChild(getElements(doc, LogclMsg, "rain2:Acqrr", ""));
		Element rain2Mrchnt = (Element) rain2Envt.appendChild(getElements(doc, LogclMsg, "rain2:Mrchnt", ""));
		Element rain2CstmrFinInst = (Element) rain2Envt
				.appendChild(getElements(doc, LogclMsg, "rain2:CstmrFinInst", ""));
		Element rain2PmtTkn = (Element) rain2Envt.appendChild(getElements(doc, LogclMsg, "rain2:PmtTkn", ""));

		// rain2Acqrr children

		Element rain2AcqrrId = (Element) rain2Acqrr.appendChild(getElements(doc, LogclMsg, "rain2:Id", ""));

		Element rain2Id_accr = (Element) rain2AcqrrId.appendChild(getElements(doc, LogclMsg, "rain2:Id", "250051"));
		Element rain2Tp_accr = (Element) rain2AcqrrId.appendChild(getElements(doc, LogclMsg, "rain2:Tp", "ACQR"));

		// rain2Mrchnt children

		Element rain2Mrchnt_id = (Element) rain2Mrchnt.appendChild(getElements(doc, LogclMsg, "rain2:Id", ""));
		Element CmonNm = (Element) rain2Mrchnt.appendChild(getElements(doc, LogclMsg, "rain2:CmonNm", "BHARAT123"));

		Element rain2idaccr = (Element) rain2Mrchnt_id
				.appendChild(getElements(doc, LogclMsg, "rain2:Id",mmList.get(globalVar).mid ));
		Element rain2Tpaccr = (Element) rain2Mrchnt_id.appendChild(getElements(doc, LogclMsg, "rain2:Tp", "MERC"));
		Element rain2_Issr = (Element) rain2Mrchnt_id.appendChild(getElements(doc, LogclMsg, "rain2:Issr", "DSTR"));

		// rain2:CstmrFinInst children

		Element rain2_Id = (Element) rain2CstmrFinInst.appendChild(getElements(doc, LogclMsg, "rain2:Id", ""));
		Element rain2Id2 = (Element) rain2_Id.appendChild(getElements(doc, LogclMsg, "rain2:Id", "000027"));
		Element rain2_Tp = (Element) rain2_Id.appendChild(getElements(doc, LogclMsg, "rain2:Tp", "FINI"));

		// rain2PmtTkn children

		Element rain2Tkn = (Element) rain2PmtTkn
				.appendChild(getElements(doc, LogclMsg, "rain2:Tkn", "2695901011421034"));

		// rain2:Tx children

		Element rain2TxTp = (Element) rain2Tx.appendChild(getElements(doc, LogclMsg, "rain2:TxTp", ""));
		Element rain2PdctTp = (Element) rain2Tx.appendChild(getElements(doc, LogclMsg, "rain2:PdctTp", "NTRQ"));
		Element rain2ChckoutTp = (Element) rain2Tx.appendChild(getElements(doc, LogclMsg, "rain2:ChckoutTp", "NTRQ"));
		Element rain2TxId = (Element) rain2Tx.appendChild(getElements(doc, LogclMsg, "rain2:TxId", ""));
		Element rain2TxDtls = (Element) rain2Tx.appendChild(getElements(doc, LogclMsg, "rain2:TxDtls", ""));
		Element rain2AuthstnRslt = (Element) rain2Tx.appendChild(getElements(doc, LogclMsg, "rain2:AuthstnRslt", ""));

		Element rain2Tp_tx = (Element) rain2TxTp.appendChild(getElements(doc, LogclMsg, "rain2:Tp", "RGST"));
		Element rain2AddnlTxTp = (Element) rain2TxTp.appendChild(getElements(doc, LogclMsg, "rain2:AddnlTxTp", "NTRQ"));

		// rain2:TxId children

		Element rain2SchmTxId = (Element) rain2TxId
				.appendChild(getElements(doc, LogclMsg, "rain2:SchmTxId", "181525712214219499"));
		Element rain2Rcncltn = (Element) rain2TxId.appendChild(getElements(doc, LogclMsg, "rain2:Rcncltn", ""));
		Element rain2RcncltnDt = (Element) rain2Rcncltn
				.appendChild(getElements(doc, LogclMsg, "rain2:RcncltnDt", "2019-03-11"));

		Element rain2TxAmts = (Element) rain2TxDtls.appendChild(getElements(doc, LogclMsg, "rain2:TxAmts", ""));
		Element rain2BllgDtls = (Element) rain2TxDtls.appendChild(getElements(doc, LogclMsg, "rain2:BllgDtls", ""));

		Element rain2TtlAmt = (Element) rain2TxAmts.appendChild(getElements(doc, LogclMsg, "rain2:TtlAmt", "0"));
		Element rain2_BllgactId = (Element) rain2BllgDtls
				.appendChild(getElements(doc, LogclMsg, "rain2:BllgactId", "9236602"));

		// rain2:AuthstnRslt children

		Element rain2_TxRspn = (Element) rain2AuthstnRslt.appendChild(getElements(doc, LogclMsg, "rain2:TxRspn", ""));
		Element rain2_Rslt = (Element) rain2_TxRspn.appendChild(getElements(doc, LogclMsg, "rain2:Rslt", "APPR"));

	}

	private static Node createTrlr(Document doc, String FileTp, String FileRef, String SndgPty, String RcptPty,
			String FileBusDt) {
		Element Trlr = doc.createElement("n1:Trlr");
		Trlr.appendChild(getElements(doc, Trlr, "n1:LogclFileCnt", Integer.toString(lfleCtr)));

		return Trlr;
	}

	// utility method to create text node
	private static Node getElements(Document doc, Element element, String name, String value) {
		Element node = doc.createElement(name);
		node.appendChild(doc.createTextNode(value));
		return node;
	}

	// utility method to create text node
	private static Node getElement(Document doc, Element element, String name) {
		Element node = doc.createElement(name);
		return node;
	}

	private static Element getNewNodes(Document doc, String element, String value) {
		Element node = doc.createElement(element);
		node.appendChild(doc.createTextNode(value));
		return node;

	}
}
