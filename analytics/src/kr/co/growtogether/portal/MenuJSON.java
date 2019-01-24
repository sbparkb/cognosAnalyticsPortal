package kr.co.growtogether.portal;


import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cognos.developer.schemas.bibus._3.ContentManagerService_PortType;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.co.growtogether.cognos.CognosControl;
import kr.co.growtogether.cognos.CognosObject;

/**
 * Servlet implementation class MenuJSON
 */
@WebServlet("/MenuJSON")
public class MenuJSON extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MenuJSON() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String storeId = request.getParameter("storeId");
		
		ContentManagerService_PortType cmService = (ContentManagerService_PortType)request.getSession().getAttribute("cmService");
		
		CognosControl ctrl = new CognosControl();
		
		ArrayList<CognosObject> list = ctrl.getContentListSid(storeId, cmService);
		
		/**
		 * Jackson Binder JSON 전송
		 */
		ObjectMapper mapper = new ObjectMapper();
		
		// string 으로 저장
		String jsonString = mapper.writeValueAsString(list);
		
		response.setContentType("text/plain;charset=UTF-8");
		
		response.getWriter().println(jsonString);
	}

}
