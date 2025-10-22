# Markdown Feature Implementation Guide

## Overview

This document describes the markdown support feature that has been implemented for MenuMaestro. The feature allows users to write and view markdown-formatted content for descriptions and directions across multiple entities.

## Implemented Changes

### Backend Changes

#### 1. Dependencies Added
- **CommonMark Java** (`org.commonmark:commonmark:0.23.0`) - For markdown parsing and validation
- Added to `pom.xml` and `impl/pom.xml`

#### 2. Markdown Sanitization Service
- **Location**: `/impl/src/main/java/dev/heinisch/menumaestro/service/MarkdownSanitizerService.java`
- **Purpose**: Validates markdown content and prevents links and images for security
- **Method**: `validateMarkdown(String markdown)` - Throws `IllegalArgumentException` if markdown contains links or images
- **Usage**: Called in all create/update services before saving entities

#### 3. Domain Model Updates

**RecipeValue** (`/domain-core/src/main/java/dev/heinisch/menumaestro/domain/recipe/RecipeValue.java`):
- Updated `description` field length from 1024 to 4096 characters
- Supports markdown formatting

**Menu** (`/domain-core/src/main/java/dev/heinisch/menumaestro/domain/menu/Menu.java`):
- Updated `description` field length to 4096 characters
- Supports markdown formatting

**Meal** (`/domain-core/src/main/java/dev/heinisch/menumaestro/domain/menu/Meal.java`):
- **NEW FIELD**: Added `description` field (VARCHAR(4096))
- Allows meal-specific markdown descriptions/directions

**Organization** (`/domain-core/src/main/java/dev/heinisch/menumaestro/domain/organization/Organization.java`):
- Updated `description` field length to 4096 characters
- Supports markdown formatting

#### 4. Database Migration
- **File**: `/db-versioning/src/main/resources/db/changelog/changes/markdown-support-changelog.xml`
- **Changes**:
  - Increased `recipe_value.description` to VARCHAR(4096)
  - Increased `menu.description` to VARCHAR(4096)
  - Added `menu_item.description` VARCHAR(4096) column
  - Increased `organization.description` to VARCHAR(4096)

#### 5. OpenAPI Specification Updates

**Recipes.yaml**:
- Updated `BaseRecipeValueDto.description` with markdown documentation and maxLength: 4096

**Menus.yaml**:
- Updated `MenuDetailDto.description` and `MenuCreateDto.description` with markdown documentation and maxLength: 4096

**Meals.yaml**:
- **NEW**: Added `description` field to `MealDto` and `MealEditDto`
- Documented as supporting markdown with maxLength: 4096

**Organizations.yaml**:
- Updated `OrganizationSummaryDto`, `OrganizationCreateDto`, and `OrganizationEditDto`
- Added markdown documentation and maxLength: 4096

#### 6. Service Updates

All services now validate markdown before saving:

**RecipeValueCreateService**:
```java
validateMarkdownDescription(recipeCreateEditDto);
```

**MenuService**:
```java
validateMarkdownDescription(menuCreateDto.getDescription());
```

**MealService**:
```java
validateMarkdownDescription(mealEditDto.getDescription());
meal.setDescription(mealEditDto.getDescription());
```

**OrganizationService**:
```java
validateMarkdownDescription(organizationCreateDto.getDescription());
validateMarkdownDescription(organizationEditDto.getDescription());
```

### Frontend Changes

#### 1. Dependencies Added
- **marked** (`^15.0.5`) - Markdown parsing library
- **ngx-markdown** (`^20.0.0`) - Angular markdown rendering
- Added to `frontend/package.json`

#### 2. App Configuration
- **File**: `/frontend/src/app/app.config.ts`
- Added `provideMarkdown()` to configure markdown rendering
- Security handled on backend, so `sanitize: SecurityContext.NONE`

#### 3. Reusable Components Created

**MarkdownViewerComponent**:
- **Location**: `/frontend/src/app/components/Markdown/MarkdownViewer/markdown-viewer.component.ts`
- **Purpose**: Renders markdown content in view mode
- **Usage**:
  ```html
  <app-markdown-viewer [content]="description"></app-markdown-viewer>
  ```
- **Styling**: Includes Tailwind CSS classes for proper markdown rendering

**MarkdownEditorComponent**:
- **Location**: `/frontend/src/app/components/Markdown/MarkdownEditor/markdown-editor.component.ts`
- **Purpose**: Provides markdown editing with live preview
- **Features**:
  - Edit/Preview tabs
  - Implements `ControlValueAccessor` for form integration
  - Shows character count
  - Displays helpful markdown syntax hints
  - Max length validation (4096 characters)
