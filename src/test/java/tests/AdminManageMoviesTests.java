package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.AdminMoviesPage;
import utils.ExcelUtils;

import java.util.List;
import java.util.Map;

public class AdminManageMoviesTests extends BaseTest {

    @DataProvider(name = "AdminValidDetails")
    public Object[][] ValiddataDetails(){
        List<Map<String, String>> rows = ExcelUtils.readSheet("AdminValidDetails");
        return rows.stream().map(row -> new Object[]{row}).toArray(Object[][]::new);
    }

    @DataProvider(name = "AdminInValidDetails")
    public Object[][] InvaliddataDetails(){
        List<Map<String, String>> rows = ExcelUtils.readSheet("AdminInValidDetails");
        return rows.stream().map(row -> new Object[]{row}).toArray(Object[][]::new);
    }

    @Test(groups = {"smoke", "admin"}, dataProvider = "AdminValidDetails")
    public void enterValidMovieDetails(Map<String, String> data) throws InterruptedException {

        // Extract the strings from the map using your Excel sheet column headers
        String title      = data.get("title");
        String genre      = data.get("genre");
        String duration   = data.get("duration");
        String language   = data.get("language");
        String posterUrl  = data.get("posterUrl");
        String trailerUrl = data.get("trailerUrl");
        String price      = data.get("price");

        // The rest of your execution logic remains untouched
        loginAsAdmin();
        AdminMoviesPage page = new AdminMoviesPage(driver).open();
        int expectedCount = page.getMovieCount() + 1;

        page.fillDetails(title, genre, duration, language, posterUrl, trailerUrl, price);
        page.submitEmptyForm();
        Thread.sleep(5000);
        int actualCount = page.getMovieCount();

        Assert.assertEquals(actualCount, expectedCount);
    }

    @Test(groups = {"known-defect", "smoke", "admin"}, dataProvider = "AdminInValidDetails")
    public void enterInValidMovieDetails(Map<String, String> data) throws InterruptedException {

        // Extract the strings from the map using your Excel sheet column headers
        String title      = data.get("title");
        String genre      = data.get("genre");
        String duration   = data.get("duration");
        String language   = data.get("language");
        String posterUrl  = data.get("posterUrl");
        String trailerUrl = data.get("trailerUrl");
        String price      = data.get("price");

        loginAsAdmin();
        AdminMoviesPage page = new AdminMoviesPage(driver).open();

        page.fillDetails(title, genre, duration, language, posterUrl, trailerUrl, price);
        page.submitEmptyForm();

        // 1. Dynamic Explicit Wait: Replaces Thread.sleep(5000)
        // It waits up to 5 seconds but moves forward the instant the error appears.
        //WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        // Assuming page.getFormErrormsg() targets a specific locator, we wait for it to be visible.
        // Replace 'By.id("error-id")' with whatever locator your page class uses for the error message element.
        //wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("mm-form-error")));

        // 2. Capture the actual error text once it is visible
        Thread.sleep(4000);
        String actualerrormsg = page.getFormErrormsg();
        String expectederrormsg = "Enter valid form details";

        // 3. Asset the validation message matches perfectly
        Assert.assertEquals(actualerrormsg, expectederrormsg, "The form validation error message mismatch!");
    }

}
