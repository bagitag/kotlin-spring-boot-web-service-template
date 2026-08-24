#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Remove optional modules from Spring Boot Web Service Template

.DESCRIPTION
    Automatically removes the persistence and/or client modules from the project.
    Updates:
    - Root pom.xml (removes module declarations)
    - Core module pom.xml (removes dependencies)
    - TemplateApplication.kt (removes property source references)
    - JaCoCo configuration (removes exclusions for deleted modules)
    - Deletes module directories and property files

.PARAMETER RemovePersistence
    Remove the persistence module (switch)

.PARAMETER RemoveClient
    Remove the client module (switch)

.PARAMETER ProjectName
    Name of the root directory (default: "template-project")

.PARAMETER BackupFirst
    Create backup before deleting (default: $true)

.PARAMETER DryRun
    Preview changes without executing them (switch)

.PARAMETER Verbose
    Show detailed output of all changes (switch)

.EXAMPLE
    .\remove-modules.ps1 -RemovePersistence

.EXAMPLE
    .\remove-modules.ps1 -RemoveClient

.EXAMPLE
    .\remove-modules.ps1 -RemovePersistence -RemoveClient

.EXAMPLE
    .\remove-modules.ps1 -RemovePersistence -DryRun

.EXAMPLE
    .\remove-modules.ps1 -RemoveClient -Verbose
#>

param(
    [Parameter(Mandatory = $false)]
    [switch]$RemovePersistence,

    [Parameter(Mandatory = $false)]
    [switch]$RemoveClient,

    [Parameter(Mandatory = $false)]
    [string]$ProjectName = "template-project",

    [Parameter(Mandatory = $false)]
    [bool]$BackupFirst = $true,

    [Parameter(Mandatory = $false)]
    [switch]$DryRun,

    [Parameter(Mandatory = $false)]
    [switch]$Verbose
)

# Set up error handling
$ErrorActionPreference = "Stop"
$VerbosePreference = if ($Verbose)
{
    "Continue"
}
else
{
    "SilentlyContinue"
}

# Colors for output
$ErrorColor = "Red"
$SuccessColor = "Green"
$WarningColor = "Yellow"
$InfoColor = "Cyan"

function Write-Info
{
    param([string]$Message)
    Write-Host $Message -ForegroundColor $InfoColor
}

function Write-Success
{
    param([string]$Message)
    Write-Host $Message -ForegroundColor $SuccessColor
}

function Write-Error-Custom
{
    param([string]$Message)
    Write-Host $Message -ForegroundColor $ErrorColor
}

function Write-Warning-Custom
{
    param([string]$Message)
    Write-Host $Message -ForegroundColor $WarningColor
}

# Validate input
function Validate-Input
{
    Write-Info "Validating input parameters..."

    if (-not $RemovePersistence -and -not $RemoveClient)
    {
        Write-Error-Custom "Error: At least one of -RemovePersistence or -RemoveClient must be specified"
        exit 1
    }

    Write-Success "✓ Input validation passed"
}

# Get the script's directory
$ScriptDir = Split-Path -Parent -Path $MyInvocation.MyCommand.Definition

# Navigate to project root
$ProjectRoot = Get-Location
if (-not (Test-Path "$ProjectRoot/pom.xml"))
{
    Write-Error-Custom "Error: pom.xml not found in current directory. Please run from project root."
    exit 1
}

Write-Info "Project root: $ProjectRoot"

# Display what will be removed
Write-Host "`n" -NoNewline
Write-Host "===================================================" -ForegroundColor White
Write-Host "Spring Boot Template - Module Removal Tool" -ForegroundColor White
Write-Host "===================================================" -ForegroundColor White

if ($RemovePersistence)
{
    Write-Host "Remove Persistence Module: YES" -ForegroundColor Yellow
}

if ($RemoveClient)
{
    Write-Host "Remove Client Module: YES" -ForegroundColor Yellow
}

if ($DryRun)
{
    Write-Host "Mode: DRY RUN (no changes will be made)" -ForegroundColor Yellow
}

Write-Host "===================================================" -ForegroundColor White
Write-Host ""

Validate-Input

