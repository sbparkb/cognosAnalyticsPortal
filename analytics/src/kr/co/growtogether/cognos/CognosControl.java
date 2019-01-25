package kr.co.growtogether.cognos;

import java.util.ArrayList;

import com.cognos.developer.schemas.bibus._3.Account;
import com.cognos.developer.schemas.bibus._3.BaseClass;
import com.cognos.developer.schemas.bibus._3.ContentManagerService_PortType;
import com.cognos.developer.schemas.bibus._3.OrderEnum;
import com.cognos.developer.schemas.bibus._3.PropEnum;
import com.cognos.developer.schemas.bibus._3.QueryOptions;
import com.cognos.developer.schemas.bibus._3.Report;
import com.cognos.developer.schemas.bibus._3.SearchPathMultipleObject;
import com.cognos.developer.schemas.bibus._3.Shortcut;
import com.cognos.developer.schemas.bibus._3.Sort;
import com.cognos.developer.schemas.bibus._3.URL;
import com.cognos.developer.schemas.bibus._3.UiClass;

public class CognosControl {
	
	public CognosObject getUserInfo(ContentManagerService_PortType cmService) {
		CognosObject object = null;
		try {
			PropEnum[] prop = {PropEnum.userName, PropEnum.defaultName, PropEnum.storeID};
			BaseClass[] bc = cmService.query(new SearchPathMultipleObject("~"), prop, new Sort[] {}, new QueryOptions());
			if (bc != null && bc.length > 0) {
				object = new CognosObject();				
				object.setUserName(((Account) bc[0]).getUserName().getValue());
				// 사용자명
				object.setDefaultName(bc[0].getDefaultName().getValue());
				// StoreID
				object.setStoreId(bc[0].getStoreID().getValue().get_value());
			}
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}

		return object;
	}
	
	public ArrayList<CognosObject> getContentList(String searchPath, ContentManagerService_PortType cmService) {
		
		SearchPathMultipleObject cmSearchPath = new SearchPathMultipleObject(searchPath);
		
		ArrayList<CognosObject> list = new ArrayList<CognosObject>();
		
		try
		{
			PropEnum[] properties =	{ PropEnum.defaultName
									 ,PropEnum.searchPath
									 ,PropEnum.objectClass
									 ,PropEnum.hasChildren
									 ,PropEnum.storeID
									 };
			
			BaseClass myCMObject;
			
			BaseClass[] children;
			
			myCMObject = (cmService.query(cmSearchPath,properties,new Sort[] {},new QueryOptions()))[0];

			String appendString = "/*[permission('read')]";
			if (searchPath.lastIndexOf("/") == (searchPath.length() - 1))
			{
				appendString = "*[permission('read')]";
			}
			if (searchPath.lastIndexOf("*") == (searchPath.length() - 1))
			{
				appendString = "[permission('read')]";
			}
			Sort nodeSortType = new Sort();
			Sort nodeSortName = new Sort();
		
			nodeSortType.setOrder(OrderEnum.ascending);
			nodeSortType.setPropName(PropEnum.objectClass);
			nodeSortName.setOrder(OrderEnum.ascending);
			nodeSortName.setPropName(PropEnum.defaultName);
			Sort[] nodeSorts = new Sort[] {nodeSortType, nodeSortName};

			if(myCMObject.getHasChildren().isValue())
			{
				cmSearchPath.set_value(searchPath + appendString);
				children =cmService.query(cmSearchPath,properties,nodeSorts,new QueryOptions());
				
				if(children != null && children.length > 0) {
					for(int i=0;i<children.length;i++) {
						CognosObject obj = new CognosObject();
						obj.setSearchPath(children[i].getSearchPath().getValue());
						obj.setDefaultName(children[i].getDefaultName().getValue());
						obj.setObjectType(children[i].getObjectClass().getValue().getValue());						
						obj.setStoreId(children[i].getStoreID().getValue().get_value());
						
						list.add(obj);
					}
				}
			}
			
		}catch (java.rmi.RemoteException remoteEx){
			return null;
		}
		
		return list;
	}

