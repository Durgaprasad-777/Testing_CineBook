param(
    [string]$TemplatePath = "C:\Users\2492244\Downloads\i-am-going-to-build-testing\i-am-going-to-build-testing\Test Summary Report - Agile.doc",
    [string]$OutputDocx = "C:\Users\2492244\Downloads\i-am-going-to-build-testing\i-am-going-to-build-testing\reports\CineBook_Test_Summary_Report_Agile_Gnaneshwar.docx",
    [string]$OutputDoc = "C:\Users\2492244\Downloads\i-am-going-to-build-testing\i-am-going-to-build-testing\reports\CineBook_Test_Summary_Report_Agile_Gnaneshwar.doc"
)

$ErrorActionPreference = "Stop"

function Set-CellText($Table, [int]$Row, [int]$Column, [string]$Text) {
    $range = $Table.Cell($Row, $Column).Range
    $range.End = $range.End - 1
    $range.Text = $Text
}

function Replace-Text($Document, [string]$FindText, [string]$ReplacementText) {
    $find = $Document.Content.Find
    $find.ClearFormatting()
    $find.Replacement.ClearFormatting()
    $find.Text = $FindText
    $find.Replacement.Text = $ReplacementText
    $find.Forward = $true
    $find.Wrap = 1
    $find.Format = $false
    $find.MatchCase = $false
    $find.MatchWholeWord = $false
    $find.MatchWildcards = $false
    $find.Execute(
        [ref]$FindText,
        [ref]$false,
        [ref]$false,
        [ref]$false,
        [ref]$false,
        [ref]$false,
        [ref]$true,
        [ref]1,
        [ref]$false,
        [ref]$ReplacementText,
        [ref]2
    ) | Out-Null
}

function Replace-ParagraphContaining($Document, [string]$Needle, [string]$ReplacementText) {
    foreach ($paragraph in $Document.Paragraphs) {
        $text = $paragraph.Range.Text
        if ($text -like "*$Needle*") {
            $range = $paragraph.Range
            $range.End = $range.End - 1
            $range.Text = $ReplacementText
        }
    }
}

$reportDate = "29-Jun-2026"
$project = "CineBook Movie Ticket Booking Platform"
$summary = "This Test Summary Report provides the completion status of the CineBook testing cycle, including functional coverage, execution results, defect status, release exit criteria, recommendations, and sign-off information for manager review."
$intro = "CineBook is a movie ticket booking platform covering moviegoer and theater-owner workflows such as login, registration, movie and theater browsing, booking, payment, my bookings, review/interest, and admin management. The QA team validated the release using FRD-based scenarios, RTM mapping, manual test evidence, Selenium WebDriver automation, TestNG execution logs, and defect reporting."