# Create backups if requested
if ($BackupFirst -and -not $DryRun)
{
    Write-Info "Creating backups..."

    $BackupDir = "$ProjectRoot/.backup-$( Get-Date -Format 'yyyyMMdd-HHmmss' )"
    New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null

    # Backup pom.xml files
    Copy-Item -Path "$ProjectRoot/pom.xml" -Destination "$BackupDir/pom.xml" -Force
    if (Test-Path "$ProjectRoot/$ProjectName-core/pom.xml")
    {
        Copy-Item -Path "$ProjectRoot/$ProjectName-core/pom.xml" -Destination "$BackupDir/core-pom.xml" -Force
    }
    if (Test-Path "$ProjectRoot/$ProjectName-web/pom.xml")
    {
        Copy-Item -Path "$ProjectRoot/$ProjectName-web/pom.xml" -Destination "$BackupDir/web-pom.xml" -Force
    }

    Write-Success "✓ Backups created in: $BackupDir"
}

$DeletedCount = 0
$UpdatedCount = 0

# Helper function to update POM files
function Update-PomRemoveDependency
{
    param(
        [string]$FilePath,
        [string]$ArtifactId
    )

    if (-not (Test-Path $FilePath))
    {
        return $false
    }

    try
    {
        $Content = Get-Content -Path $FilePath -Raw

        # Pattern to match dependency block
        $Pattern = "(\s*)<dependency>\s*<groupId>com\.example</groupId>\s*<artifactId>$ProjectName-$ArtifactId</artifactId>\s*<version>\`$\{project\.parent\.version\}</version>\s*</dependency>"

        if ($Content -match $Pattern)
        {
            $NewContent = $Content -replace $Pattern, ""

            if (-not $DryRun)
            {
                Set-Content -Path $FilePath -Value $NewContent -Encoding UTF8 -NoNewline
            }

            Write-Verbose "Removed dependency from: $FilePath"
            return $true
        }
    }
    catch
    {
        Write-Verbose "Warning: Could not process $FilePath - $_"
    }

    return $false
}

# Helper function to update module declarations
function Update-PomRemoveModule
{
    param(
        [string]$FilePath,
        [string]$ModuleName
    )

    if (-not (Test-Path $FilePath))
    {
        return $false
    }

    try
    {
        $Content = Get-Content -Path $FilePath -Raw

        # Pattern to match module declaration (with optional whitespace)
        $Pattern = "\s*<module>$ModuleName</module>\s*\n?"

        if ($Content -match $Pattern)
        {
            $NewContent = $Content -replace $Pattern, "`n"

            if (-not $DryRun)
            {
                Set-Content -Path $FilePath -Value $NewContent -Encoding UTF8 -NoNewline
            }

            Write-Verbose "Removed module from: $FilePath"
            return $true
        }
    }
    catch
    {
        Write-Verbose "Warning: Could not process $FilePath - $_"
    }

    return $false
}

# Helper function to remove lines from TemplateApplication.kt
function Update-TemplateApplication
{
    param(
        [string]$FilePath,
        [string]$LinePattern
    )

    if (-not (Test-Path $FilePath))
    {
        return $false
    }

    try
    {
        $Content = Get-Content -Path $FilePath -Raw

        if ($Content -match $LinePattern)
        {
            # Comment out the line instead of removing it
            $NewContent = $Content -replace "(\s*)$LinePattern", "`$1// `$0"

            if (-not $DryRun)
            {
                Set-Content -Path $FilePath -Value $NewContent -Encoding UTF8 -NoNewline
            }

            Write-Verbose "Updated: $FilePath"
            return $true
        }
    }
    catch
    {
        Write-Verbose "Warning: Could not process $FilePath - $_"
    }

    return $false
}

# ==================== PERSISTENCE MODULE ====================