	public ArrayList<CognosObject> getContentListSid(String storeId, ContentManagerService_PortType cmService) {
		
		SearchPathMultipleObject cmSearchPath = new SearchPathMultipleObject("storeID(\"" + storeId + "\")/*[permission('read')]");
		
		ArrayList<CognosObject> list = new ArrayList<CognosObject>();
		
		try
		{
			PropEnum[] properties =	{ PropEnum.defaultName
									 ,PropEnum.searchPath
									 ,PropEnum.objectClass
									 ,PropEnum.hasChildren
									 ,PropEnum.storeID
									 };
			
			BaseClass myCMObject;
			
			BaseClass[] children;
			
			myCMObject = (cmService.query(cmSearchPath,properties,new Sort[] {},new QueryOptions()))[0];
	
			Sort nodeSortType = new Sort();
			Sort nodeSortName = new Sort();
		
			nodeSortType.setOrder(OrderEnum.ascending);
			nodeSortType.setPropName(PropEnum.objectClass);
			nodeSortName.setOrder(OrderEnum.ascending);
			nodeSortName.setPropName(PropEnum.defaultName);
			Sort[] nodeSorts = new Sort[] {nodeSortType, nodeSortName};
	
			if(myCMObject.getHasChildren().isValue())
			{
				children =cmService.query(cmSearchPath,properties,nodeSorts,new QueryOptions());
				
				if(children != null && children.length > 0) {
					for(int i=0;i<children.length;i++) {
						CognosObject obj = new CognosObject();
						obj.setSearchPath(children[i].getSearchPath().getValue());
						obj.setDefaultName(children[i].getDefaultName().getValue());
						obj.setObjectType(children[i].getObjectClass().getValue().getValue());						
						obj.setStoreId(children[i].getStoreID().getValue().get_value());						
						list.add(obj);
					}
				}
			}
			
		}catch (java.rmi.RemoteException remoteEx){
			return null;
		}
		
		return list;
	}
	
	/**
	 * StoreID를 이용하여 보고서 정보를 조회한다.
	 * Gateway 지정으로 보고서 실행 URL 생성한다.
	 * @param String storeId
	 * @param String gateway  setDefaultOutput  setExecuteUrl
	 * @return
	 */
	public CognosObject getCognosObjectInfo(String gateway, String storeId, ContentManagerService_PortType cmService, String promptTf) {
	
		CognosObject obj = null;
		if (storeId == null || "".equals(storeId) || gateway == null || "".equals(gateway)) {
			return obj;
		}						

		try {						
			PropEnum[] prop = {PropEnum.searchPath, PropEnum.defaultName, PropEnum.storeID, PropEnum.objectClass, PropEnum.defaultOutputFormat, PropEnum.target, PropEnum.uri, PropEnum.description};
			BaseClass[] bc = cmService.query(new SearchPathMultipleObject("storeID(\"" + storeId + "\")"), prop, new Sort[] {}, new QueryOptions());
			
			if(bc != null && bc.length > 0) {
				obj = new CognosObject();
				obj.setSearchPath(bc[0].getSearchPath().getValue());
				obj.setDefaultName(bc[0].getDefaultName().getValue());
				obj.setStoreId(bc[0].getStoreID().getValue().get_value());
				obj.setObjectType(bc[0].getObjectClass().getValue().getValue());
	
				if (((UiClass)bc[0]).getDescription().getValue() != null && ((UiClass)bc[0]).getDescription().getValue().length > 0) {
					obj.setDescription(((UiClass)bc[0]).getDescription().getValue()[0].getValue());
				}
				
				String endcodedUrl = java.net.URLEncoder.encode(obj.getSearchPath(), "UTF-8").replaceAll("\\+","%20");
				
				if ("report".equals(obj.getObjectType())) {
	
					if( ( (Report)bc[0] ).getDefaultOutputFormat().getValue() != null) {
						obj.setDefaultOutput(((Report)bc[0]).getDefaultOutputFormat().getValue()[0]);						
						obj.setExecuteUrl(gateway + "?b_action=cognosViewer&ui.action=view&ui.object=defaultOutput(" + endcodedUrl + ")&ui.format=" + obj.getDefaultOutput()+"&cv.header=false&cv.toolbar=false");
					} else {
						obj.setExecuteUrl(gateway + "?b_action=cognosViewer&ui.action=run&ui.object=" + endcodedUrl + "&ui.name=" + obj.getDefaultName() + "&run.outputFormat=&run.prompt="+promptTf+"&cv.header=false&cv.toolbar=false");						
					}
				} else if ("URL".equals(obj.getObjectType())) {
					obj.setExecuteUrl(((URL)bc[0]).getUri().getValue());
				} else if ("shortcut".equals(obj.getObjectType())) {
					String path = ((Shortcut)bc[0]).getTarget().getValue()[0].getSearchPath().getValue();
					String endcodedShortcutUrl = java.net.URLEncoder.encode(path, "UTF-8").replaceAll("\\+","%20");
					if (path.indexOf("report") >= 0) {
						obj.setTargetObjectType("report");
						obj.setExecuteUrl(gateway + "?b_action=cognosViewer&ui.action=run&ui.object=" + endcodedShortcutUrl + "&run.outputFormat=&run.prompt="+promptTf+"&cv.header=false&cv.toolbar=false");
					} 				
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			obj = null;
		}
	
		return obj;
	}
}
