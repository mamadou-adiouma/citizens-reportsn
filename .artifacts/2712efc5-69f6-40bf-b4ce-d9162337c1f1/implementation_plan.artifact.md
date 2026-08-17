# Fix Gradle Errors and Implement Administration Section

The project currently fails to sync due to an extension name conflict ("kotlin") likely caused by unsupported/unstable Gradle and Android Gradle Plugin (AGP) versions. We will downgrade to stable versions and then implement the requested Administration portal.

## User Review Required

> [!IMPORTANT]
> I am downgrading AGP to `8.3.1` and Gradle to `8.5` to ensure stability. Please confirm if you require specific newer versions.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///D:/programmation/S2/programmation_native/projet/gradle/libs.versions.toml)
- Downgrade `agp` to `8.3.1`.
- Add `navigation` version if missing (already present in dependencies but good to have in TOML).

#### [MODIFY] [gradle-wrapper.properties](file:///D:/programmation/S2/programmation_native/projet/gradle/wrapper/gradle-wrapper.properties)
- Downgrade `distributionUrl` to Gradle `8.5`.

### Administration UI

#### [NEW] Admin Package Structure
- `ui.admin`: Contains all admin-related Fragments and Activities.

#### [NEW] Admin Screens
- **AdminLoginActivity**: Login portal for administrators.
- **AdminDashboardFragment**: Dashboard with statistics overview.
- **AdminIncidentsFragment**: List of all reports with filters.
- **AdminIncidentDetailFragment**: Detailed view to update status and assign technicians.
- **AdminStatsFragment**: Detailed charts and reports.
- **AdminDepartmentsFragment**: Management of municipal departments.

#### [NEW] Layouts
- `activity_admin_login.xml`
- `fragment_admin_dashboard.xml`
- `fragment_admin_incidents.xml`
- `fragment_admin_incident_detail.xml`
- `fragment_admin_stats.xml`
- `fragment_admin_departments.xml`

## Verification Plan

### Automated Tests
- Gradle Sync must pass.
- Build must succeed.

### Manual Verification
- Launch the app and navigate to the Admin Login.
- Verify each screen matches the mockup design (colors, layouts, functionality).