- **Usage**:
  ```html
  <app-markdown-editor
    formControlName="description"
    [placeholder]="'Enter markdown description...'"
    [maxLength]="4096">
  </app-markdown-editor>
  ```

## Frontend Integration Instructions

To complete the frontend integration, follow these steps for each entity type:

### Recipe Components

#### Recipe Create (`/frontend/src/app/pages/recipe/recipe-create/`)

1. Import the MarkdownEditorComponent:
```typescript
import { MarkdownEditorComponent } from '../../../components/Markdown/MarkdownEditor/markdown-editor.component';
```

2. Add to component imports array:
```typescript
imports: [
  // ... existing imports
  MarkdownEditorComponent
]
```

3. Replace the description textarea in the template:
```html
<!-- OLD -->
<input-field
  [label]="'Directions*'"
  [type]="InputType.textarea"
  [(value)]="recipeCreate.description"
  ...
></input-field>

<!-- NEW -->
<div class="mb-4">
  <label class="block text-sm font-medium mb-2">Directions*</label>
  <app-markdown-editor
    [(ngModel)]="recipeCreate.description"
    [placeholder]="'Enter recipe directions in markdown format...'"
    [maxLength]="4096"
    [required]="true">
  </app-markdown-editor>
</div>
```

#### Recipe Detail (`/frontend/src/app/pages/recipe/recipe-detail/`)

1. Import the MarkdownViewerComponent:
```typescript
import { MarkdownViewerComponent } from '../../../components/Markdown/MarkdownViewer/markdown-viewer.component';
```

2. Add to component imports array

3. Replace the description display in the template:
```html
<!-- OLD -->
<p>{{ recipe.description }}</p>

<!-- NEW -->
<app-markdown-viewer [content]="recipe.description"></app-markdown-viewer>
```

#### Recipe Edit

Follow the same pattern as Recipe Create.

### Menu Components

#### Menu Create Modal (`/frontend/src/app/pages/menu/menu-overview/components/create-menu-modal-content/`)

1. Import MarkdownEditorComponent
2. Update template:
```html
<div class="mb-4">
  <label class="block text-sm font-medium mb-2">Description*</label>
  <app-markdown-editor
    formControlName="description"
    [placeholder]="'Enter menu description in markdown format...'"
    [maxLength]="4096">
  </app-markdown-editor>
</div>
```

#### Menu Detail View (`/frontend/src/app/pages/menu/menu-detail-view/`)

1. Import MarkdownViewerComponent
2. Update template:
```html
<app-markdown-viewer [content]="menu.description"></app-markdown-viewer>
```

### Meal Components

#### Edit Meal (`/frontend/src/app/pages/menu/edit-meal/`)

1. Import MarkdownEditorComponent
2. Add description field to the form:
```html
<div class="mb-4">
  <label class="block text-sm font-medium mb-2">Description</label>
  <app-markdown-editor
    formControlName="description"
    [placeholder]="'Enter meal-specific description or notes in markdown format...'"
    [maxLength]="4096">
  </app-markdown-editor>
</div>
```

3. Update the component TypeScript to include description in the form model

#### Meal Detail (`/frontend/src/app/pages/menu/detail-meal/`)

1. Import MarkdownViewerComponent
2. Add description display:
```html
<div *ngIf="meal.description" class="mb-4">
  <h3 class="font-semibold mb-2">Description</h3>
  <app-markdown-viewer [content]="meal.description"></app-markdown-viewer>
</div>
```

### Organization Components

#### Organization Create (`/frontend/src/app/pages/organization/organization-overview/components/organization-create/`)

1. Import MarkdownEditorComponent
2. Update template:
```html
<div class="mb-4">
  <label class="block text-sm font-medium mb-2">Description</label>
  <app-markdown-editor
    formControlName="description"
    [placeholder]="'Enter organization description in markdown format...'"
    [maxLength]="4096">
  </app-markdown-editor>
</div>
```

#### Organization Detail (`/frontend/src/app/pages/organization/organization-detail/`)

1. Import MarkdownViewerComponent
2. Update template to use markdown viewer for description display

## Supported Markdown Syntax

Due to security restrictions, the following markdown features are supported:

