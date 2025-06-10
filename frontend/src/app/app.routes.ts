import { Routes } from '@angular/router';
import { StylesComponent } from './pages/styles/styles.component';
import { ListDragAndDropComponent } from './pages/menu/components/list-drag-and-drop/list-drag-and-drop.component';
import { CreateRecipeComponent } from './pages/recipe/recipe-create/recipe-create.component';
import { MenuDetailViewComponent } from './pages/menu/menu-detail-view/menu-detail-view.component';
import { MenuOverviewComponent } from './pages/menu/menu-overview/menu-overview.component';
import { LoginComponent } from './pages/auth/login/login-page';
import { AuthGuard } from './security/auth-guard';
import { AccountRegistration } from './pages/auth/registration/registration';
import { OrganizationOverview } from './pages/organization/organization-overview/organization-overview.component';
import { OrganizationDetailview } from './pages/organization/organization-detail/organization-detail.component';
import { DetailShoppingListComponent } from './pages/shopping-lists/detail-shopping-list/detail-shopping-list.component';
import { AccountOverview } from './pages/account/overview/account-overview';
import { AccountEditComponent } from './pages/account/edit/account-edit.component';
import { ShoppingListsOverviewComponent } from './pages/shopping-lists/shopping-lists-overview/shopping-lists-overview.component';
import { HeroComponent } from './pages/hero/hero.component';
import { DetailMealComponent } from './pages/menu/detail-meal/detail-meal.component';
import { RecipeDetailComponent } from './pages/recipe/recipe-detail/recipe-detail.component';
import { RecipesOverviewComponent } from './pages/recipe/recipes-overview/recipes-overview.component';
import { StashComponent } from './pages/stash/stash.component';
import { PrivacyComponent } from './pages/privacy/privacy.component';
import { IngredientsManagementComponent } from './pages/ingredient/ingredients-management/ingredients-management.component';
import { ChangePasswordComponent } from './pages/account/change-password/change-password.component';
import { RecipeEditPageComponent } from './pages/recipe/recipe-edit-page/recipe-edit-page.component';
import { EditMealComponent } from './pages/menu/edit-meal/edit-meal.component';
import { MenuDisplayViewComponent } from './pages/menu/menu-display-view/menu-display-view.component';

export const routes: Routes = [
  { path: '', component: HeroComponent },
  { path: 'privacy', component: PrivacyComponent },
  { path: 'styles', component: StylesComponent },
  { path: 'list-drag-and-drop', component: ListDragAndDropComponent },
  {
    path: 'recipes',
    children: [
      { path: '', component: RecipesOverviewComponent },
      { path: 'create', component: CreateRecipeComponent, canActivate: [AuthGuard] },
      { path: ':id/edit', component: RecipeEditPageComponent, canActivate: [AuthGuard] },
      { path: ':id', component: RecipeDetailComponent },
    ],
  },
  {
    path: 'menus',
    children: [
      { path: '', component: MenuOverviewComponent, canActivate: [AuthGuard] },
      { path: ':id', component: MenuDetailViewComponent, canActivate: [AuthGuard] },
      { path: ':id/closed', component: MenuDisplayViewComponent, canActivate: [AuthGuard] },
      { path: ':menuId/meal/:mealId', component: DetailMealComponent, canActivate: [AuthGuard] },
      { path: ':menuId/meal/:mealId/edit', component: EditMealComponent, canActivate: [AuthGuard] },
    ],
  },
  {
    path: 'stashes/:id',
    component: StashComponent,
    canActivate: [AuthGuard],
  },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: AccountRegistration },
  { path: 'organizations', component: OrganizationOverview, canActivate: [AuthGuard] },
  {
    path: 'organizations',
    children: [
      { path: '', component: OrganizationOverview, canActivate: [AuthGuard] },
      { path: ':id', component: OrganizationDetailview, canActivate: [AuthGuard] },
    ],
  },
  {
    path: 'shopping-lists',
    children: [
      { path: '', component: ShoppingListsOverviewComponent, canActivate: [AuthGuard] },
      { path: ':id', component: DetailShoppingListComponent },
    ],
  },
  { path: 'account', component: AccountOverview, canActivate: [AuthGuard] },
  { path: 'account/edit', component: AccountEditComponent, canActivate: [AuthGuard] },
  { path: 'account/change-password', component: ChangePasswordComponent, canActivate: [AuthGuard] },
  { path: 'ingredients', component: IngredientsManagementComponent },
];
