param(
    [string]$WorkbookPath = "C:\Users\2492244\Downloads\CineBookTestingReport (4) (2).xlsx",
    [string]$ProjectRoot = "C:\Users\2492244\Downloads\i-am-going-to-build-testing\i-am-going-to-build-testing",
    [string]$OutputDocx = "C:\Users\2492244\Downloads\i-am-going-to-build-testing\i-am-going-to-build-testing\reports\CineBook_Test_Summary_Report_Gnaneshwar.docx",
    [string]$OutputMarkdown = "C:\Users\2492244\Downloads\i-am-going-to-build-testing\i-am-going-to-build-testing\reports\CineBook_Test_Summary_Report_Gnaneshwar.md"
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.IO.Compression.FileSystem

function Escape-Xml([string]$Text) {
    if ($null -eq $Text) { return "" }
    return [System.Security.SecurityElement]::Escape($Text)
}

function Read-ZipEntryText($Zip, [string]$Name) {
    $entry = $Zip.GetEntry($Name)
    if ($null -eq $entry) { throw "Missing zip entry: $Name" }
    $reader = [System.IO.StreamReader]::new($entry.Open())
    try { return $reader.ReadToEnd() } finally { $reader.Close() }
}

function Get-ColumnNumber([string]$CellRef) {
    $letters = $CellRef -replace "\d", ""
    $n = 0
    foreach ($ch in $letters.ToCharArray()) {
        $n = ($n * 26) + ([int][char]$ch - [int][char]"A" + 1)
    }
    return $n
}

function Read-XlsxSheetRows($Zip, [string]$SheetPath, [object[]]$SharedStrings) {
    [xml]$sheetXml = Read-ZipEntryText $Zip $SheetPath
    $rows = @()
    foreach ($row in $sheetXml.worksheet.sheetData.row) {
        $values = @{}
        foreach ($cell in $row.c) {
            $col = Get-ColumnNumber $cell.r
            $raw = $cell.v
            if ($cell.t -eq "s" -and $null -ne $raw) {
                $value = $SharedStrings[[int]$raw]
            } elseif ($cell.t -eq "inlineStr") {
                $value = $cell.is.t
            } else {
                $value = $raw
            }
            $values[$col] = [string]$value
        }
        if ($values.Count -gt 0) {
            $max = ($values.Keys | Measure-Object -Maximum).Maximum
            $arr = @()
            for ($i = 1; $i -le $max; $i++) {
                if ($values.ContainsKey($i)) { $arr += $values[$i] } else { $arr += "" }
            }
            $rows += ,$arr
        }
    }
    return $rows
}

function Add-ZipText($Archive, [string]$Path, [string]$Content) {
    $entry = $Archive.CreateEntry($Path)
    $writer = [System.IO.StreamWriter]::new($entry.Open(), [System.Text.UTF8Encoding]::new($false))
    try { $writer.Write($Content) } finally { $writer.Close() }
}

function P([string]$Text, [string]$Style = "", [bool]$Bold = $false) {
    $styleXml = if ($Style) { "<w:pPr><w:pStyle w:val=`"$Style`"/></w:pPr>" } else { "" }
    $boldXml = if ($Bold) { "<w:rPr><w:b/></w:rPr>" } else { "" }
    $safe = Escape-Xml $Text
    return "<w:p>$styleXml<w:r>$boldXml<w:t xml:space=`"preserve`">$safe</w:t></w:r></w:p>"
}

function Bullet([string]$Text) {
    $safe = Escape-Xml $Text
    return "<w:p><w:pPr><w:numPr><w:ilvl w:val=`"0`"/><w:numId w:val=`"1`"/></w:numPr></w:pPr><w:r><w:t xml:space=`"preserve`">$safe</w:t></w:r></w:p>"
}

function Table($Headers, $Rows) {
    $xml = "<w:tbl><w:tblPr><w:tblStyle w:val=`"TableGrid`"/><w:tblW w:w=`"0`" w:type=`"auto`"/><w:tblBorders><w:top w:val=`"single`" w:sz=`"6`" w:space=`"0`" w:color=`"666666`"/><w:left w:val=`"single`" w:sz=`"6`" w:space=`"0`" w:color=`"666666`"/><w:bottom w:val=`"single`" w:sz=`"6`" w:space=`"0`" w:color=`"666666`"/><w:right w:val=`"single`" w:sz=`"6`" w:space=`"0`" w:color=`"666666`"/><w:insideH w:val=`"single`" w:sz=`"6`" w:space=`"0`" w:color=`"666666`"/><w:insideV w:val=`"single`" w:sz=`"6`" w:space=`"0`" w:color=`"666666`"/></w:tblBorders></w:tblPr>"
    $xml += "<w:tr>"
    foreach ($h in $Headers) {
        $xml += "<w:tc><w:tcPr><w:shd w:fill=`"D9EAF7`"/></w:tcPr>" + (P $h "" $true) + "</w:tc>"
    }
    $xml += "</w:tr>"
    foreach ($row in $Rows) {
        $xml += "<w:tr>"
        foreach ($cell in $row) {
            $text = ([string]$cell).Replace("`r`n", "`n").Replace("`r", "`n")
            $lines = $text -split "`n"
            $xml += "<w:tc>"
            foreach ($line in $lines) { $xml += P $line }
            $xml += "</w:tc>"
        }
        $xml += "</w:tr>"
    }
    $xml += "</w:tbl>"
    return $xml
}

$zip = [IO.Compression.ZipFile]::OpenRead($WorkbookPath)
try {
    [xml]$sst = Read-ZipEntryText $zip "xl/sharedStrings.xml"
    $shared = @()
    foreach ($si in $sst.sst.si) {
        $parts = @()
        foreach ($node in $si.ChildNodes) {
            if ($node.Name -eq "t") { $parts += $node.InnerText }
            elseif ($node.Name -eq "r") { $parts += $node.t.InnerText }
        }
        $shared += ($parts -join "")
    }

    $scenarioRows = Read-XlsxSheetRows $zip "xl/worksheets/sheet1.xml" $shared
    $caseRows = Read-XlsxSheetRows $zip "xl/worksheets/sheet2.xml" $shared
    $defectRows = Read-XlsxSheetRows $zip "xl/worksheets/sheet3.xml" $shared
    $rtmRows = Read-XlsxSheetRows $zip "xl/worksheets/sheet4.xml" $shared
} finally {
    $zip.Dispose()
}

$scenarioData = $scenarioRows | Select-Object -Skip 1
$caseData = $caseRows | Select-Object -Skip 1
$defectData = $defectRows | Select-Object -Skip 1
$rtmData = $rtmRows | Select-Object -Skip 1

$uniqueTestCases = $caseData | ForEach-Object { $_[1] } | Where-Object { $_ -match "^TC_" } | Sort-Object -Unique
$executionStatuses = $caseData | ForEach-Object { $_[8] } | Where-Object { $_.Trim() }
$passedSteps = ($executionStatuses | Where-Object { $_ -match "^Pass$" }).Count
$failedSteps = ($executionStatuses | Where-Object { $_ -match "^Fail$" }).Count
$passPercent = [math]::Round(($passedSteps / [math]::Max(1, $executionStatuses.Count)) * 100, 2)
$failPercent = [math]::Round(($failedSteps / [math]::Max(1, $executionStatuses.Count)) * 100, 2)

$severityGroups = @{}
foreach ($g in ($defectData | Group-Object { $_[5].Trim().ToLower() })) { $severityGroups[$g.Name] = $g.Count }
$priorityGroups = @{}
foreach ($g in ($defectData | Group-Object { $_[6].Trim().ToLower() })) { $priorityGroups[$g.Name] = $g.Count }
$statusGroups = @{}
foreach ($g in ($defectData | Group-Object { $_[9].Trim().ToLower() })) { $statusGroups[$g.Name] = $g.Count }

$moduleSummary = $scenarioData |
    Group-Object { $_[0] } |
    Sort-Object Count -Descending |
    ForEach-Object { @($_.Name, $_.Count) }

$rtmRequirementCount = ($rtmData | ForEach-Object { $_[1] } | Where-Object { $_ } | Sort-Object -Unique).Count

$logPath = Join-Path $ProjectRoot "reports\cinebook-automation.log"
$latestAutomation = @{ Passed = 0; Failed = 0; Skipped = 0 }
if (Test-Path $logPath) {
    $latestLines = Get-Content $logPath | Where-Object { $_ -match "^2026-06-29 22:" }
    $latestAutomation.Passed = ($latestLines | Select-String "Passed:").Count
    $latestAutomation.Failed = ($latestLines | Select-String "Failed:").Count
    $latestAutomation.Skipped = ($latestLines | Select-String "Skipped:").Count
}

$reportDate = "29-Jun-2026"
$releaseDecision = "Conditional Go for QA sign-off after closure or approved deferral of the two Critical priority defects."
$executiveSummary = "CineBook functional testing covered login, registration, movie browsing, theater browsing, booking and seat selection, payment, my bookings, review/interest, and admin workflows. The team executed 166 test steps across 60 unique test cases and achieved $passPercent% pass rate. Seven defects remain open, including two Critical priority booking defects related to concurrent booking and seat reservation. The application is functionally strong for core navigation and admin validation, but final release approval should wait until Critical booking risks are resolved or formally accepted by the business."

$defectRowsForTable = $defectData | ForEach-Object {
    @($_[1], $_[2], $_[5], $_[6], $_[7], $_[9])
}

$moduleRowsForTable = $moduleSummary | ForEach-Object { $_ }

$body = ""
$body += P "Test Summary Report" "Title"
$body += P "CineBook Movie Ticket Booking Platform" "Subtitle"
$body += P "Prepared by: Gnaneshwar, Team Lead"
$body += P "Team Size: 6"
$body += P "Submitted to: Maheshwaran, Manager"
$body += P "Report Date: $reportDate"
$body += P " "

$body += P "1. Executive Summary" "Heading1"
$body += P $executiveSummary

$body += P "2. Project and Report Details" "Heading1"
$body += Table @("Field", "Details") @(
    @("Project", "CineBook Movie Ticket Booking Platform"),
    @("Report Type", "Agile Test Summary Report"),
    @("Prepared By", "Gnaneshwar, Team Lead"),
    @("Submitted To", "Maheshwaran, Manager"),
    @("QA Team Size", "6 members"),
    @("Reference FRD", "FRD_CineBook_Movie_Ticket_Booking_Platform_Flow 1 1.docx"),
    @("Primary Test Evidence", "CineBookTestingReport (4) (2).xlsx, Selenium TestNG automation logs, Extent report"),
    @("Test Environment", "CineBook web application, Selenium WebDriver, TestNG, Maven, Chrome/Edge browser validation")
)

$body += P "3. Scope of Testing" "Heading1"
$body += Bullet "Functional validation against FRD requirements and RTM mappings."
$body += Bullet "Positive, negative, validation, navigation, access control, and role-based workflow testing."
$body += Bullet "Manual test documentation and automated Selenium TestNG regression execution."
$body += Bullet "Modules covered: Login, Registration, Movie Browsing, Theater Browsing, Booking, Payment, My Bookings, Review/Interest, Admin Movie Management, Admin Show Management, Admin Booking Management, Admin Analytics, Navigation and Access Control."

$body += P "4. Test Coverage Summary" "Heading1"
$body += Table @("Metric", "Result") @(
    @("Total test scenarios", $scenarioData.Count),
    @("Unique test cases", $uniqueTestCases.Count),
    @("Executed test steps", $executionStatuses.Count),
    @("Passed test steps", "$passedSteps ($passPercent%)"),
    @("Failed test steps", "$failedSteps ($failPercent%)"),
    @("Open defects", $defectData.Count),
    @("RTM mapped rows", $rtmData.Count),
    @("Unique requirement references covered in RTM", $rtmRequirementCount)
)

$body += P "5. Automation Execution Snapshot" "Heading1"
$body += Table @("Automation Source", "Passed", "Failed", "Skipped", "Observation") @(
    @("Latest Selenium TestNG log run on 29-Jun-2026 22:00 hour", $latestAutomation.Passed, $latestAutomation.Failed, $latestAutomation.Skipped, "One My Bookings tab test failed because the expected tab element was not found; skipped cases are mainly known-defect or excluded flow validations.")
)

$body += P "6. Module Coverage" "Heading1"
$body += Table @("Module", "Scenario Count") $moduleRowsForTable

$body += P "7. Defect Summary" "Heading1"
$body += Table @("Category", "Count") @(
    @("Total open defects", $defectData.Count),
    @("Critical priority", ($priorityGroups["critical"] + 0)),
    @("High priority", ($priorityGroups["high"] + 0)),
    @("Medium priority", ($priorityGroups["medium"] + 0)),
    @("Medium severity", ($severityGroups["medium"] + 0)),
    @("Low severity", ($severityGroups["low"] + 0)),
    @("New status", ($statusGroups["new"] + 0))
)

$body += P "8. Open Defect Details" "Heading1"
$body += Table @("Defect ID", "Description", "Severity", "Priority", "Reported By", "Status") $defectRowsForTable

$body += P "9. Key Risks and Observations" "Heading1"
$body += Bullet "Booking concurrency and seat hold defects can affect revenue accuracy and customer trust, so they should be treated as release blockers unless business approval is provided."
$body += Bullet "Movie media defects such as poster and trailer playback affect the user experience but can be released only with a documented workaround or fix plan."
$body += Bullet "Admin analytics and validation issues are manageable but should be resolved to avoid misleading operational dashboards and unfriendly error messages."
$body += Bullet "RTM coverage is established across 16 requirement references, supporting traceability from FRD to scenarios, test cases, and defects."

$body += P "10. Exit Criteria Assessment" "Heading1"
$body += Table @("Exit Criterion", "Status", "Remarks") @(
    @("All planned functional modules tested", "Met", "Core moviegoer and admin workflows are covered."),
    @("RTM updated", "Met", "63 RTM rows map requirements to scenarios/test cases/defects."),
    @("Critical defects closed", "Not Met", "Two Critical priority defects remain open in booking/seat reservation."),
    @("Test evidence available", "Met", "Workbook, automation log, screenshots, and Extent report are available."),
    @("Regression automation executed", "Partially Met", "Latest automation run has 20 passed, 1 failed, and 8 skipped results.")
)

$body += P "11. Recommendation" "Heading1"
$body += P $releaseDecision
$body += P "The QA team recommends completing fixes and retesting for DF_201 and DF_203 before final production approval. If the release must proceed, these defects should be explicitly accepted by Maheshwaran/business stakeholders with a documented mitigation and post-release fix timeline."

$body += P "12. Sign-Off" "Heading1"
$body += Table @("Role", "Name", "Status") @(
    @("QA Team Lead", "Gnaneshwar", "Prepared and submitted"),
    @("Manager", "Maheshwaran", "Pending review/approval")
)

$documentXml = "<?xml version=`"1.0`" encoding=`"UTF-8`" standalone=`"yes`"?><w:document xmlns:w=`"http://schemas.openxmlformats.org/wordprocessingml/2006/main`"><w:body>$body<w:sectPr><w:pgSz w:w=`"11906`" w:h=`"16838`"/><w:pgMar w:top=`"1008`" w:right=`"1008`" w:bottom=`"1008`" w:left=`"1008`" w:header=`"708`" w:footer=`"708`" w:gutter=`"0`"/></w:sectPr></w:body></w:document>"

$stylesXml = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:style w:type="paragraph" w:default="1" w:styleId="Normal"><w:name w:val="Normal"/><w:rPr><w:sz w:val="22"/><w:szCs w:val="22"/></w:rPr></w:style>
  <w:style w:type="paragraph" w:styleId="Title"><w:name w:val="Title"/><w:rPr><w:b/><w:sz w:val="36"/><w:color w:val="1F4E79"/></w:rPr></w:style>
  <w:style w:type="paragraph" w:styleId="Subtitle"><w:name w:val="Subtitle"/><w:rPr><w:sz w:val="26"/><w:color w:val="666666"/></w:rPr></w:style>
  <w:style w:type="paragraph" w:styleId="Heading1"><w:name w:val="heading 1"/><w:basedOn w:val="Normal"/><w:next w:val="Normal"/><w:rPr><w:b/><w:sz w:val="28"/><w:color w:val="1F4E79"/></w:rPr></w:style>
  <w:style w:type="table" w:styleId="TableGrid"><w:name w:val="Table Grid"/><w:tblPr><w:tblBorders><w:top w:val="single" w:sz="4" w:space="0" w:color="auto"/><w:left w:val="single" w:sz="4" w:space="0" w:color="auto"/><w:bottom w:val="single" w:sz="4" w:space="0" w:color="auto"/><w:right w:val="single" w:sz="4" w:space="0" w:color="auto"/><w:insideH w:val="single" w:sz="4" w:space="0" w:color="auto"/><w:insideV w:val="single" w:sz="4" w:space="0" w:color="auto"/></w:tblBorders></w:tblPr></w:style>
</w:styles>
"@

$numberingXml = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:numbering xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:abstractNum w:abstractNumId="0"><w:multiLevelType w:val="hybridMultilevel"/><w:lvl w:ilvl="0"><w:start w:val="1"/><w:numFmt w:val="bullet"/><w:lvlText w:val="*"/><w:lvlJc w:val="left"/><w:pPr><w:ind w:left="720" w:hanging="360"/></w:pPr></w:lvl></w:abstractNum>
  <w:num w:numId="1"><w:abstractNumId w:val="0"/></w:num>
</w:numbering>
"@

$contentTypes = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
  <Override PartName="/word/numbering.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml"/>
  <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
  <Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
</Types>
"@

$rels = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>
"@

$docRels = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/numbering" Target="numbering.xml"/>
</Relationships>
"@

$created = (Get-Date).ToUniversalTime().ToString("s") + "Z"
$core = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dcmitype="http://purl.org/dc/dcmitype/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <dc:title>CineBook Test Summary Report</dc:title>
  <dc:creator>Gnaneshwar</dc:creator>
  <cp:lastModifiedBy>Codex</cp:lastModifiedBy>
  <dcterms:created xsi:type="dcterms:W3CDTF">$created</dcterms:created>
  <dcterms:modified xsi:type="dcterms:W3CDTF">$created</dcterms:modified>
</cp:coreProperties>
"@

$app = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
  <Application>Codex</Application>
</Properties>
"@

if (Test-Path $OutputDocx) { Remove-Item -LiteralPath $OutputDocx -Force }
$archive = [IO.Compression.ZipFile]::Open($OutputDocx, [IO.Compression.ZipArchiveMode]::Create)
try {
    Add-ZipText $archive "[Content_Types].xml" $contentTypes
    Add-ZipText $archive "_rels/.rels" $rels
    Add-ZipText $archive "word/document.xml" $documentXml
    Add-ZipText $archive "word/styles.xml" $stylesXml
    Add-ZipText $archive "word/numbering.xml" $numberingXml
    Add-ZipText $archive "word/_rels/document.xml.rels" $docRels
    Add-ZipText $archive "docProps/core.xml" $core
    Add-ZipText $archive "docProps/app.xml" $app
} finally {
    $archive.Dispose()
}

$md = @"
# Test Summary Report

**Project:** CineBook Movie Ticket Booking Platform  
**Prepared by:** Gnaneshwar, Team Lead  
**Team Size:** 6  
**Submitted to:** Maheshwaran, Manager  
**Report Date:** $reportDate

## Executive Summary

$executiveSummary

## Key Metrics

| Metric | Result |
|---|---:|
| Total test scenarios | $($scenarioData.Count) |
| Unique test cases | $($uniqueTestCases.Count) |
| Executed test steps | $($executionStatuses.Count) |
| Passed test steps | $passedSteps ($passPercent%) |
| Failed test steps | $failedSteps ($failPercent%) |
| Open defects | $($defectData.Count) |
| RTM mapped rows | $($rtmData.Count) |
| Unique requirement references covered | $rtmRequirementCount |

## Recommendation

$releaseDecision

The QA team recommends completing fixes and retesting for DF_201 and DF_203 before final production approval. If the release must proceed, these defects should be explicitly accepted by Maheshwaran/business stakeholders with a documented mitigation and post-release fix timeline.
"@

Set-Content -LiteralPath $OutputMarkdown -Value $md -Encoding UTF8

Write-Output "Generated: $OutputDocx"
Write-Output "Generated: $OutputMarkdown"
