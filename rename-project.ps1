#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Rename Spring Boot Web Service Template project and packages

.DESCRIPTION
    Automatically renames the project artifact IDs and package names throughout the entire project.
    Updates:
    - Directory names (template-project-* to your-project-*)
    - POM files (artifact IDs and dependencies)
    - Kotlin source files (package declarations and imports)
    - Configuration files (application, logback, liquibase, docker, etc.)
    - Property files
    - OpenAPI specifications

.PARAMETER OldProjectName
    Current Maven artifact ID (default: "template-project")

.PARAMETER NewProjectName
    New Maven artifact ID (e.g., "my-awesome-service")

.PARAMETER OldPackage
    Current Kotlin package (default: "com.example.templateproject")

.PARAMETER NewPackage
    New Kotlin package (e.g., "com.mycompany.myservice")

.PARAMETER BackupFirst
    Create backup of original files before making changes (default: $true)

.PARAMETER DryRun
    Preview changes without executing them (switch)

.PARAMETER Verbose
    Show detailed output of all changes (switch)

.EXAMPLE
    .\rename-project.ps1 -NewProjectName "my-service" -NewPackage "com.acme.myservice"

.EXAMPLE
    .\rename-project.ps1 -OldProjectName "template-project" -NewProjectName "awesome-api" `
                        -OldPackage "com.example.templateproject" -NewPackage "com.company.awesomeapi" -Verbose

.EXAMPLE
    .\rename-project.ps1 -NewProjectName "new-service" -NewPackage "com.new.service" -DryRun
#>

param(
    [Parameter(Mandatory = $false)]
    [string]$OldProjectName = "template-project",

    [Parameter(Mandatory = $true)]
    [string]$NewProjectName,

    [Parameter(Mandatory = $false)]
    [string]$OldPackage = "com.example.templateproject",

    [Parameter(Mandatory = $true)]
    [string]$NewPackage,

    [Parameter(Mandatory = $false)]
    [bool]$BackupFirst = $true,

    [Parameter(Mandatory = $false)]
    [switch]$DryRun,

    [Parameter(Mandatory = $false)]
    [switch]$Verbose
)

# Set up error handling
$ErrorActionPreference = "Stop"
$VerbosePreference = if ($Verbose) { "Continue" } else { "SilentlyContinue" }

# Colors for output
$ErrorColor = "Red"
$SuccessColor = "Green"
$WarningColor = "Yellow"
$InfoColor = "Cyan"

function Write-Info {
    param([string]$Message)
    Write-Host $Message -ForegroundColor $InfoColor
}

function Write-Success {
    param([string]$Message)
    Write-Host $Message -ForegroundColor $SuccessColor
}

function Write-Error-Custom {
    param([string]$Message)
    Write-Host $Message -ForegroundColor $ErrorColor
}

function Write-Warning-Custom {
    param([string]$Message)
    Write-Host $Message -ForegroundColor $WarningColor
}

# Validate input
function Validate-Input {
    Write-Info "Validating input parameters..."

    if (-not $NewProjectName) {
        Write-Error-Custom "Error: NewProjectName is required"
        exit 1
    }

    if (-not $NewPackage) {
        Write-Error-Custom "Error: NewPackage is required"
        exit 1
    }

    if ($OldProjectName -eq $NewProjectName) {
        Write-Error-Custom "Error: OldProjectName and NewProjectName must be different"
        exit 1
    }

    if ($OldPackage -eq $NewPackage) {
        Write-Error-Custom "Error: OldPackage and NewPackage must be different"
        exit 1
    }

    # Validate naming conventions
    if ($NewProjectName -notmatch '^[a-z0-9][a-z0-9\-]*[a-z0-9]$') {
        Write-Error-Custom "Error: NewProjectName must be lowercase with hyphens (e.g., 'my-service')"
        exit 1
    }

    if ($NewPackage -notmatch '^[a-z]+([a-z0-9]*\.)*[a-z]+$') {
        Write-Error-Custom "Error: NewPackage must be valid Java package format (e.g., 'com.company.service')"
        exit 1
    }

    Write-Success "✓ Input validation passed"
}

# Get the script's directory
$ScriptDir = Split-Path -Parent -Path $MyInvocation.MyCommand.Definition

# Navigate to project root (where pom.xml is)
$ProjectRoot = Get-Location
if (-not (Test-Path "$ProjectRoot/pom.xml")) {
    Write-Error-Custom "Error: pom.xml not found in current directory. Please run from project root."
    exit 1
}

Write-Info "Project root: $ProjectRoot"

# Display what will be renamed
Write-Host "`n" -NoNewline
Write-Host "===================================================" -ForegroundColor White
Write-Host "Spring Boot Template - Project Rename Tool" -ForegroundColor White
Write-Host "===================================================" -ForegroundColor White
Write-Host "Old Project Name : $OldProjectName" -ForegroundColor White
Write-Host "New Project Name : $NewProjectName" -ForegroundColor White
Write-Host "Old Package      : $OldPackage" -ForegroundColor White
Write-Host "New Package      : $NewPackage" -ForegroundColor White
if ($DryRun) {
    Write-Host "Mode             : DRY RUN (no changes will be made)" -ForegroundColor Yellow
}
Write-Host "===================================================" -ForegroundColor White
Write-Host ""