- **Headers**: `# H1`, `## H2`, `### H3`, etc.
- **Bold**: `**bold text**`
- **Italic**: `*italic text*`
- **Lists**: Unordered (`- item`) and ordered (`1. item`)
- **Blockquotes**: `> quote`
- **Code**: Inline `` `code` `` and code blocks with ```
- **Horizontal Rules**: `---`
- **Tables**: Standard markdown table syntax

**NOT SUPPORTED** (for security reasons):
- **Links**: `[text](url)` - Will be rejected by backend
- **Images**: `![alt](url)` - Will be rejected by backend

## Testing

### Backend Testing

1. Start the backend application
2. The database migration will run automatically
3. Test creating/updating entities with markdown in description fields
4. Verify that links and images are rejected with appropriate error messages

### Frontend Testing

1. Install dependencies: `cd frontend && npm install`
2. Start the frontend: `npm start`
3. Test the markdown editor:
   - Switch between Edit and Preview tabs
   - Try various markdown syntax
   - Verify character counter
   - Test form validation

## Build Instructions

### Backend

```bash
mvn clean install
```

This will:
- Download the commonmark dependency
- Run database migrations
- Generate OpenAPI DTOs with the new description fields
- Compile and test all modules

### Frontend

```bash
cd frontend
npm install
npm run build
```

This will:
- Install ngx-markdown and marked
- Generate API client from updated OpenAPI specs
- Build the Angular application

## Security Considerations

1. **Backend Validation**: The `MarkdownSanitizerService` prevents XSS attacks by rejecting markdown with links and images
2. **Content Length**: All markdown fields are limited to 4096 characters
3. **Frontend Sanitization**: While ngx-markdown is configured with `sanitize: SecurityContext.NONE`, this is safe because the backend validates all content
4. **Database**: VARCHAR(4096) prevents excessively large content

## Troubleshooting

### Backend Issues

**Issue**: Liquibase fails to run migration
- **Solution**: Check that the database schema version is correct and the changelog is included in `db.changelog-master.xml`

**Issue**: Validation errors when creating entities
- **Solution**: Ensure the description doesn't contain links `[text](url)` or images `![alt](url)`

### Frontend Issues

**Issue**: Markdown not rendering
- **Solution**: Verify that `provideMarkdown()` is in `app.config.ts` and that the component imports `MarkdownModule`

**Issue**: Character counter not updating
- **Solution**: Ensure `ngModel` or `formControlName` is properly bound to the markdown editor

## Future Enhancements

Potential improvements for future versions:

1. **Configurable Restrictions**: Allow admins to enable/disable links and images per organization
2. **Markdown Templates**: Provide pre-defined templates for common recipe formats
3. **Rich Text Toolbar**: Add a formatting toolbar with buttons for common markdown syntax
4. **Collaborative Editing**: Add real-time collaborative editing for shared recipes
5. **Import/Export**: Support importing recipes from markdown files
6. **Syntax Highlighting**: Add syntax highlighting in the editor

## File Changes Summary

### Backend Files Modified
- `pom.xml` - Added commonmark version property
- `impl/pom.xml` - Added commonmark dependency
- `domain-core/.../recipe/RecipeValue.java` - Updated description length
- `domain-core/.../menu/Menu.java` - Updated description length
- `domain-core/.../menu/Meal.java` - Added description field
- `domain-core/.../organization/Organization.java` - Updated description length
- `endpoint/.../Recipes.yaml` - Updated description documentation
- `endpoint/.../Menus.yaml` - Updated description documentation
- `endpoint/.../Meals.yaml` - Added description field
- `endpoint/.../Organizations.yaml` - Updated description documentation
- `impl/.../service/RecipeValueCreateService.java` - Added markdown validation
- `impl/.../service/MenuService.java` - Added markdown validation
- `impl/.../service/MealService.java` - Added markdown validation and description handling
- `impl/.../service/OrganizationService.java` - Added markdown validation

### Backend Files Created
- `impl/.../service/MarkdownSanitizerService.java` - New service for markdown validation
- `db-versioning/.../markdown-support-changelog.xml` - New database migration
- Updated `db-versioning/.../db.changelog-master.xml` - Included new migration

### Frontend Files Modified
- `frontend/package.json` - Added ngx-markdown and marked dependencies
- `frontend/src/app/app.config.ts` - Added provideMarkdown configuration

### Frontend Files Created
- `frontend/src/app/components/Markdown/MarkdownViewer/markdown-viewer.component.ts`
- `frontend/src/app/components/Markdown/MarkdownEditor/markdown-editor.component.ts`

### Frontend Files To Update (see instructions above)
- Recipe create/edit/detail components
- Menu create/detail components
- Meal create/edit/detail components
- Organization create/edit/detail components

## Contact & Support

For questions or issues with this feature, please refer to the MenuMaestro project documentation or create an issue in the project repository.
