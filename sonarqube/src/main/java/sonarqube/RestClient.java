package sonarqube;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class RestClient {
	
	private static final Logger LOG = LoggerFactory.getLogger(RestClient.class);
	
	public Map<String, Object> restCall(String sUrl, String sComponentRoots, String sSeverities, int iPage) {
		URL url = null;
		HttpURLConnection conn = null;
		Map<String, Object> responseBean = null;
		try {
			if (sUrl == null || sComponentRoots == null || sSeverities == null||  iPage <= 0) {
				throw new RuntimeException("Failed : Url/ComponentRoots/Severities/TotalPage not found");
			}
			LOG.info("sonarqube server call....");
			url = new URL(sUrl + "?componentRoots=" + sComponentRoots + "&severities=" + sSeverities + "&pageIndex=" + iPage);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			
			String output = br.readLine();
			
			if(output != null) {
				responseBean = new Gson().fromJson(output, Map.class);
			}
			LOG.info("sonarqube server call complete....");
		} catch (MalformedURLException e) {
			LOG.error(e.getMessage(), e);
		} catch (IOException e1) {
			LOG.error(e1.getMessage(), e1);
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return responseBean;
	}
	
	public void process(String sUrl, String sComponentRoots, String sSeverities) {
		
		Map<String, Object> getPageMap = restCall(sUrl, sComponentRoots,sSeverities, 1);
		if (getPageMap == null) {
			throw new RuntimeException("Error001 : response error");
		}

		Double iTotal = (Double) getPageMap.get("total");
		Double iPs = (Double) getPageMap.get("ps");
		int iLoop = (int) (Math.ceil(iTotal / iPs) + 1);
		
	    String sProjectName = sComponentRoots.replaceAll(":", "-");
	    String folderName = System.getProperty("user.dir") + "/output/" + Util.getDate("yyyyMMdd");
	    
	    File mkFolder = new File(folderName);
	    
	    if(!mkFolder.exists()){
	    	mkFolder.mkdirs(); 
	    } 
	    
	    /**
	     else{
   			File[] destroy = mkFolder.listFiles(); 
   			for(File des : destroy){
    			des.delete(); 
   			}	
   		}*/
	        
		File file = new File(folderName + "/sonarqube_result_" + sProjectName + "_" + Util.getDate("yyyyMMddhhmmss") + ".xlsx");
		XSSFExcelWriter writer = new XSSFExcelWriter();
		writer.createSheet(sProjectName);
		writer.setColumn(0, "severity", "severity", 5000);
		writer.setColumn(1, "type", "type", 5000);
		writer.setColumn(2, "rule", "rule", 8000);
		writer.setColumn(3, "status", "status", 3000);
		writer.setColumn(4, "resolution", "resolution", 3000);
		writer.setColumn(5, "createDate", "createDate", 3000);
		writer.setColumn(6, "updateDate", "updateDate", 3000);
		writer.setColumn(7, "component", "component", 10000);
		writer.setColumn(8, "line", "line", 1000);
		writer.setColumn(9, "message", "message", 10000);
		writer.setHeaderToFirstRow();
		LOG.info("excel write start...");
		for(int i = 1 ; i < iLoop ; i++ ) {
			Map<String, Object> responseMap = restCall(sUrl, sComponentRoots, sSeverities, i);
			if (responseMap == null) {
				throw new RuntimeException("Error00" + i + " : response error");
			}
			List<Map<String, Object>> issueBeanList = (List<Map<String, Object>>) responseMap.get("issues");
			if (issueBeanList != null) {
				writer.setData(issueBeanList);
			}
		}
		
		writer.setColumnsWidth();
		writer.write(file);
		LOG.info("excel write end...");
	}

	public static void main(String[] args) {
		if(args.length != 3) {
			throw new RuntimeException("Error000 : parameter not found");
		}		
		
		RestClient rc = new RestClient();
		rc.process(args[0],args[1],args[2]);

	}
}
