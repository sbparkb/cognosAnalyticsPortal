<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.cognos.developer.schemas.bibus._3.Account"%>
<%@ page import="com.cognos.developer.schemas.bibus._3.Report"%>
<%@ page import="com.cognos.developer.schemas.bibus._3.QueryOptions"%>
<%@ page import="com.cognos.developer.schemas.bibus._3.Sort"%>
<%@ page import="com.cognos.developer.schemas.bibus._3.SearchPathMultipleObject"%>
<%@ page import="com.cognos.developer.schemas.bibus._3.BaseClass"%>
<%@ page import="com.cognos.developer.schemas.bibus._3.PropEnum"%>
<%@ page import="com.cognos.developer.schemas.bibus._3.XmlEncodedXML"%>
<%@ page import="com.cognos.developer.schemas.bibus._3.SearchPathSingleObject"%>
<%@ page import="javax.xml.namespace.QName"%>
<%@ page import="com.cognos.developer.schemas.bibus._3.RoutingInfo"%>
<%@ page import="org.apache.axis.client.Stub"%>
<%@ page import="org.apache.axis.message.SOAPHeaderElement"%>
<%@ page import="com.cognos.developer.schemas.bibus._3.BiBusHeader"%>
<%@ page import="com.cognos.developer.schemas.bibus._3.CAMPassport"%>
<%@ page import="com.cognos.developer.schemas.bibus._3.CAM"%>
<%@ page import="com.cognos.developer.schemas.bibus._3.ContentManagerService_PortType"%>
<%@ page import="com.cognos.developer.schemas.bibus._3.ContentManagerService_ServiceLocator"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Running Report...</title>
</head>
<body>
<%!
private static final String BIBUS_NS = "http://developer.cognos.com/schemas/bibus/3/";

private static final String BIBUS_HDR = "biBusHeader";

private static final QName BUS_QNAME = new QName(BIBUS_NS, BIBUS_HDR);


public static BiBusHeader getHeaderObject(SOAPHeaderElement SourceHeader, boolean isNewConversation, String RSGroup) 
{	
	
	if (SourceHeader == null)
		return null;
	
	BiBusHeader bibus = null;
	try {
		bibus = (BiBusHeader)SourceHeader.getValueAsType(BUS_QNAME);
        // Note BUS_QNAME expands to: 
        // new QName("http://developer.cognos.com/schemas/bibus/3/", "biBusHeader")
        
		//If the header will be used for a new conversation, clear
		//tracking information, and set routing if supplied (clear if not)
        if (isNewConversation){
            
            bibus.setTracking(null);
            
            //If a Routing Server Group is specified, direct requests to it
            if (RSGroup.length()>0) {
                RoutingInfo routing = new RoutingInfo(RSGroup); 
                bibus.setRouting(routing);
            }                  
            else {
                bibus.setRouting(null);
            }
        }
	} catch (Exception e) {
		
		e.printStackTrace();
	}
	
	return bibus;
}

