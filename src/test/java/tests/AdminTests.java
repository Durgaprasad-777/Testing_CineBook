package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.AdminBookingsPage;
import pages.AdminMoviesPage;
import pages.AdminShowsPage;
import pages.AnalyticsPage;
import utils.ExcelUtils;

import java.util.List;
import java.util.Map;

public class AdminTests extends BaseTest {

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

    @Test(groups = {"sanity", "regression", "admin", "FRD_2_9"},
            description = "FRD_2.9: Admin can open Manage Movies and see movie form/table")
    public void adminManageMoviesPageLoads() {
        loginAsAdmin();
        AdminMoviesPage page = new AdminMoviesPage(driver).open();
        Assert.assertTrue(page.isDisplayed(), "Manage Movies page should be displayed.");
        Assert.assertTrue(page.hasMovieFormFields(), "Manage Movies form fields should be displayed.");
        Assert.assertTrue(page.hasTableOrEmptyState(), "Movie table or empty state should be displayed.");
    }

    @Test(groups = {"regression", "admin", "FRD_2_9"},
            description = "FRD_2.9.4: Admin movie form should keep validation active for missing required fields")
    public void adminMovieFormRequiresMandatoryFields() {
        loginAsAdmin();
        AdminMoviesPage page = new AdminMoviesPage(driver).open();
        page.submitEmptyForm();
        Assert.assertTrue(page.formStillDisplayed(), "Invalid movie form should remain displayed.");
    }

    @Test(groups = {"sanity", "regression", "admin", "FRD_2_10"},
            description = "FRD_2.10: Admin can open Manage Shows and see show form/table")
    public void adminManageShowsPageLoads() {
        loginAsAdmin();
        AdminShowsPage page = new AdminShowsPage(driver).open();
        Assert.assertTrue(page.isDisplayed(), "Manage Shows page should be displayed.");
        Assert.assertTrue(page.hasShowFormFields(), "Manage Shows form fields should be displayed.");
        Assert.assertTrue(page.hasTableOrEmptyState(), "Show table or empty state should be displayed.");
    }

    @Test(groups = {"sanity", "regression", "admin", "FRD_2_11"},
            description = "FRD_2.11.1-2.11.3: Admin can view and filter booking records")
    public void adminBookingsPageLoadsAndTabsWork() {
        loginAsAdmin();
        AdminBookingsPage page = new AdminBookingsPage(driver).open();
        Assert.assertTrue(page.isDisplayed(), "Manage Bookings page should be displayed.");
        page.switchStatusTabs();
        Assert.assertTrue(page.hasTableOrEmptyRow(), "Admin bookings table or empty row should be displayed.");
    }

    @Test(groups = {"sanity", "regression", "admin", "FRD_2_11"},
            description = "FRD_2.11.4-2.11.6: Admin can open analytics dashboard with KPIs")
    public void adminAnalyticsDashboardLoads() {
        loginAsAdmin();
        AnalyticsPage page = new AnalyticsPage(driver).open();
        Assert.assertTrue(page.isDisplayed(), "Analytics dashboard should be displayed.");
        Assert.assertTrue(page.hasKpis(), "Analytics KPI cards should be displayed.");
        page.refresh();
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
