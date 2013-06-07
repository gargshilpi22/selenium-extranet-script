
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.thoughtworks.selenium.SeleniumException;

public class OneRateUpdateNoID {

	@Test(dataProvider = "properties", invocationCount = 1, threadPoolSize = 1)
	public void testBizops(int startIndex, int endIndex) throws Exception {
		// Creating an instance of Firefox Driver and passing in the base url
		// for
		// the tests
		WebDriver ffdriver = new FirefoxDriver();
		WebDriverBackedSelenium driver = new WebDriverBackedSelenium(ffdriver,
				"");
		//"http://sgarg.duncllc.com:8585/"
		// Implicit timeout (in ms) being set for everything that goes through
		// the firefox driver
		driver.setTimeout("60000");

		// This arrayList keeps a listing of all the properties have not been
		// updated
		ArrayList<Integer> missingProperties = new ArrayList<Integer>();
		// A hashMap collection to use the propertyId as the key and other info
		// from database as values
		HashMap<String, List<String>> tsvObject = new HashMap<String, List<String>>();
		// inputStream and bufferedReader to read the database file into memory
		FileInputStream fstream = new FileInputStream("");
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		BufferedWriter bw = new BufferedWriter(new FileWriter("output.txt"));
		// Read each row from the database
		String strLine = br.readLine();
		// First row contains column names -> being used as keys within the
		// hashMap
		String[] keys = strLine.split("\t");
		// Create an arrayList of Strings for each key in hashMap
		for (String key : keys) {
			tsvObject.put(key, new ArrayList<String>());
		}
		// Initialize temporary array with null values
		String[] values = null;
		// Read each row separated by tabs and store them in the hashMap. Loop
		// terminated when
		// all keys, value pairs have been created
		while ((strLine = br.readLine()) != null) {
			values = strLine.split("\t");
			for (int i = 0; i < keys.length; i++) {
				tsvObject.get(keys[i]).add(values[i]);
			}
		}
		// find number of key, value pairs
		int numEntries = tsvObject.get(keys[0]).size();
		
		// Module 1: open the homepage and login
		driver.open("");
//		driver.open("http://sgarg.duncllc.com:8585/");
//		driver.type("id=field-user", "rkappera1");
//		driver.type("id=field-pass", "Password1");
//		driver.click("id=field-signin");

		DateTimeFormatter fmt = DateTimeFormat.forPattern("MM_d_yyyy-h_m_sa");

		// Loop to iterate through all entries of key, value pairs
		for (int i = startIndex; i < endIndex; i++) {
			
			try {			
				System.out.println("index: " + i);
//				driver.open("http://localhost:8585/App/ViewAdminHome");
				
				driver.waitForPageToLoad("60000");
				driver.type("id=field-user", "");
				driver.type("id=field-pass", "");
				driver.click("id=field-signin");
	
				driver.waitForPageToLoad("60000");

				// Module 2: Retrieve host, chain and property Id for each row
				// from
				// database
				// and populate those fields in the XXX page
				driver.select("name=host", tsvObject.get("HOST").get(i));
				driver.type("id=chain", tsvObject.get("CHAIN_CODE").get(i));
				driver.type("name=id", tsvObject.get("PROPERTY_ID").get(i));
				System.out.println("PropertyNum: " + (i + 2) + " PropertyID: "
						+ tsvObject.get("PROPERTY_ID").get(i));
				bw.write("PropertyNum: " + (i + 2) + " PropertyID: "
						+ tsvObject.get("PROPERTY_ID").get(i));
				driver.click("//input[@value='propertyProfile']");
				driver.click("//input[@value='submit']");

				driver.waitForPageToLoad("60000");

				// Module 3: Select the property that displays
				try {
					driver.click("id=select_prop");
				} catch (SeleniumException e) {
					missingProperties.add(i);
					System.out.println("Property didn't Exist");
					bw.write("Property didn't exist");
					takeScreenShot(fmt.print(DateTime.now()) + "-Row_" + i
							+ ".png", ffdriver);
					continue;
				}
				driver.waitForPageToLoad("60000");

				// click on inventory
				driver.click("//img[@src='/images/inventory_off.gif']");
				driver.waitForPageToLoad("60000");

				String rateLink = tsvObject.get("RateLink").get(i);

				if (rateLink.equals("Land")) {
					try {
						driver.isElementPresent("link=Land");
					} catch (SeleniumException e) {
						missingProperties.add(i);
						System.out.println("No Land Link Present");
						bw.write("No Land Link Present");
						takeScreenShot(fmt.print(DateTime.now()) + "-Row_" + i
								+ ".png", ffdriver);
						driver.open("https://extranet.orbitz.com/");
						continue;
					}
					driver.click("link=Land");
				} else if (rateLink.equals("Package")) {
					try {
						driver.isElementPresent("link=Package");
					} catch (SeleniumException e) {
						missingProperties.add(i);
						System.out.println("No package Link");
						bw.write("No Package link");
						takeScreenShot(fmt.print(DateTime.now()) + "-Row_" + i
								+ ".png", ffdriver);
						driver.open("https://extranet.orbitz.com/");
						continue;
					}
					driver.click("link=Package");
				} else {
					try {
						driver.isElementPresent("link=Both");
					} catch (SeleniumException e) {
						missingProperties.add(i);
						System.out.println("No Both Link Present");
						bw.write("No Both Link Present");
						takeScreenShot(fmt.print(DateTime.now()) + "-Row_" + i
								+ ".png", ffdriver);
						driver.open("https://extranet.orbitz.com/");
						continue;
					}
					driver.click("link=Both");
				}

//				driver.addCustomRequestHeader("enableRetail", "true");
				driver.waitForPageToLoad("60000");
				
				// find open rate code field and add new PRAC to it
				for (int rateCodeNum = 1; rateCodeNum < 16; rateCodeNum++) {
					String rateCodeXPathName = "name=rateCode" + rateCodeNum;
					String newRac = tsvObject.get("New_RAC").get(i);
					// if open field
					if (driver.getValue(rateCodeXPathName).equals("") || driver.getValue(rateCodeXPathName).equals(newRac)) {
						driver.type(rateCodeXPathName, tsvObject.get("New_RAC")
								.get(i));
						break;
					}
				}

				// updates cancellation policy if found 
				// or blank space
				String rateCodeName = tsvObject.get("NEW_CXL").get(i);
				if (!rateCodeName.trim().equals("")) {
					for (int rateCodeField = 1; rateCodeField < 33; rateCodeField++) {
						String ratePlanCode = "id=ratePlanCode" + rateCodeField;
						if (!driver.isElementPresent(ratePlanCode)) {
							driver.click("id=add_en_rtplan"
									+ (rateCodeField - 1));
							
							driver.waitForPageToLoad("60000");							
							
						}
						if (driver.getValue(ratePlanCode).equals("")
								|| driver.getValue(ratePlanCode).equals(
										rateCodeName)) {
							driver.type(ratePlanCode, rateCodeName);

							if (tsvObject.get("REFUND").get(i)
									.equals("NONREFUNDABLE")) {
								driver.check("id=nonRefundable" + rateCodeField);
								break;
							} else if ((tsvObject.get("REFUND").get(i)
									.equals("REFUNDABLE"))) {
								driver.check("id=isRefundable" + rateCodeField);
								System.out.println("name=cancelNumDays"
										+ rateCodeField + "_1_1");
								System.out.println(tsvObject.get("CXLBY").get(i));
								driver.select("name=cancelNumDays"
										+ rateCodeField + "_1_1",
										tsvObject.get("CXLBY").get(i));
								driver.select("name=chargeType" + rateCodeField
										+ "11", tsvObject.get("PENTYPE").get(i));
				
								System.out.println(tsvObject.get("PCTPEN").get(i));
								driver.select("name=percentageOption"
										+ rateCodeField + "11",
										tsvObject.get("PCTPEN").get(i));
								driver.type("id=numNights" + rateCodeField
										+ "11", tsvObject.get("NTSPEN").get(i));
								break; 
							}
						}
					}
				}


				int inputFieldIndex = 1;
				while (true) {
                    try {
                           String rateCodeFilter = "id=rateCodeFilter"
                                         + inputFieldIndex;
                           String additionalDesc = "id=additionalDesc"
                                         + inputFieldIndex;
                           String newPtext = tsvObject.get("New_Text").get(i);
                           // newPtext = newPtext.substring(1,
                           // newPtext.length()-1);
                           String newPfilter = tsvObject.get("New_RF").get(i);
                           try {
                                  driver.getText(rateCodeFilter);
                    
                                  if (driver.getValue(rateCodeFilter).equals("") || driver.getValue(rateCodeFilter).equals(newPfilter)) {
                                         driver.type(rateCodeFilter, newPfilter);
                                         driver.type(additionalDesc, newPtext);
                                         break;
                                  }
                                  inputFieldIndex++;
                                  
                           } catch (SeleniumException e) {
                                  // click plus button
                                  driver.click("id=add_rate_filter");
                                  driver.type(rateCodeFilter, newPfilter);
                                  driver.type(additionalDesc, newPtext);
                                  break;
                           }
                    } catch (NullPointerException e) {
                           break;
                    }
             }

				
				driver.type("id=rateDescriptionFilter", "");
				
				
				driver.click("//input[@src='/images/save_setting.gif']");

				driver.waitForPageToLoad("60000");
				
				driver.click("link=Sign out");
				
			}catch (Exception e) {
				driver.deleteAllVisibleCookies();
				driver.close();
				System.out.println("Hit transaction limit");
				driver.open("");
//				driver.open("http://sgarg.duncllc.com:8585/");
				driver.type("id=field-user", "");
				driver.type("id=field-pass", "");
				driver.click("id=field-signin");
				continue;
			}
		}	
		
		bw.close();
		driver.close();
	
	}	
	@DataProvider(name = "propertyStartIndex")
	public Object[][] properties() {
		return new Object[][] { {0,70} };  //, {1301,2598} };// {3656,4000},{4001,4858} };
	}

	private void takeScreenShot(String fileName, WebDriver ffdriver)
			throws IOException {
		System.out.println(fileName);
		fileName = fileName.trim();
		File screenshotFile = ((TakesScreenshot) ffdriver)
				.getScreenshotAs(OutputType.FILE);
		FileUtils.copyFile(screenshotFile, new File(fileName));
	}
}
