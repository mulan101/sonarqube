package global.com.samsung.common.servlet.user;

import global.com.samsung.common.constants.GlobalConstants;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.auth.core.spi.AuthenticationInfo;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.security.AccountManagerFactory;
import com.day.crx.security.token.TokenUtil;

/**
* * @Copyright Samsung SDS Inc.
* 
* Class Name : LoginSsoServlet.java
* Description : SSO 로그인 후 호출되어 aem로그인 처리를 해준다.
* 
* Modification Information
*
* 일자 작성자 내용
* --------- --------- --------------------
* 2016. 4. 18. ws.kim 신규작성
*
* 
* @author ws.kim
* @since 2016. 4. 18.
* @version 1.0
* @serial 
* @see 
*/
@SlingServlet(paths = {"/bin/user/login_sso"}, methods = "GET")
public class LoginSsoServlet extends SlingSafeMethodsServlet {
private static final Logger log = LoggerFactory.getLogger(LoginSsoServlet.class);	
private static final long serialVersionUID = 1L;

@Reference
SlingRepository slingRepository;	

@Reference
private ResourceResolverFactory resolverFactory;

@Reference 
AccountManagerFactory accountManagerFactory;


/**
* Implementation for get request
* 
* WMC SSO 로그인 후 userId 정보를 받아 user인증과 
* 토큰 생성 및 쿠키설정 후 로그인.
* 
*/
@Override
protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response ) throws ServletException, IOException {	
Session session= null;
String convertUserId = "";
String wmcDecUserId = "";
String referrer = request.getHeader("REFERER"); 
//Temporary use
String userId = request.getParameter("s_user_id");

//log.debug("referrer:" + referrer);

String returnUrl = null;
try{
AuthenticationInfo authenticationInfo = null;
session = slingRepository.loginService(null, slingRepository.getDefaultWorkspace() );
//session = AccessUserManager.getInstance().getLoginService(slingRepository);	
log.debug("session:" + session);

if (!userId.isEmpty()) {	
//AccountManager accountManager = accountManagerFactory.createAccountManager(session);	
//user 정보를 가져와 유효한 user의 경우 로그인 프로세스를 적용.
//User user = accountManager.findAccount(userId);	

String wmcEncUserInfo = userId;
if ( "".equals(wmcEncUserInfo) || wmcEncUserInfo == null) {
response.sendRedirect("/projects.html"); 
}

int wmcSecuiKey = GlobalConstants.WMC_NEWSECUI_KEY;
AnyEncKey aeKey = new AnyEncKey();
convertUserId = aeKey.Comm_DecStr(wmcEncUserInfo, wmcSecuiKey);

if (convertUserId == null) {
response.sendRedirect("/projects.html"); 
}

log.debug("convertUserId:" + convertUserId);
if(convertUserId.length() >14){
wmcDecUserId = convertUserId.substring(14, convertUserId.length());	
}else{
response.sendRedirect("/projects.html"); 
}
log.debug("wmcDecUserId:" + wmcDecUserId);

UserManager userManager = ((JackrabbitSession) session).getUserManager();	
Authorizable authorizable = userManager.getAuthorizable(wmcDecUserId);	
User user = (User) authorizable;	

if(user != null){
//Token Generation and cookie applies
authenticationInfo = TokenUtil.createCredentials(request, response, slingRepository, wmcDecUserId, true);	
log.debug("authenticationInfo:" + authenticationInfo);
if(!authenticationInfo.isEmpty()){
if(4502 == request.getServerPort()){	
String strUrl =strUrl(request, wmcDecUserId);
log.debug("strUrl == " + strUrl);
response.sendRedirect("/apps/samsungp5/login/content/redirect.html?strUrl=" + strUrl);
} 
} else{
response.sendRedirect("/projects.html");
}
} else {	
response.sendRedirect("/projects.html");
}	
} else {
response.sendRedirect("/projects.html"); 
}
} catch (LoginException e) { 
e.toString();
} catch (RepositoryException e) { 
e.toString();	
} catch (Exception e){
e.toString();
} finally {
if (session != null) { 
session.logout(); 
}
}	
}	


/**
* Clears a page from the dispatcher cache strUrl
* 
* {QA서버 로그인 시켜주기 위한 url과 계정을 추가한다.}
* 
* @param request
* @param username
* @return
*/
private String strUrl(SlingHttpServletRequest request, String username) {
String strUrl = null;
int wmcSecuiKey = GlobalConstants.WMC_NEWSECUI_KEY;
long time = System.currentTimeMillis(); 
SimpleDateFormat dayTime = new SimpleDateFormat("yyyymmddhhmmss"); // Noncompliant;
String strTime = dayTime.format(new Date(time));

AnyEncKey aeKey = new AnyEncKey();
String userId = aeKey.Comm_EncStr(strTime + username, wmcSecuiKey);

if(4502 == request.getServerPort()){

String strCurrentUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/bin/user/login_sso";	

strCurrentUrl = strCurrentUrl.replace("4502", "4503");
strUrl = strCurrentUrl + "?s_user_id=" + userId;	
}	
return strUrl;
}
}