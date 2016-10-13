package com.vnpt.tutorial.reportemployment;

import java.io.IOException;
import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import com.vnpt.dao.DepartmentDAO;
import com.vnpt.dao.ReportEmpDAO;
import com.vnpt.entity.Department;
import com.vnpt.entity.NodeTree;
import com.vnpt.entity.Personalreport;
import com.vnpt.entity.SearchForm;
import com.vnpt.entity.Servicereport;
import com.vnpt.entity.Staff;
import com.vnpt.entity.User;
import com.vnpt.entity.UserForm;
import com.vnpt.entity.Weeklyplan;
import com.vnpt.entity.Weekreport;
import com.vnpt.entity.Dailyreport;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.util.JSONWrappedObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class MyController {

	@Autowired
	private DepartmentDAO departmentDAO;
	
	@Autowired
	private ReportEmpDAO reportEmpDAO;

	@RequestMapping({ "/", "/home", "/index" })
	public String home(Model model) {
		return "loginPage"; 
	}
	
	private String username;
	
	@RequestMapping(value = {"/"}, method = RequestMethod.GET)
	public String welcomePage(Model model) {

		model.addAttribute("title", "Welcome");
		model.addAttribute("message", "This is welcome page! Welcome to VNPT Hung Yen!");

		return "loginPage";
	}
	
	@RequestMapping(value = {"/welcome"})
	    protected View welcome() {

	        Set<String> roles = AuthorityUtils
	                .authorityListToSet(SecurityContextHolder.getContext()
	                        .getAuthentication().getAuthorities());
	        if (roles.contains("ROLE_LEADER")) {
	        	
	            return new RedirectView("weeklyplan");
	        }
	        if (roles.contains("ROLE_DD_ADMIN")) {
	            return new RedirectView("dailyreport");
	        }
	        if (roles.contains("ROLE_PD_ADMIN")) {
	            return new RedirectView("provinceDirector");
	        }
	        return new RedirectView("dailyreport");
	    }

	@InitBinder
	public void myInitBinder(WebDataBinder dataBinder) {
		Object target = dataBinder.getTarget();
		if (target == null) {
			return;
		}
		System.out.println("Target=" + target);

		if (target.getClass() == Dailyreport.class) {

			// Register to handle the conversion between the multipart object
			// and byte array.
			dataBinder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
		}
	}
	
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String loginPage(Model model) {

		return "loginPage";
	}
	
	@RequestMapping(value = "/addUser", method = RequestMethod.GET)
	public String addUser(Model model) {	

		List<User> users = reportEmpDAO.listUsers("dbadmin1");

		ObjectMapper objectMapper = new ObjectMapper();

		try {
	
			NodeTree noteTree1 = new NodeTree(users.get(0).getUserid(),users.get(0).getUsername());
			
			String nodeTree = objectMapper.writeValueAsString(noteTree1);		
			JsonNode node3 = objectMapper.readValue(nodeTree, JsonNode.class);

			model.addAttribute("managerTree",node3.toString());
			
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//String managerTree = "[{\"id\":1,\"text\":\"Root node\",\"children\":[{\"id\":2,\"text\":\"Root node 2\", \"children\":[{\"id\":4,\"text\":\"Child node 4\"},{\"id\":5,\"text\":\"Child node 5\"}]},{\"id\":3,\"text\":\"Child node 2\"}]}]";

		return "adduserPage";
	}
	
	@RequestMapping(value = "/404", method = RequestMethod.GET)
	public String addUserError(Model model) {	

		return "404Page";
	}
	
	@RequestMapping(value = "/addSuccessful", method = RequestMethod.GET)
	public String addUserSuccessful(Model model) {	

		return "addSuccessfulPage";
	}
	
	@RequestMapping(value = "/addUser", method = RequestMethod.POST)
	public @ResponseBody String submitUser(@ModelAttribute("/userForm") UserForm userForm, Model model) {

		if(!userForm.getPassword().equals(userForm.getRepeatpass())) {
			
			return "403Page";
		}

		return "Add User Complete!";
	}
	
	@RequestMapping(value = "/logoutSuccessful", method = RequestMethod.GET)
	public String logoutSuccessfulPage(Model model) {

		model.addAttribute("title", "Logout");
		return "logoutSuccessfulPage";
	}
	
	@RequestMapping(value = "/403", method = RequestMethod.GET)
	public String accessDenied(Model model, Principal principal) {

		if (principal != null) {
			model.addAttribute("message",
					"Hi " + principal.getName() + "<br> You do not have permission to access this page!");
		} else {
			model.addAttribute("msg", "You do not have permission to access this page!");
		}
		return "403Page";
	}
		
	@RequestMapping(value = "/userInfo", method = RequestMethod.GET)
	public String userInfo(Model model, Principal principal) {

		// Sau khi user login thanh cong se co principal
		this.username = principal.getName();
		System.out.println("username:"+this.username);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		String date = dateFormat.format(cal.getTime());

		model.addAttribute("date", date);
		
		return "employeePage";
	}
	
	@RequestMapping(value = "/leader", method = RequestMethod.GET)
	public String leaderPage(Model model, Principal principal) {
		this.username = principal.getName();
		System.out.println("username:"+this.username);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		String date = dateFormat.format(cal.getTime());

		model.addAttribute("date", date);
		return "leaderPage";
	}

	@RequestMapping(value = "/districtDirector", method = RequestMethod.GET)
	public String districtDirectorPage(Model model,  Principal principal) {
		this.username = principal.getName();
		System.out.println("username:"+this.username);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		String date = dateFormat.format(cal.getTime());
		return "districtDirectorPage";
	}

	@RequestMapping(value = "/provinceDirector", method = RequestMethod.GET)
	public String adminPage(Model model, Principal principal) {
		this.username = principal.getName();
		System.out.println("username:"+this.username);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		String date = dateFormat.format(cal.getTime());
		return "provinceDirectorPage";
	}

	@RequestMapping({ "/deptList" })
	public String deptList(Model model) {

		List<Department> list = departmentDAO.listDepartment();
		model.addAttribute("departments", list);
		return "deptList";
	}

	@RequestMapping(value = "/savereport.do", method = RequestMethod.GET)
	public String showForm() {
		
		System.out.println("This is check!");
		return "savereport";
	}
	
	@RequestMapping(value = "/savereport.do", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody String addUser(@ModelAttribute(value = "dailyreport") Dailyreport dailyreport, BindingResult result) {
		
		String returnText ="savereport";
		
		System.out.println("CCC"+result.getFieldValue("date"));

		ObjectMapper mapper = new ObjectMapper();

		String jsonInString = "{\"name\":\"mkyong\",\"salary\":7500,\"skills\":[\"java\",\"python\"]}";
		Staff staff1;
		
		try {
			
			staff1 = mapper.readValue(jsonInString, Staff.class);
			
			System.out.println(staff1);
			System.out.println(jsonInString);
			
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return jsonInString;
	}

	// GET: Show upload form page.
	@RequestMapping(value = "/dailyreport", method = RequestMethod.GET)
	public String showReportFormHandler(Model model,Principal principal) {

		this.username = principal.getName();
		System.out.println("username:"+this.username);

		Dailyreport dailyreport = new Dailyreport();

//		List<Dailyreport> list = reportEmpDAO.listReport();
		List<Dailyreport> list = reportEmpDAO.listReport(this.username);
		if (list.size() > 0) {
			dailyreport = list.get(list.size()-1);
			
			System.out.println(dailyreport.getStatus());
			
			if(dailyreport.getStatus().equals("DONE")) {
				
				dailyreport = new Dailyreport();
				return "redirect:weeklyplan";
//				System.out.println(dailyreport.getDate());
			} else if(dailyreport.getStatus().equals("DRAF")) {
				try {
					String date = dailyreport.getDate();
					date = convertDateFormat(date);
					dailyreport.setDate(date);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
				}
				System.out.println("DRAF");
			}
		}

		dailyreport.setUsername(this.username);

		model.addAttribute("dailyreport", dailyreport);

		return "dailyreport";
	}
	
	private String convertDateFormat(String date) throws ParseException {

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date tDate = df.parse(date);

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		String convertDate = formatter.format(tDate);
		
		return convertDate;
	}
	
	@RequestMapping(value = "/weekreport", method = RequestMethod.GET)
	public String searchReportFormHandler(Model model, Principal principal) {
		this.username = principal.getName();
		
		Dailyreport dailyreport = new Dailyreport();
		List<Dailyreport> list = reportEmpDAO.listReport(this.username);

		Dailyreport weekreport = new Dailyreport();
		
		for(int i =0; i< list.size();i++) {
			weekreport.setCardCut(list.get(i).getCardCut());
		}
		
		if (list.size() > 0) {
			dailyreport = list.get(list.size()-1);
			if(dailyreport.getStatus().equals("DONE")) {
				dailyreport = new Dailyreport();
			} else if(dailyreport.getStatus().equals("DRAF")) {
			}
		}

		SearchForm searchForm = new SearchForm();
		
//		List<String> users = new ArrayList<String>();
		
//		users.add("user01");
//		users.add("dbhuy");
//		users.add("dbhuong");

		List<User> users = reportEmpDAO.listUsers(this.username);
		
		List<String> strUsers = new ArrayList<String>();
		
		for(int i =0;i < users.size();i++){
			
			strUsers.add(users.get(i).getUsername());
		}
		
		searchForm.setListOfUsers(strUsers);
		
		dailyreport.setUsername(this.username);
		model.addAttribute("dailyreport", dailyreport);
		model.addAttribute("searchForm", searchForm);
		
		return "weekreport";
	}
	
	//Leaderreport
	@RequestMapping(value = "/leaderreport", method = RequestMethod.GET)
	public String leaederReportFormHandler(Model model, Principal principal) {
		SearchForm searchForm = new SearchForm();
		this.username = principal.getName();
		searchForm.setUsername(this.username);
		
		List<User> listUser = reportEmpDAO.listUsers(this.username);
		
		for(int i =0;i < listUser.size();i++) {
//			List<Dailyreport> list = reportEmpDAO.listWeekReport(this.username, searchForm.getFromDate(), searchForm.getToDate());

		}

		List<String> strUsers = new ArrayList<String>();
		List<User> users = reportEmpDAO.listUsers(this.username);

		for(int i =0;i < users.size();i++) {
			
			strUsers.add(users.get(i).getUsername());
		}

		searchForm.setListOfUsers(strUsers);
		
		model.addAttribute("searchForm", searchForm);	
		
		return "leaderreport";
		
	}
	
	@RequestMapping(value = "/leaderreport", method = RequestMethod.POST)
	public String leaderReportHandlerPOST(HttpServletRequest request, //
			Model model, //
			@ModelAttribute("/searchForm") SearchForm searchForm) {

		return this.doSearchLeaderReport(request, model, searchForm);
		
	}
	
	@RequestMapping(value = "/leaderreport", method = RequestMethod.POST,  params = {"expReport"})
	public ModelAndView getPersonalReportExcel(@ModelAttribute("/searchForm") SearchForm searchForm) {
//		System.out.println(searchForm.getToDate());
		List<Personalreport> personalreportlist = reportEmpDAO.listPersonalReport(this.username, searchForm.getFromDate(), searchForm.getToDate());
		return new ModelAndView("PersonalreportListExcel", "PersonalReportList", personalreportlist);
	}
	

	@RequestMapping(value = "/weeklyplan", method = RequestMethod.GET)
	public String createWeekPlanFormHandler(Model model, Principal principal) {
		
		Weeklyplan weeklyplan = new Weeklyplan();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		String monday = dateFormat.format(cal.getTime());
		cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		String saturday = dateFormat.format(cal.getTime());

		List<Weeklyplan> list = reportEmpDAO.listWeeklyPlan(this.username,monday, saturday);
		
		System.out.println("Size:"+list.size());
		
		if (list.size() > 0) {
			weeklyplan = list.get(list.size()-1);
		}

		weeklyplan.setUsername(this.username);
		model.addAttribute("list", list);
		model.addAttribute("weeklyplan", weeklyplan);
		model.addAttribute("monday",monday);
		model.addAttribute("saturday",saturday);
		
		return "weeklyplan";
	}
	
	@RequestMapping(value = "/weeklyplan", method = RequestMethod.POST,  params = { "savePlan" }, produces = "application/x-www-form-urlencoded;charset=UTF-8")
	public String saveWeekPlanFormHandlerPOST(HttpServletRequest request,
			Model model,	
			@ModelAttribute("Weeklyplan") Weeklyplan weeklyplan
			) {
		
		reportEmpDAO.createWeeklyPlan(weeklyplan);

		return this.doShowWeeklyplan(request, model, weeklyplan);
	}
	
	@RequestMapping(value = "/weeklyplan", method = RequestMethod.POST, params = { "deletePlan" })
	public String deleteWeekPlanFormHandlerPOST(HttpServletRequest request, Model model) {
		
		System.out.println("deletePlan");

		for (String weeklyplanid : request.getParameterValues("weeklyplanid")) {
			
			System.out.println(weeklyplanid);
			
			reportEmpDAO.deleteWeeklyplan(Integer.parseInt(weeklyplanid));
			
		}

		return "redirect:weeklyplan";
		
	}

	// POST: Do Upload
	// Get data from date to date
	// 
	@RequestMapping(value = "/weekreport", method = RequestMethod.POST)
	public String searchReportHandlerPOST(HttpServletRequest request, //
			Model model, //
			@ModelAttribute("/searchForm") SearchForm searchForm) {

		return this.doSearchReport(request, model, searchForm);
		
	}
	
	@RequestMapping(value = "/dailyreport", method = RequestMethod.POST,  params = { "saveReport" })
	public String saveReportHandlerPOST(HttpServletRequest request, //
			Model model, //
			@ModelAttribute("dailyreport") Dailyreport dailyreport) {
		
		System.out.println("saveReport");
		dailyreport.setStatus("DRAF");
		reportEmpDAO.createNewReport(dailyreport);
		return this.doShowSaveReport(request, model, dailyreport);

	}
	
	@RequestMapping(value = "/dailyreport", method = RequestMethod.POST,  params = { "editReport" })
	public String editReportHandlerPOST(HttpServletRequest request, //
			Model model, //
			@ModelAttribute("dailyreport") Dailyreport dailyreport) {
		
		System.out.println("editReport");
		dailyreport.setStatus("DONE");
		reportEmpDAO.updateReport(dailyreport);
		return this.doShowReport(request, model, dailyreport);

	}
	
	@RequestMapping(value = "/dailyreport", method = RequestMethod.POST,  params = { "sendReport" })
	public String checkReportHandlerPOST(HttpServletRequest request, //
			Model model, //
			@ModelAttribute("dailyreport") Dailyreport dailyreport) {
		
		System.out.println("sendReport");
		
		if(dailyreport.getStatus()!= null){
			dailyreport.setStatus("DONE");
			reportEmpDAO.updateReport(dailyreport);
			
		} else {
			dailyreport.setStatus("DONE");
			reportEmpDAO.createNewReport(dailyreport);
		}
		
		return this.doShowReport(request, model, dailyreport);

	}
	
	@RequestMapping(value = "/dailyreport", method = RequestMethod.POST,  params = {"expReport"})
	public String exportReportHandlerPOST(HttpServletRequest request, //
			Model model, //
			@ModelAttribute("dailyreport") Dailyreport dailyreport) {
		
		System.out.println("expReport in Dailyreport");
		
		return "redirect:dailyreport";

	}
	
		@RequestMapping(value = "/weekreport", method = RequestMethod.POST,  params = {"expReport"})
		public ModelAndView getExcel(@ModelAttribute("/searchForm") SearchForm searchForm) {
	//		System.out.println(searchForm.getToDate());
			List<Weekreport> list;
		if (searchForm.getListOfUsers() != null)
			list = reportEmpDAO.listWeekReport(searchForm.getListOfUsers().get(0), searchForm.getFromDate(),
					searchForm.getToDate());
		else
			list = reportEmpDAO.listWeekReport(this.username, searchForm.getFromDate(), searchForm.getToDate());

			// Alt+shift+R: replace variable name
			Weekreport weekreport = new Weekreport();
			List<Servicereport> listservicereport = new ArrayList<Servicereport>();
			if ((list.size() > 0) && list.get(list.size()-1) !=null) {
				weekreport = list.get(list.size()-1);
	
				Servicereport prepaidreport = new Servicereport(username,"Tra truoc", weekreport.getPrepaidDevelop(),weekreport.getPrepaidRestore(), weekreport.getPrepaidCut(), weekreport.getPrepaidStop(), weekreport.getPrepaidDevelop()+weekreport.getPrepaidRestore()- weekreport.getPrepaidCut()- weekreport.getPrepaidStop(), weekreport.getPrepaidUninstall(),weekreport.getPrepaidUnresponsive());
				Servicereport postpaidreport = new Servicereport(username,"Tra sau", weekreport.getPostpaidDevelop(),weekreport.getPostpaidRestore(), weekreport.getPostpaidCut(), weekreport.getPostpaidStop(), weekreport.getPostpaidDevelop()+weekreport.getPostpaidRestore()- weekreport.getPostpaidCut()- weekreport.getPostpaidStop(), weekreport.getPostpaidUninstall(),weekreport.getPostpaidUnresponsive());
				Servicereport fixedreport = new Servicereport(username,"Co Dinh", weekreport.getFixedDevelop(), weekreport.getFixedRestore(),weekreport.getFixedCut(), weekreport.getFixedStop(), weekreport.getFixedDevelop()+ weekreport.getFixedRestore()- weekreport.getFixedCut()- weekreport.getFixedStop(), weekreport.getFixedUninstall(),weekreport.getFixedUnresponsive());
				Servicereport gphonereport = new Servicereport(username,"Gphone", weekreport.getGphoneDevelop(),weekreport.getGphoneRestore(), weekreport.getGphoneCut(), weekreport.getGphoneStop(), weekreport.getGphoneDevelop()+weekreport.getGphoneRestore()- weekreport.getGphoneCut()-weekreport.getGphoneStop(), weekreport.getGphoneUninstall(),weekreport.getGphoneUnresponsive());
				Servicereport fiberreport = new Servicereport(username, "Fiber",  weekreport.getFiberDevelop(), weekreport.getFiberRestore(),weekreport.getFiberCut(), weekreport.getFiberStop(), weekreport.getFiberDevelop()+ weekreport.getFiberRestore()-weekreport.getFiberCut()- weekreport.getFiberStop(), weekreport.getFiberUninstall(),weekreport.getFiberUnresponsive());
				Servicereport megareport = new Servicereport(username, "Mega",  weekreport.getMegaDevelop(), weekreport.getMegaRestore(),weekreport.getMegaCut(), weekreport.getMegaStop(), weekreport.getMegaDevelop()+ weekreport.getMegaRestore()- weekreport.getMegaCut()- weekreport.getMegaStop(), weekreport.getMegaUninstall(),weekreport.getMegaUnresponsive());
				Servicereport mytvreport = new Servicereport(username, "Mytv",  weekreport.getMytvDevelop(), weekreport.getMytvRestore(),weekreport.getMytvCut(), weekreport.getMytvStop(), weekreport.getMytvDevelop()+ weekreport.getMytvRestore()- weekreport.getMytvCut()- weekreport.getMytvStop(), weekreport.getMytvUninstall(),weekreport.getMytvUnresponsive());
				Servicereport ivanreport = new Servicereport(username, "Ivan",  weekreport.getIvanDevelop(), weekreport.getIvanRestore(), weekreport.getIvanCut(), weekreport.getIvanStop(), weekreport.getIvanDevelop()+ weekreport.getIvanRestore()- weekreport.getIvanCut()- weekreport.getIvanStop(), weekreport.getIvanUninstall(),weekreport.getIvanUnresponsive());
				Servicereport vnedureport = new Servicereport(username,"Vnedu",  weekreport.getVneduDevelop(), weekreport.getVneduRestore(),weekreport.getVneduCut(), weekreport.getVneduStop(), weekreport.getVneduDevelop()+ weekreport.getVneduRestore()- weekreport.getVneduCut()- weekreport.getVneduStop(), weekreport.getVneduUninstall(),weekreport.getVneduUnresponsive());
				Servicereport cardreport = new Servicereport(username,"Card", weekreport.getCardDevelop(), weekreport.getCardRestore(),weekreport.getCardCut(), weekreport.getCardStop(), weekreport.getCardDevelop()+ weekreport.getCardRestore()- weekreport.getCardCut()- weekreport.getCardStop(), weekreport.getCardUninstall(),weekreport.getCardUnresponsive());
				Servicereport chargereport = new Servicereport(username, "Charge",  weekreport.getChargeDevelop(), weekreport.getChargeRestore(),weekreport.getChargeCut(), weekreport.getChargeStop(),  weekreport.getChargeDevelop()+ weekreport.getChargeRestore()-  weekreport.getChargeCut()- weekreport.getChargeStop(), weekreport.getChargeUninstall(),weekreport.getChargeUnresponsive());
				
				listservicereport.add(prepaidreport);
				listservicereport.add(postpaidreport);
				listservicereport.add(fixedreport);
				listservicereport.add(gphonereport);
				listservicereport.add(fiberreport);
				listservicereport.add(megareport);
				listservicereport.add(mytvreport);
				listservicereport.add(ivanreport);
				listservicereport.add(vnedureport);
				listservicereport.add(cardreport);
				listservicereport.add(chargereport);
				
			}
			
	//		System.out.println(listservicereport.size());
	
			return new ModelAndView("WeekreportListExcel", "WeekReportList", listservicereport);
		}
	
	public String exportReportHandlerPOST(HttpServletRequest request, //
			Model model, //
			@ModelAttribute("searchForm") SearchForm searchForm) {
		
		System.out.println("expReport in Weekreport");
		
		return "redirect:weekreport";
	}

	private String doShowWeeklyplan(HttpServletRequest request,Model model,Weeklyplan weeklyplan){
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		String monday = dateFormat.format(cal.getTime());
		cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		String saturday = dateFormat.format(cal.getTime());
		
		List<Weeklyplan> list = reportEmpDAO.listWeeklyPlan(this.username,monday, saturday);
		model.addAttribute("list", list);
		model.addAttribute("weeklyplan", weeklyplan);
		model.addAttribute("monday",monday);
		model.addAttribute("saturday",saturday);
		return "weeklyplan";
	}
	
	private String doShowSaveReport(HttpServletRequest request, Model model, //
			Dailyreport dailyreport) {

		// List<Dailyreport> list = reportEmpDAO.listReport();
		model.addAttribute("dailyreport", dailyreport);
		return "dailyreport";
	}
	
	private String doShowReport(HttpServletRequest request, Model model, //
			Dailyreport dailyreport) {

		// List<Dailyreport> list = reportEmpDAO.listReport();
		model.addAttribute("dailyreport", dailyreport);
		return "showReportResult";
	}
	
	private String doSearchReport(HttpServletRequest request, Model model, //
			SearchForm searchForm) {
		
		Weekreport  weekreport= new Weekreport();
		List<Weekreport> list;
		if (searchForm.getListOfUsers() == null) {

			list = reportEmpDAO.listWeekReport(this.username, searchForm.getFromDate(),searchForm.getToDate());

		} else {
			list = reportEmpDAO.listWeekReport(searchForm.getListOfUsers().get(0),
					searchForm.getFromDate(), searchForm.getToDate());
		}
		if (list.size() > 0) {
			weekreport = list.get(list.size()-1);
		}
				
		List<User> users = reportEmpDAO.listUsers(this.username);
		
		List<String> strUsers = new ArrayList<String>();
		
		for(int i =0;i < users.size();i++){
			
			strUsers.add(users.get(i).getUsername());
		}
		
		searchForm.setListOfUsers(strUsers);
		
		model.addAttribute("dailyreport", weekreport);
		model.addAttribute("searchForm", searchForm);
		
		return "weekreport";
		
	}
	
	//LeaderReport
	private String doSearchLeaderReport(HttpServletRequest request, Model model, //
			SearchForm searchForm) {

		List<Personalreport> list = reportEmpDAO.listPersonalReport(this.username, searchForm.getFromDate(), searchForm.getToDate());

		System.out.println(list.size());
			
		for(int i =0;i <list.size()-1; i++){
			
			System.out.println(list.get(i).getUsername());
			System.out.println(list.get(i).getPrepaidDevelop());
			System.out.println(list.get(i).getPostpaidDevelop());
		}
		
		model.addAttribute("list", list);
		model.addAttribute("searchForm", searchForm);
		
		return "leaderreport";
		
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}