Validate-Input

# Create backups if requested
if ($BackupFirst -and -not $DryRun) {
    Write-Info "Creating backups of important files..."

    $BackupDir = "$ProjectRoot/.backup-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
    New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null

    # Backup all pom.xml files
    Get-ChildItem -Path $ProjectRoot -Filter "pom.xml" -Recurse | ForEach-Object {
        $RelativePath = $_.FullName -replace [regex]::Escape($ProjectRoot + '\'), ''
        Copy-Item -Path $_.FullName -Destination "$BackupDir\$($RelativePath -replace '\\', '_')" -Force
        Write-Verbose "Backed up: $RelativePath"
    }

    Write-Success "✓ Backups created in: $BackupDir"
}

$RenameCount = 0
$FileCount = 0
$SkipCount = 0

# Helper function to perform replacements in files
function Update-FileContent {
    param(
        [string]$FilePath,
        [string]$OldText,
        [string]$NewText,
        [bool]$IsXmlOrKt = $false
    )

    if (-not (Test-Path $FilePath)) {
        return $false
    }

    try {
        $Content = Get-Content -Path $FilePath -Raw -ErrorAction SilentlyContinue
        if ($null -eq $Content) {
            return $false
        }

        if ($Content -contains $OldText -or $Content -match ([regex]::Escape($OldText))) {
            $NewContent = $Content -replace ([regex]::Escape($OldText)), $NewText

            if (-not $DryRun) {
                Set-Content -Path $FilePath -Value $NewContent -Encoding UTF8 -NoNewline
            }

            Write-Verbose "Updated: $FilePath"
            return $true
        }
    }
    catch {
        Write-Verbose "Warning: Could not process $FilePath - $_"
    }

    return $false
}

# 1. Rename directories
Write-Info "`nPhase 1: Renaming directories..."

$DirsToRename = @()
Get-ChildItem -Path $ProjectRoot -Directory -Filter "$OldProjectName*" | ForEach-Object {
    $OldDir = $_.FullName
    $NewDir = $OldDir -replace [regex]::Escape($OldProjectName), $NewProjectName

    if ($OldDir -ne $NewDir) {
        $DirsToRename += @{
            Old = $OldDir
            New = $NewDir
            RelOld = $_.Name
            RelNew = $_.Name -replace [regex]::Escape($OldProjectName), $NewProjectName
        }
    }
}

# Actually rename directories (reverse order to handle nested dirs)
$DirsToRename | Sort-Object -Property New -Descending | ForEach-Object {
    Write-Info "  Renaming: $($_.RelOld) → $($_.RelNew)"

    if (-not $DryRun) {
        Rename-Item -Path $_.Old -NewName $_.RelNew -Force -ErrorAction SilentlyContinue
    }

    $RenameCount++
}

if ($RenameCount -gt 0) {
    Write-Success "✓ Renamed $RenameCount directories"
} else {
    Write-Warning-Custom "⚠ No directories matched the old project name"
}

# 2. Update POM files
Write-Info "`nPhase 2: Updating POM files..."

$PomFiles = Get-ChildItem -Path $ProjectRoot -Filter "pom.xml" -Recurse

foreach ($PomFile in $PomFiles) {
    $RelativePath = $PomFile.FullName -replace [regex]::Escape($ProjectRoot + '\'), ''

    $Updated = Update-FileContent -FilePath $PomFile.FullName -OldText $OldProjectName -NewText $NewProjectName

    if ($Updated) {
        Write-Verbose "  Updated: $RelativePath"
        $FileCount++
    }
}

Write-Success "✓ Updated $FileCount POM files"

# 3. Update Kotlin files (package declarations and imports)
Write-Info "`nPhase 3: Updating Kotlin source files..."

$KtFiles = Get-ChildItem -Path $ProjectRoot -Filter "*.kt" -Recurse

$OldPackagePath = $OldPackage -replace '\.', '/'
$NewPackagePath = $NewPackage -replace '\.', '/'

foreach ($KtFile in $KtFiles) {
    $RelativePath = $KtFile.FullName -replace [regex]::Escape($ProjectRoot + '\'), ''

    $Updated = $false

    # Update package declarations
    if (Update-FileContent -FilePath $KtFile.FullName -OldText "package $OldPackage" -NewText "package $NewPackage") {
        $Updated = $true
    }

    # Update imports
    if (Update-FileContent -FilePath $KtFile.FullName -OldText "import $OldPackage" -NewText "import $NewPackage") {
        $Updated = $true
    }

    # Update class references in the same file
    if (Update-FileContent -FilePath $KtFile.FullName -OldText "$OldPackage." -NewText "$NewPackage.") {
        $Updated = $true
    }

    if ($Updated) {
        Write-Verbose "  Updated: $RelativePath"
        $FileCount++
    }
}

Write-Success "✓ Updated $FileCount Kotlin files"

# 4. Rename source directories (com/example/templateproject -> com/mycompany/myservice)
Write-Info "`nPhase 4: Renaming source package directories..."

Get-ChildItem -Path "$ProjectRoot/**/src/main/kotlin" -Directory -ErrorAction SilentlyContinue | ForEach-Object {
    $SrcDir = $_.FullName
    $BaseDir = $SrcDir | Split-Path

    if (Test-Path "$SrcDir/$OldPackagePath") {
        $OldPackageDir = "$SrcDir/$OldPackagePath"
        $NewPackageDir = "$SrcDir/$NewPackagePath"

        # Create new package directory structure
        $NewPackageDirParent = Split-Path -Parent $NewPackageDir
        if (-not (Test-Path $NewPackageDirParent)) {
            New-Item -ItemType Directory -Path $NewPackageDirParent -Force | Out-Null
        }

        if (-not $DryRun) {
            Move-Item -Path $OldPackageDir -Destination $NewPackageDir -Force -ErrorAction SilentlyContinue
        }

        Write-Info "  Renamed: $OldPackagePath → $NewPackagePath"
        $RenameCount++
    }
}

Write-Success "✓ Renamed $RenameCount package directories"

# 5. Update test files
Write-Info "`nPhase 5: Updating test files..."

$TestKtFiles = Get-ChildItem -Path "$ProjectRoot/**/src/test/kotlin" -Filter "*.kt" -Recurse -ErrorAction SilentlyContinue

foreach ($KtFile in $TestKtFiles) {
    $RelativePath = $KtFile.FullName -replace [regex]::Escape($ProjectRoot + '\'), ''

    $Updated = $false

    if (Update-FileContent -FilePath $KtFile.FullName -OldText "package $OldPackage" -NewText "package $NewPackage") {
        $Updated = $true
    }

    if (Update-FileContent -FilePath $KtFile.FullName -OldText "import $OldPackage" -NewText "import $NewPackage") {
        $Updated = $true
    }

    if ($Updated) {
        Write-Verbose "  Updated: $RelativePath"
        $FileCount++
    }
}

Write-Success "✓ Updated $FileCount test files"

# 6. Update configuration files
Write-Info "`nPhase 6: Updating configuration files..."

$ConfigFiles = Get-ChildItem -Path $ProjectRoot -Include "*.yml", "*.yaml", "*.properties" -Recurse -ErrorAction SilentlyContinue

foreach ($ConfigFile in $ConfigFiles) {
    $RelativePath = $ConfigFile.FullName -replace [regex]::Escape($ProjectRoot + '\'), ''

    $Updated = $false

    if (Update-FileContent -FilePath $ConfigFile.FullName -OldText $OldPackage -NewText $NewPackage) {
        $Updated = $true
    }

    if (Update-FileContent -FilePath $ConfigFile.FullName -OldText $OldProjectName -NewText $NewProjectName) {
        $Updated = $true
    }

    if ($Updated) {
        Write-Verbose "  Updated: $RelativePath"
        $FileCount++
    }
}

Write-Success "✓ Updated $FileCount configuration files"

# 7. Update Docker and compose files
Write-Info "`nPhase 7: Updating Docker files..."

$DockerFiles = Get-ChildItem -Path $ProjectRoot -Include "Dockerfile", "docker-compose*.yml" -ErrorAction SilentlyContinue

foreach ($DockerFile in $DockerFiles) {
    $Updated = Update-FileContent -FilePath $DockerFile.FullName -OldText $OldProjectName -NewText $NewProjectName

    if ($Updated) {
        Write-Verbose "  Updated: $DockerFile.Name"
        $FileCount++
    }
}

Write-Success "✓ Updated $FileCount Docker files"

# 8. Update other files
Write-Info "`nPhase 8: Updating other files..."

$OtherFiles = Get-ChildItem -Path $ProjectRoot -Include "openapi.yaml", "*.md", "*.xml" -Recurse -ErrorAction SilentlyContinue

foreach ($OtherFile in $OtherFiles) {
    if ($OtherFile.Name -like "target" -or $OtherFile.FullName -like "*target*") {
        continue
    }

    $Updated = $false

    if (Update-FileContent -FilePath $OtherFile.FullName -OldText $OldProjectName -NewText $NewProjectName) {
        $Updated = $true
    }

    if (Update-FileContent -FilePath $OtherFile.FullName -OldText $OldPackage -NewText $NewPackage) {
        $Updated = $true
    }

    if ($Updated) {
        $RelativePath = $OtherFile.FullName -replace [regex]::Escape($ProjectRoot + '\'), ''
        Write-Verbose "  Updated: $RelativePath"
        $FileCount++
    }
}

Write-Success "✓ Updated $FileCount other files"

# Final summary
Write-Host "`n" -NoNewline
Write-Host "===================================================" -ForegroundColor Green
if ($DryRun) {
    Write-Host "DRY RUN - NO CHANGES APPLIED" -ForegroundColor Yellow
    Write-Host "===================================================" -ForegroundColor Green
    Write-Host "This operation would have made the following changes:" -ForegroundColor White
} else {
    Write-Host "RENAME COMPLETED SUCCESSFULLY" -ForegroundColor Green
    Write-Host "===================================================" -ForegroundColor Green
}

Write-Host "Directories renamed: $RenameCount" -ForegroundColor White
Write-Host "Files updated: $FileCount" -ForegroundColor White
Write-Host "===================================================" -ForegroundColor Green

Write-Host "`nNext steps:" -ForegroundColor Cyan
Write-Host "1. Review the changes (especially pom.xml files)" -ForegroundColor White
Write-Host "2. Verify all Kotlin source files have correct package declarations" -ForegroundColor White
Write-Host "3. Run: mvn clean install" -ForegroundColor White
Write-Host "4. Run: mvn spring-boot:run -pl $NewProjectName-web" -ForegroundColor White
Write-Host "5. If errors occur, restore from backup: .backup-*/" -ForegroundColor White

if (-not $DryRun) {
    Write-Host "`nTo verify changes, you can run:" -ForegroundColor Cyan
    Write-Host "  grep -r '$OldProjectName' . --include='*.xml' --include='*.kt'" -ForegroundColor Gray
    Write-Host "  grep -r '$OldPackage' . --include='*.kt' --include='*.xml'" -ForegroundColor Gray
}

Write-Host ""