if ($RemovePersistence)
{
    Write-Info "`nPhase 1: Removing persistence module..."

    # 1. Remove from root pom.xml modules
    Write-Info "  Updating root pom.xml..."
    if (Update-PomRemoveModule -FilePath "$ProjectRoot/pom.xml" -ModuleName "$ProjectName-persistence")
    {
        $UpdatedCount++
    }

    # 2. Remove dependency from core pom.xml
    Write-Info "  Updating core module pom.xml..."
    if (Update-PomRemoveDependency -FilePath "$ProjectRoot/$ProjectName-core/pom.xml" -ArtifactId "persistence")
    {
        $UpdatedCount++
    }

    # 3. Update TemplateApplication.kt
    Write-Info "  Updating TemplateApplication.kt..."
    $AppPath = Get-ChildItem -Path "$ProjectRoot/**/TemplateApplication.kt" -Recurse | Select-Object -First 1
    if ($AppPath)
    {
        if (Update-TemplateApplication -FilePath $AppPath.FullName -LinePattern '"classpath:persistence-.*properties"')
        {
            $UpdatedCount++
        }
    }

    # 4. Remove JaCoCo exclusions for persistence
    Write-Info "  Updating JaCoCo configuration..."
    $RootPom = "$ProjectRoot/pom.xml"
    if (Test-Path $RootPom)
    {
        $Content = Get-Content -Path $RootPom -Raw
        $NewContent = $Content -replace '(\s*)<exclude>com/example/templateproject/persistence/.*</exclude>\s*\n?', ""

        if ($NewContent -ne $Content -and -not $DryRun)
        {
            Set-Content -Path $RootPom -Value $NewContent -Encoding UTF8 -NoNewline
        }

        if ($NewContent -ne $Content)
        {
            $UpdatedCount++
        }
    }

    # 5. Delete persistence directory
    Write-Info "  Deleting persistence module directory..."
    $PersistenceDir = "$ProjectRoot/$ProjectName-persistence"
    if (Test-Path $PersistenceDir)
    {
        if (-not $DryRun)
        {
            Remove-Item -Path $PersistenceDir -Recurse -Force
        }

        Write-Verbose "Deleted: $PersistenceDir"
        $DeletedCount++
    }

    # 6. Delete persistence property files
    Write-Info "  Removing persistence property files..."
    Get-ChildItem -Path "$ProjectRoot/*/src/main/resources" -Filter "persistence-*.properties" -ErrorAction SilentlyContinue | ForEach-Object {
        if (-not $DryRun)
        {
            Remove-Item -Path $_.FullName -Force
        }

        $RelPath = $_.FullName -replace [regex]::Escape($ProjectRoot + '\'), ''
        Write-Verbose "Deleted: $RelPath"
        $DeletedCount++
    }

    # 7. Delete liquibase directory if exists
    Write-Info "  Removing liquibase migration files..."
    Get-ChildItem -Path "$ProjectRoot/*/src/main/resources/liquibase" -ErrorAction SilentlyContinue | ForEach-Object {
        if (-not $DryRun)
        {
            Remove-Item -Path $_.FullName -Recurse -Force
        }

        $RelPath = $_.FullName -replace [regex]::Escape($ProjectRoot + '\'), ''
        Write-Verbose "Deleted: $RelPath"
        $DeletedCount++
    }

    Write-Success "✓ Persistence module removal completed"
}

# ==================== CLIENT MODULE ====================

if ($RemoveClient)
{
    Write-Info "`nPhase 2: Removing client module..."

    # 1. Remove from root pom.xml modules
    Write-Info "  Updating root pom.xml..."
    if (Update-PomRemoveModule -FilePath "$ProjectRoot/pom.xml" -ModuleName "$ProjectName-client")
    {
        $UpdatedCount++
    }

    # 2. Remove dependency from core pom.xml
    Write-Info "  Updating core module pom.xml..."
    if (Update-PomRemoveDependency -FilePath "$ProjectRoot/$ProjectName-core/pom.xml" -ArtifactId "client")
    {
        $UpdatedCount++
    }

    # 3. Update TemplateApplication.kt
    Write-Info "  Updating TemplateApplication.kt..."
    $AppPath = Get-ChildItem -Path "$ProjectRoot/**/TemplateApplication.kt" -Recurse | Select-Object -First 1
    if ($AppPath)
    {
        if (Update-TemplateApplication -FilePath $AppPath.FullName -LinePattern '"classpath:client-.*properties"')
        {
            $UpdatedCount++
        }
    }

    # 4. Remove JaCoCo exclusions for client
    Write-Info "  Updating JaCoCo configuration..."
    $RootPom = "$ProjectRoot/pom.xml"
    if (Test-Path $RootPom)
    {
        $Content = Get-Content -Path $RootPom -Raw
        $NewContent = $Content -replace '(\s*)<exclude>com/example/templateproject/client/.*</exclude>\s*\n?', ""

        if ($NewContent -ne $Content -and -not $DryRun)
        {
            Set-Content -Path $RootPom -Value $NewContent -Encoding UTF8 -NoNewline
        }

        if ($NewContent -ne $Content)
        {
            $UpdatedCount++
        }
    }

    # 5. Delete client directory
    Write-Info "  Deleting client module directory..."
    $ClientDir = "$ProjectRoot/$ProjectName-client"
    if (Test-Path $ClientDir)
    {
        if (-not $DryRun)
        {
            Remove-Item -Path $ClientDir -Recurse -Force
        }

        Write-Verbose "Deleted: $ClientDir"
        $DeletedCount++
    }

    # 6. Delete client property files
    Write-Info "  Removing client property files..."
    Get-ChildItem -Path "$ProjectRoot/*/src/main/resources" -Filter "client-*.properties" -ErrorAction SilentlyContinue | ForEach-Object {
        if (-not $DryRun)
        {
            Remove-Item -Path $_.FullName -Force
        }

        $RelPath = $_.FullName -replace [regex]::Escape($ProjectRoot + '\'), ''
        Write-Verbose "Deleted: $RelPath"
        $DeletedCount++
    }

    # 7. Update RetryConfiguration.kt if exists (comment it out or delete)
    Write-Info "  Updating RetryConfiguration.kt..."
    $RetryConfigPath = Get-ChildItem -Path "$ProjectRoot/**/RetryConfiguration.kt" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($RetryConfigPath)
    {
        $Content = Get-Content -Path $RetryConfigPath.FullName -Raw
        if (-not $DryRun)
        {
            Remove-Item -Path $RetryConfigPath.FullName -Force
        }

        Write-Verbose "Deleted: $RetryConfigPath.Name"
        $DeletedCount++
    }

    Write-Success "✓ Client module removal completed"
}

# ==================== VALIDATION ====================

Write-Info "`nPhase 3: Validating changes..."

# Check if pom.xml is valid XML
try
{
    [xml](Get-Content -Path "$ProjectRoot/pom.xml")
    Write-Success "✓ Root pom.xml is valid XML"
}
catch
{
    Write-Error-Custom "✗ Root pom.xml contains syntax errors: $_"
}

# Final summary
Write-Host "`n" -NoNewline
Write-Host "===================================================" -ForegroundColor Green
if ($DryRun)
{
    Write-Host "DRY RUN - NO CHANGES APPLIED" -ForegroundColor Yellow
    Write-Host "===================================================" -ForegroundColor Green
    Write-Host "This operation would have made the following changes:" -ForegroundColor White
}
else
{
    Write-Host "MODULE REMOVAL COMPLETED SUCCESSFULLY" -ForegroundColor Green
    Write-Host "===================================================" -ForegroundColor Green
}

Write-Host "POM files updated: $UpdatedCount" -ForegroundColor White
Write-Host "Items deleted: $DeletedCount" -ForegroundColor White
Write-Host "===================================================" -ForegroundColor Green

Write-Host "`nNext steps:" -ForegroundColor Cyan
Write-Host "1. Review all changes (check pom.xml and TemplateApplication.kt)" -ForegroundColor White
Write-Host "2. Delete any remaining references in your code to removed modules" -ForegroundColor White
Write-Host "3. Delete property files for removed modules in resources/" -ForegroundColor White
Write-Host "4. Run: mvn clean install" -ForegroundColor White
Write-Host "5. Run: mvn spring-boot:run -pl $ProjectName-web" -ForegroundColor White
Write-Host "6. If errors occur, restore from backup: .backup-*/" -ForegroundColor White

if (-not $DryRun)
{
    Write-Host "`nTo verify module removal, you can run:" -ForegroundColor Cyan
    Write-Host "  mvn clean verify" -ForegroundColor Gray
    Write-Host "  # Check for any remaining references:" -ForegroundColor Gray

    if ($RemovePersistence)
    {
        Write-Host "  grep -r 'persistence' . --include='*.kt' --include='*.xml' --include='*.properties'" -ForegroundColor Gray
    }

    if ($RemoveClient)
    {
        Write-Host "  grep -r 'client' . --include='*.kt' --include='*.xml' --include='*.properties'" -ForegroundColor Gray
    }
}

Write-Host ""