public String getExecuteUrl(String gateway, String storeId, ContentManagerService_PortType cmService) {
	
	String executeUrl = "";
	
	if (storeId == null || "".equals(storeId) || gateway == null || "".equals(gateway)) {
		return "";
	}						

	try {	
		PropEnum[] prop = {PropEnum.searchPath, PropEnum.defaultName, PropEnum.storeID, PropEnum.objectClass, PropEnum.defaultOutputFormat, PropEnum.target, PropEnum.uri, PropEnum.description};
		BaseClass[] bc = cmService.query(new SearchPathMultipleObject("storeID(\"" + storeId + "\")"), prop, new Sort[] {}, new QueryOptions());
		
		if(bc != null && bc.length > 0) {

			String objectType = bc[0].getObjectClass().getValue().getValue();
			String searchPath = bc[0].getSearchPath().getValue();

						
			String endcodedUrl = java.net.URLEncoder.encode(searchPath, "UTF-8").replaceAll("\\+","%20");
						
			if ("report".equals(objectType)) {
			
				if( ( (Report)bc[0] ).getDefaultOutputFormat().getValue() != null) {
					String getDefaultOutput = ((Report)bc[0]).getDefaultOutputFormat().getValue()[0];
					executeUrl = gateway + "?b_action=cognosViewer&ui.action=view&ui.object=defaultOutput(" + endcodedUrl + ")&ui.format=" + getDefaultOutput+"&cv.header=false&cv.toolbar=false";
				}else{								
					String defaultName = bc[0].getDefaultName().getValue();
					executeUrl = gateway + "?b_action=cognosViewer&ui.action=run&ui.object=" + endcodedUrl + "&ui.name=" + defaultName + "&run.outputFormat=&run.prompt=false&cv.header=false&cv.toolbar=false";
				}				
			}
		}
	} catch (Exception e) {
		e.printStackTrace();
	}

	return "";
}
%>
<%
	/*
	String CMURL = "http://june-pc:9300/p2pd/servlet/dispatch";
	String gateway = "http://june-PC:9300/bi/v1/disp";
	String storeId = "i51934401914C458EB3AF477B745962C9";
	String namespace = "DBAuth";
	String uid = "admin";
	String pwd = "1234";
	*/
	
	String CMURL = request.getParameter("CMURL");
	String gateway = request.getParameter("gateway");
	String storeId = request.getParameter("storeId");
	String namespace = request.getParameter("namespace");
	String uid = request.getParameter("uid");
	String pwd = request.getParameter("pwd");

	java.net.URL serverURL = new java.net.URL(CMURL);

	ContentManagerService_PortType cmService = null;
	ContentManagerService_ServiceLocator cmServiceLocator = null;	
	
	cmServiceLocator = new ContentManagerService_ServiceLocator();
	cmService = cmServiceLocator.getcontentManagerService(serverURL);
	
    BiBusHeader bibus = null;
	SOAPHeaderElement x = ((Stub)cmService).getResponseHeader("http://developer.cognos.com/schemas/bibus/3/", "biBusHeader");
	bibus = getHeaderObject(x, true, "");

    if (!(bibus == null)) 
     {
        ((Stub)cmService).clearHeaders();
        ((Stub)cmService).setHeader("http://developer.cognos.com/schemas/bibus/3/", "biBusHeader", bibus); 
    }
		
    StringBuffer credentialXML = new StringBuffer();

	credentialXML.append("<credential>");

	credentialXML.append("<namespace>");
	credentialXML.append(namespace);
	credentialXML.append("</namespace>");

	credentialXML.append("<username>");
	credentialXML.append(uid);
	credentialXML.append("</username>");

	credentialXML.append("<password>");
	credentialXML.append(pwd);
	credentialXML.append("</password>");

	credentialXML.append("</credential>");

	String encodedCredentials = credentialXML.toString();
	
	cmService.logon(new XmlEncodedXML(encodedCredentials), new SearchPathSingleObject[] {});
		
	SOAPHeaderElement soapHeader = ((Stub)cmService).getResponseHeader("http://developer.cognos.com/schemas/bibus/3/", "biBusHeader");
	((Stub)cmService).setHeader(soapHeader);  //이 라인이 없어지면 권한 오류가 발생한다. 로그인 후 반드시 실행해야 함 
				
	PropEnum[] prop = {PropEnum.searchPath, PropEnum.defaultName, PropEnum.storeID, PropEnum.objectClass, PropEnum.defaultOutputFormat, PropEnum.target, PropEnum.uri, PropEnum.description};
	BaseClass[] bc = cmService.query(new SearchPathMultipleObject("storeID(\"" + storeId + "\")"), prop, new Sort[] {}, new QueryOptions());
	
	String executeUrl = "";
	String defaultName = "";
	
	if(bc != null && bc.length > 0) {

		String objectType = bc[0].getObjectClass().getValue().getValue();
		String searchPath = bc[0].getSearchPath().getValue();

					
		String endcodedUrl = java.net.URLEncoder.encode(searchPath, "UTF-8").replaceAll("\\+","%20");
					
		if ("report".equals(objectType)) {
		
			if( ( (Report)bc[0] ).getDefaultOutputFormat().getValue() != null) {
				String getDefaultOutput = ((Report)bc[0]).getDefaultOutputFormat().getValue()[0];
				executeUrl = gateway + "?b_action=cognosViewer&ui.action=view&ui.object=defaultOutput(" + endcodedUrl + ")&ui.format=" + getDefaultOutput+"&cv.header=false&cv.toolbar=false";
			}else{								
				defaultName = bc[0].getDefaultName().getValue();
				executeUrl = gateway + "?b_action=cognosViewer&ui.action=run&ui.object=" + endcodedUrl + "&ui.name=" + defaultName + "&run.outputFormat=&run.prompt=false&cv.header=false&cv.toolbar=false";
			}				
		}
	}

%>
<script>
</script>
보고서 실행 URL
<input type="text" value="<%=executeUrl %>" style="width: 100%;"><br><br> 
보고서 링크 <a href="<%=executeUrl %>"><br><%=defaultName%></a>
</body>
</html>