$word = $null
try {
    $word = New-Object -ComObject Word.Application
    $word.Visible = $false
    $document = $word.Documents.Open((Resolve-Path $TemplatePath).Path, $false, $false)

    Replace-Text $document "<Customer Name>" "CineBook"
    Replace-Text $document "<Project Name>" $project
    Replace-Text $document "<Version No.>" "V1.0"
    Replace-ParagraphContaining $document "The purpose of this document is to provide an insight" $summary
    Replace-ParagraphContaining $document "Provide an introduction which summarizes" $intro
    Replace-ParagraphContaining $document "List down the intended audience" "Intended audience: Maheshwaran, Product Owner, Development Team, QA Team, Business Analyst, and project stakeholders responsible for release review and approval."
    Replace-ParagraphContaining $document "List down the In-scope considered" "In scope: Login, Registration, Movie Browsing, Theater Browsing, Booking and Seat Selection, Payment, My Bookings and Cancellation, Review and Movie Interest, Admin Movie Management, Admin Show Management, Admin Booking Management, Admin Analytics, Navigation, Access Control, RTM validation, defect reporting, and Selenium TestNG regression evidence."
    Replace-ParagraphContaining $document "List down the Out of scope considered" "Out of scope: Production deployment validation, payment gateway live settlement, full performance/load testing, full security penetration testing, mobile native app testing, and third-party provider certification."
    Replace-ParagraphContaining $document "List down the Applications/Systems to be tested" "Application tested: CineBook web application. Systems not tested in this cycle: live payment settlement systems, production monitoring tools, and external notification integrations."
    Replace-ParagraphContaining $document "List down the details of the Integrations tested" "Integrations tested: Browser-based CineBook UI workflows, Stripe payment redirection behavior in test flow, role-based route access, and application API behavior visible through functional workflows. Full third-party integration certification was not in scope."
    Replace-ParagraphContaining $document "Include the applicable testing types as per the release need" "Applicable testing types: Smoke testing, Functional testing, End-to-End testing, Regression testing, Validation testing, Role-based access testing, RTM-based coverage testing, and Selenium TestNG automation execution."
    Replace-ParagraphContaining $document "Include the automation applicability as per the release need" "Automation applicability: Selenium WebDriver with Java, TestNG, Maven, Page Object Model, grouped smoke/regression suites, Extent Reports, screenshots, Log4j2 execution logs, and Excel-driven test data."
    Replace-ParagraphContaining $document "List down the testing tools/solution accelerators" "Tools used for this release are captured in the table below. Rows not applicable to this release are intentionally left blank to preserve the reference report structure."
    Replace-ParagraphContaining $document "List down all the Test Environment" "Test environment details are provided below. Additional rows are retained from the reference template and left blank where no update is required."
    Replace-ParagraphContaining $document "Bring out the different testing types and the execution summary" "The execution summary below reflects CineBook manual/functional test evidence and the latest Selenium TestNG automation execution available in the project reports."
    Replace-ParagraphContaining $document "Bring out the defect summary details" "The defect summary below reflects all open defects captured in the CineBook defect report workbook."
    Replace-ParagraphContaining $document "Sample metrics" "Metrics below are updated for the CineBook release where data is available. Cells are left blank where the metric is not tracked for this testing cycle."
    Replace-ParagraphContaining $document "List down the defects by application and status-wise" "CineBook currently has 7 open/new defects: 2 Critical priority, 3 High priority, and 2 Medium priority."
    Replace-ParagraphContaining $document "List down all the defects raised during execution across different applications and got deferred" "No defects are formally deferred in the current defect report. All 7 reported defects are in New/Open status."
    Replace-ParagraphContaining $document "List down the list of deliverables" "Testing deliverables: FRD, test scenario sheet, test case sheet, defect report, RTM, Selenium TestNG automation framework, Extent report, automation log, and screenshots for failed/known-defect evidence."
    Replace-ParagraphContaining $document "List down the Release Exit criteria" "Release exit criteria are assessed below. The release is recommended for conditional QA sign-off only after Critical booking defects are fixed/retested or formally approved for deferral."
    Replace-ParagraphContaining $document "List down the Total number of UAT defects RCA" "Root cause analysis is summarized for the current open defect set. Percentages are based on 7 reported defects."
    Replace-ParagraphContaining $document "Capture Known Issues if any" "Known issues: DF_201 concurrent booking seat count issue, DF_203 missing seat hold/reservation enforcement, DF_101 missing movie poster, DF_102 trailer playback issue, DF_202 booked seat selection timing inconsistency, DF_401 unfriendly admin validation error, and DF_001 analytics audience-interest scoping issue."
    Replace-ParagraphContaining $document "Specific recommendations if applicable" "Recommendation: Conditional Go for QA sign-off after closure or approved deferral of the two Critical priority booking defects. The QA team recommends fixing and retesting DF_201 and DF_203 before final production approval."

    $tables = $document.Tables

    # Table 1: document approval metadata.
    Set-CellText $tables.Item(1) 2 2 "Gnaneshwar"
    Set-CellText $tables.Item(1) 2 3 "Maheshwaran"
    Set-CellText $tables.Item(1) 3 2 "Team Lead"
    Set-CellText $tables.Item(1) 3 3 "Manager"
    Set-CellText $tables.Item(1) 5 2 $reportDate
    Set-CellText $tables.Item(1) 5 3 $reportDate

    # Table 2: tools. Existing rows are preserved; non-applicable rows remain blank.
    Set-CellText $tables.Item(2) 2 2 "JUnit/TestNG developer checks"
    Set-CellText $tables.Item(2) 2 3 "TestNG 7.10.2"
    Set-CellText $tables.Item(2) 2 4 "Open Source"
    Set-CellText $tables.Item(2) 3 2 "Selenium WebDriver, TestNG, Maven"
    Set-CellText $tables.Item(2) 3 3 "Selenium 4.44.0, Maven project"
    Set-CellText $tables.Item(2) 3 4 "Open Source"
    Set-CellText $tables.Item(2) 4 2 "Maven/TestNG suite execution"
    Set-CellText $tables.Item(2) 4 3 "Project configured"
    Set-CellText $tables.Item(2) 4 4 "Open Source"
    Set-CellText $tables.Item(2) 5 2 "Excel test data/workbook"
    Set-CellText $tables.Item(2) 5 3 "CineBookTestingReport workbook"
    Set-CellText $tables.Item(2) 5 4 "Project artifact"
    Set-CellText $tables.Item(2) 6 2 "Selenium WebDriver with Java Page Object Model"
    Set-CellText $tables.Item(2) 6 3 "Selenium 4.44.0"
    Set-CellText $tables.Item(2) 6 4 "Open Source"

    # Table 3: environments.
    Set-CellText $tables.Item(3) 2 1 "QA/Test Environment"
    Set-CellText $tables.Item(3) 2 2 "CineBook Web Application"
    Set-CellText $tables.Item(3) 2 3 "https://cine-book-one-rosy.vercel.app/"
    Set-CellText $tables.Item(3) 2 4 "QA Team - Gnaneshwar"
    Set-CellText $tables.Item(3) 2 5 "Validated through Chrome/Edge browser workflows and Selenium automation."

    # Table 4: execution summary.
    Set-CellText $tables.Item(4) 2 1 "Functional / Regression Testing"
    Set-CellText $tables.Item(4) 2 2 "166"
    Set-CellText $tables.Item(4) 2 3 "166"
    Set-CellText $tables.Item(4) 2 4 "158"
    Set-CellText $tables.Item(4) 2 5 "8"
    Set-CellText $tables.Item(4) 2 6 "0"
    Set-CellText $tables.Item(4) 2 7 "100%"
    Set-CellText $tables.Item(4) 2 8 "95.18%"
    Set-CellText $tables.Item(4) 2 9 "4.82%"
    Set-CellText $tables.Item(4) 2 10 "0%"
    Set-CellText $tables.Item(4) 3 1 "Selenium TestNG Automation Snapshot"
    Set-CellText $tables.Item(4) 3 2 "29"
    Set-CellText $tables.Item(4) 3 3 "29"
    Set-CellText $tables.Item(4) 3 4 "20"
    Set-CellText $tables.Item(4) 3 5 "1"
    Set-CellText $tables.Item(4) 3 6 "8"
    Set-CellText $tables.Item(4) 3 7 "100%"
    Set-CellText $tables.Item(4) 3 8 "68.97%"
    Set-CellText $tables.Item(4) 3 9 "3.45%"
    Set-CellText $tables.Item(4) 3 10 "27.59%"
    Set-CellText $tables.Item(4) 4 2 "195"
    Set-CellText $tables.Item(4) 4 3 "195"
    Set-CellText $tables.Item(4) 4 4 "178"
    Set-CellText $tables.Item(4) 4 5 "9"
    Set-CellText $tables.Item(4) 4 6 "8"
    Set-CellText $tables.Item(4) 4 7 "100%"
    Set-CellText $tables.Item(4) 4 8 "91.28%"
    Set-CellText $tables.Item(4) 4 9 "4.62%"
    Set-CellText $tables.Item(4) 4 10 "4.10%"

    # Table 5: defect summary.
    Set-CellText $tables.Item(5) 2 1 "CineBook Functional Testing"
    Set-CellText $tables.Item(5) 2 2 "7"
    Set-CellText $tables.Item(5) 2 3 "0"
    Set-CellText $tables.Item(5) 2 4 "0"
    Set-CellText $tables.Item(5) 2 5 "0"
    Set-CellText $tables.Item(5) 2 6 "0"
    Set-CellText $tables.Item(5) 3 1 "Critical Booking Defects"
    Set-CellText $tables.Item(5) 3 2 "2"
    Set-CellText $tables.Item(5) 3 3 "0"
    Set-CellText $tables.Item(5) 3 4 "0"
    Set-CellText $tables.Item(5) 3 5 "0"
    Set-CellText $tables.Item(5) 3 6 "0"
    Set-CellText $tables.Item(5) 4 2 "7"
    Set-CellText $tables.Item(5) 4 3 "0"
    Set-CellText $tables.Item(5) 4 4 "0"
    Set-CellText $tables.Item(5) 4 5 "0"
    Set-CellText $tables.Item(5) 4 6 "0"

    # Table 6: overall metrics.
    Set-CellText $tables.Item(6) 2 2 "Accepted stories / agreed stories"
    Set-CellText $tables.Item(6) 2 3 "100%"
    Set-CellText $tables.Item(6) 2 4 ""
    Set-CellText $tables.Item(6) 2 5 "Non SLA"
    Set-CellText $tables.Item(6) 3 2 "Completed test cases in release"
    Set-CellText $tables.Item(6) 3 3 "60"
    Set-CellText $tables.Item(6) 3 4 "60 unique test cases"
    Set-CellText $tables.Item(6) 3 5 "Non SLA"
    Set-CellText $tables.Item(6) 4 2 "Execution progress against plan"
    Set-CellText $tables.Item(6) 4 3 "100%"
    Set-CellText $tables.Item(6) 4 4 "100% execution"
    Set-CellText $tables.Item(6) 4 5 "Non SLA"
    Set-CellText $tables.Item(6) 5 2 "Open/deferred defects carried forward"
    Set-CellText $tables.Item(6) 5 3 "0"
    Set-CellText $tables.Item(6) 5 4 "7 open defects"
    Set-CellText $tables.Item(6) 5 5 "Non SLA"
    Set-CellText $tables.Item(6) 6 2 "Production defects after release"
    Set-CellText $tables.Item(6) 6 3 "0%"
    Set-CellText $tables.Item(6) 6 4 "0% - not released to production"
    Set-CellText $tables.Item(6) 6 5 "SLA"

    # Table 7: exit criteria.
    Set-CellText $tables.Item(7) 2 2 "100% planned test execution completed"
    Set-CellText $tables.Item(7) 2 3 "166/166 functional test steps executed"
    Set-CellText $tables.Item(7) 2 4 "Met"
    Set-CellText $tables.Item(7) 2 5 "Execution completed for documented CineBook test cases."
    Set-CellText $tables.Item(7) 3 2 "Minimum 95% pass rate"
    Set-CellText $tables.Item(7) 3 3 "95.18%"
    Set-CellText $tables.Item(7) 3 4 "Met"
    Set-CellText $tables.Item(7) 3 5 "158 passed and 8 failed test steps."
    Set-CellText $tables.Item(7) 4 2 "Zero open Critical/Very High defects"
    Set-CellText $tables.Item(7) 4 3 "2 Critical priority defects open"
    Set-CellText $tables.Item(7) 4 4 "Not Met"
    Set-CellText $tables.Item(7) 4 5 "DF_201 and DF_203 must be fixed/retested or formally deferred."
    Set-CellText $tables.Item(7) 5 2 "Zero unapproved major defects"
    Set-CellText $tables.Item(7) 5 3 "3 High priority defects open"
    Set-CellText $tables.Item(7) 5 4 "Not Met"
    Set-CellText $tables.Item(7) 5 5 "High priority defects require manager/business review."
    Set-CellText $tables.Item(7) 6 1 "Automation regression"
    Set-CellText $tables.Item(7) 6 2 "No failed critical automation checks"
    Set-CellText $tables.Item(7) 6 3 "20 passed, 1 failed, 8 skipped"
    Set-CellText $tables.Item(7) 6 4 "Partially Met"
    Set-CellText $tables.Item(7) 6 5 "Latest TestNG log shows one My Bookings tab failure."

    # Table 8: UAT defect root cause analysis. Rows retained exactly as provided.
    Set-CellText $tables.Item(8) 2 2 "0%"
    Set-CellText $tables.Item(8) 3 2 "0%"
    Set-CellText $tables.Item(8) 4 2 "0%"
    Set-CellText $tables.Item(8) 5 2 "14.29%"
    Set-CellText $tables.Item(8) 6 2 "85.71%"
    Set-CellText $tables.Item(8) 7 2 "0%"
    Set-CellText $tables.Item(8) 8 2 "0%"
    Set-CellText $tables.Item(8) 9 2 "100%"

    # Table 9: sign-off.
    Set-CellText $tables.Item(9) 2 1 "Gnaneshwar"
    Set-CellText $tables.Item(9) 2 2 "Team Lead"
    Set-CellText $tables.Item(9) 2 4 $reportDate
    Set-CellText $tables.Item(9) 3 1 "Maheshwaran"
    Set-CellText $tables.Item(9) 3 2 "Manager"
    Set-CellText $tables.Item(9) 3 4 ""

    # Table 10: change log.
    Set-CellText $tables.Item(10) 2 2 "Initial CineBook test summary report baseline created on $reportDate by Gnaneshwar"
    Set-CellText $tables.Item(10) 5 2 "All applicable sections"
    Set-CellText $tables.Item(10) 5 3 "Gnaneshwar"
    Set-CellText $tables.Item(10) 5 4 $reportDate
    Set-CellText $tables.Item(10) 5 5 "Updated reference template with CineBook test execution, defect, metrics, RCA, recommendation, and sign-off details."

    if (Test-Path $OutputDocx) { Remove-Item -LiteralPath $OutputDocx -Force }
    if (Test-Path $OutputDoc) { Remove-Item -LiteralPath $OutputDoc -Force }
    $document.SaveAs([ref]$OutputDocx, [ref]16)
    $document.SaveAs([ref]$OutputDoc, [ref]0)
    $document.Close($false)
    Write-Output "Generated: $OutputDocx"
    Write-Output "Generated: $OutputDoc"
} finally {
    if ($word) { $word.Quit() }
}
