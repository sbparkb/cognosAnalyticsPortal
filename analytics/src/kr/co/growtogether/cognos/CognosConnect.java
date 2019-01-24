package kr.co.growtogether.cognos;

import java.net.MalformedURLException;

import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Stub;

import com.cognos.developer.schemas.bibus._3.BiBusHeader;
import com.cognos.developer.schemas.bibus._3.CAM;
import com.cognos.developer.schemas.bibus._3.CAMPassport;
import com.cognos.developer.schemas.bibus._3.ContentManagerService_PortType;
import com.cognos.developer.schemas.bibus._3.ContentManagerService_ServiceLocator;
import com.cognos.developer.schemas.bibus._3.CookieVar;
import com.cognos.developer.schemas.bibus._3.HdrSession;

public class CognosConnect {
	
	private ContentManagerService_PortType cmService = null;
	private ContentManagerService_ServiceLocator cmServiceLocator = null;

	public ContentManagerService_PortType connectToCognosServer(String CMURL, String camPassport)
	{
		// Create the service locators
		cmServiceLocator = new ContentManagerService_ServiceLocator();
		
		try
		{
			java.net.URL serverURL = new java.net.URL(CMURL);
			cmService = cmServiceLocator.getcontentManagerService(serverURL);
			
			CAM  cam  = new CAM();
			CAMPassport camPass = new CAMPassport();
			camPass.setId(camPassport);
			cam.setCAMPassport(camPass);
			
			BiBusHeader bibus = new BiBusHeader();
			
			CookieVar newBiBusCookieVars[] = new CookieVar[1];
			newBiBusCookieVars[0] = new CookieVar(); 
			newBiBusCookieVars[0].setName("cam_passport");
			newBiBusCookieVars[0].setValue(camPassport);
			HdrSession hdrSession = new HdrSession(); 
			hdrSession.setCookieVars(newBiBusCookieVars);
			bibus.setHdrSession(hdrSession);
			((Stub)cmService).setHeader("http://developer.cognos.com/schemas/bibus/3/", "biBusHeader", bibus);
			
			return cmService;
		}
		//handle uncaught exceptions
		catch (MalformedURLException e)
		{
			System.out.println("Malformed URL:\n" + e.getMessage());
			return null;
		}
		catch (ServiceException e)
		{
			System.out.println("Service Exception:\n" + e.getMessage());
			return null;
		}
	}
	
}
