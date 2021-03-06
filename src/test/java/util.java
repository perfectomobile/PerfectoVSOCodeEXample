package test.java;


import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.RemoteExecuteMethod;
import org.openqa.selenium.remote.RemoteWebDriver;


public class util {

	//public static String REPORT_LIB = "../../TestResults/";
	public static String REPORT_LIB = ".."+File.separator;

	public static String SCREENSHOTS_LIB = "/Users/uzie/Documents/PMRepos/reports/";


	public static void closeTest(RemoteWebDriver driver)
	{
		System.out.println("CloseTest");
		driver.quit();
	}


	public static AppiumDriver getAppiumDriver(device device,String app,String platform,String Cloud,String user,String password,String appLocationToInstall)   {

		AppiumDriver webdriver= null;


		DesiredCapabilities capabilities = new DesiredCapabilities("", "", Platform.ANY);

		if (platform.equalsIgnoreCase("ios"))
		{
			capabilities.setCapability("bundleId", app);
		//	capabilities.setCapability("automationName", "appium");
			if (app.equals("healthclinic.client.patients"))
			{
				// instrument and work with Perfecto
				capabilities.setCapability("automationName", "PerfectoMobile");
				capabilities.setCapability("autoInstrument", true);
			}


		}else
		{
			capabilities.setCapability("app-activity",app);
			capabilities.setCapability("appPackage",app);

		}

		if (appLocationToInstall!= null)
		{
		//	capabilities.setCapability("app",appLocationToInstall );

		}
		capabilities.setCapability("user", user);
		capabilities.setCapability("password", password);
		if (device._id != null)
		{
			capabilities.setCapability("deviceName",  device._id);
		}
		if (device._os!= null)
		{
			//Android or IOS
			capabilities.setCapability("platformName",  device._os);
		}
		if (device._osVersion!= null)
		{
			capabilities.setCapability("platformVersion",  device._osVersion);
		}
		try {
			webdriver = new AndroidDriver(new URL(Cloud+"/nexperience/perfectomobile/wd/hub") , capabilities);
		} catch (Exception e) {
			String ErrToRep = e.getMessage().substring(0,e.getMessage().indexOf("Command duration")-1);
			System.out.println(ErrToRep);
			return (null);


		}

		return webdriver;

	}

	public static String getScreenShot(RemoteWebDriver driver,String name,String deviceID )
	{
		String screenShotName = SCREENSHOTS_LIB+name+"_"+deviceID+".png";
		driver   = (RemoteWebDriver) new Augmenter().augment( driver );
		File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);

		try {
			FileUtils.copyFile(scrFile, new File(screenShotName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return screenShotName;
	}


	public static void stoptApp(String appName,RemoteWebDriver d )
	{
		Map<String,String> params = new HashMap<String,String>();
		params.put("name", appName);
		d.executeScript("mobile:application:close", params);
	}


	public static void switchToContext(RemoteWebDriver driver, String context) {
		RemoteExecuteMethod executeMethod = new RemoteExecuteMethod(driver);
		Map<String,String> params = new HashMap<String,String>();
		params.put("name", context);
		executeMethod.execute(DriverCommand.SWITCH_TO_CONTEXT, params);
	}
	public static void swipe(String start,String end,RemoteWebDriver d )
	{
		Map<String,String> params = new HashMap<String,String>();
		params.put("start", start);  //50%,50%
		params.put("end", end);  //50%,50%

		d.executeScript("mobile:touch:swipe", params);

	}


	public static void downloadReport(RemoteWebDriver driver, String type, String fileName) throws IOException {
		try { 
			String command = "mobile:report:download"; 
			Map<String, Object> params = new HashMap(); 
			params.put("type", "html"); 
			String report = (String)driver.executeScript(command, params); 
			File reportFile = new File(getVSOReportLib(fileName) ); 
			BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(reportFile)); 
			byte[] reportBytes = OutputType.BYTES.convertFromBase64Png(report); 
			output.write(reportBytes); output.close(); 
		} catch (Exception ex) { 
			System.out.println("Got exception " + ex); }
	}
	public static List <PerfectoTestParams> getVSOExecParam()
	{
		return getVSOExecParam(null);
	}

	public static List <PerfectoTestParams> getVSOExecParam(String file)
	{

		List<PerfectoTestParams> params = new ArrayList();
		List<String> devices = new ArrayList();
		System.out.println("EXECUET TEST BUILD THE LIST FROM THE FILE v1");
		BufferedReader br;
		try {

			File f;
			if (file==null)
			{
				String current = new java.io.File( "." ).getCanonicalPath();
				System.out.println("Current dir:"+current);
				// on OS the file will be on folder app on Win two so i check if file exist 
				f = new File(".."+File.separator +"PerfectoConfigExe.json");
				if(!f.exists() ) { 
					// windoew
					f = new File(".."+File.separator +".."+File.separator +"PerfectoConfigExe.json");
				}
			}else

			{
				f = new File(file);
			}

			FileReader r = new FileReader(f);
			br = new BufferedReader(r);
			String line = null;  
			String platform = "Android"; // Android or IOS
			String PerfectoRepKeyForAll = "";
			String bandleID = "";
			String appType = "";

			//
			try {
				JSONParser parser = new JSONParser();

				Object obj = parser.parse(r);

				JSONObject jsonObject = (JSONObject) obj;

				String source = (String) jsonObject.get("Source");
				appType = (String) jsonObject.get("Application type");
				if (appType.toLowerCase().equals("native"))
				{
					PerfectoRepKeyForAll = (String) jsonObject.get("Perfecto Repository");
					bandleID = (String) jsonObject.get("BundleID");


					if (PerfectoRepKeyForAll.toLowerCase().contains(".apk"))
					{
						platform = "Android";
					}
					else
					{
						platform = "ios";
					}
				}
				
				System.out.println("devices:");
				JSONArray devicesList = (JSONArray) jsonObject.get("devices");
				Iterator<JSONObject> iterator = devicesList.iterator();
				while (iterator.hasNext()) {

					JSONObject dev = (JSONObject) iterator.next().get("device");
					System.out.println("os"+dev.get("os"));
					device d = new device((String)dev.get("deviceID"),(String)dev.get("os"),(String)dev.get("osVersion"));
					PerfectoTestParams p = new PerfectoTestParams(d, PerfectoRepKeyForAll, platform,bandleID);
					params.add(p);

				}

			} catch (Exception e) {
				e.printStackTrace();
			}





		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  

		return params;

	}

	public static String getVSOReportLib(String repID) {

		try {
			String current = new java.io.File( "." ).getCanonicalPath();
			String repLib = current+File.separator+"Reports";
			System.out.println("Current dir:"+repLib);

			File dir = new File(repLib);
			if (!dir.exists())
			{
				dir.mkdir();
			}

			return repLib+File.separator+"rep_"+repID+".html";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return File.separator;
	}

	public static String getReprtName(String repID,boolean withPath) {
		if (withPath)
		{
			return REPORT_LIB+"/rep_"+repID+".html";
		}
		else
		{
			return  "/rep_"+repID+".html";
		}

	}
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}
}
