import { Routes } from '@angular/router';
import { ListDragAndDropComponent } from './pages/menu/components/list-drag-and-drop/list-drag-and-drop.component';
import { CreateRecipeComponent } from './pages/recipe/recipe-create/recipe-create.component';
import { MenuDetailViewComponent } from './pages/menu/menu-detail-view/menu-detail-view.component';
import { MenuOverviewComponent } from './pages/menu/menu-overview/menu-overview.component';
import { LoginComponent } from './pages/auth/login/login-page';
import { AuthGuard } from './security/auth-guard';
import { AccountRegistration } from './pages/auth/registration/registration';
import { VerifyEmailComponent } from './pages/auth/verify-email/verify-email';
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
import { ForgotPasswordComponent } from './pages/auth/forgot-password/forgot-password-page';
import { ResetPasswordComponent } from './pages/auth/reset-password/reset-password-page';

export const mainTitle: string = "MenuMaestro"

export const routes: Routes = [
  { path: '', component: HeroComponent, title: mainTitle},
  { path: 'privacy', component: PrivacyComponent, title: `${mainTitle} - Privacy`},
  { path: 'list-drag-and-drop', component: ListDragAndDropComponent },
  {
    path: 'recipes',
    children: [
      { path: '', component: RecipesOverviewComponent, title: `${mainTitle} - Recipes` },
      { path: 'create', component: CreateRecipeComponent, canActivate: [AuthGuard], title: `${mainTitle} - Create Recipe` },
      { path: ':id/edit', component: RecipeEditPageComponent, canActivate: [AuthGuard], title: `${mainTitle} - Edit Recipe` },
      { path: ':id', component: RecipeDetailComponent, title: `${mainTitle} - Recipe Details` },
    ],
  },
  {
    path: 'menus',
    children: [
      { path: '', component: MenuOverviewComponent, canActivate: [AuthGuard], title: `${mainTitle} - Menus Details` },
      { path: ':id', component: MenuDetailViewComponent, canActivate: [AuthGuard], title: `${mainTitle} - Menu Details` },
      { path: ':id/closed', component: MenuDisplayViewComponent, canActivate: [AuthGuard], title: `${mainTitle} - Menu Details` },
      { path: ':menuId/meal/:mealId', component: DetailMealComponent, canActivate: [AuthGuard], title: `${mainTitle} - Meal Details` },
      { path: ':menuId/meal/:mealId/edit', component: EditMealComponent, canActivate: [AuthGuard], title: `${mainTitle} - Meal Details` },
    ],
  },
  {
    path: 'stashes/:id',
    component: StashComponent, title: `${mainTitle} - Stashes`,
    canActivate: [AuthGuard],
  },
  { path: 'login', component: LoginComponent, title: `${mainTitle} - Login` },
  { path: 'register', component: AccountRegistration, title: `${mainTitle} - Register` },
  { path: 'accounts/verification', component: VerifyEmailComponent, title: `${mainTitle} - Verify Email` },
  { path: 'forgot-password', component: ForgotPasswordComponent, title: `${mainTitle} - Forgot Password` },
  { path: 'reset-password', component: ResetPasswordComponent, title: `${mainTitle} - Reset Password` },
  {
    path: 'organizations',
    children: [
      { path: '', component: OrganizationOverview, canActivate: [AuthGuard], title: `${mainTitle} - Organizations` },
      { path: ':id', component: OrganizationDetailview, canActivate: [AuthGuard], title: `${mainTitle} - Organization Details` },
    ],
  },
  {
    path: 'shopping-lists',
    children: [
      { path: '', component: ShoppingListsOverviewComponent, canActivate: [AuthGuard], title: `${mainTitle} - Shopping Lists` },
      { path: ':id', component: DetailShoppingListComponent, title: `${mainTitle} - Shopping List Details` },
    ],
  },
  { path: 'account', component: AccountOverview, canActivate: [AuthGuard], title: `${mainTitle} - Account` },
  { path: 'account/edit', component: AccountEditComponent, canActivate: [AuthGuard], title: `${mainTitle} - Edit Account` },
  { path: 'account/change-password', component: ChangePasswordComponent, canActivate: [AuthGuard], title: `${mainTitle} - Change Password` },

  { path: 'ingredients', component: IngredientsManagementComponent, title: `${mainTitle} - Admin Ingredients` },
];
