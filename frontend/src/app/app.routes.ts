import { Routes } from '@angular/router';
import { AuthGuard } from './security/auth-guard';

export const mainTitle: string = "MenuMaestro"

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/hero/hero.component').then(m => m.HeroComponent),
    title: mainTitle,
  },
  {
    path: 'privacy',
    loadComponent: () => import('./pages/privacy/privacy.component').then(m => m.PrivacyComponent),
    title: `${mainTitle} - Privacy`,
  },
  {
    path: 'list-drag-and-drop',
    loadComponent: () =>
      import('./pages/menu/components/list-drag-and-drop/list-drag-and-drop.component').then(m => m.ListDragAndDropComponent),
  },
  {
    path: 'recipes',
    children: [
      {
        path: '',
        loadComponent: () => import('./pages/recipe/recipes-overview/recipes-overview.component').then(m => m.RecipesOverviewComponent),
        title: `${mainTitle} - Recipes`,
      },
      {
        path: 'create',
        loadComponent: () => import('./pages/recipe/recipe-create/recipe-create.component').then(m => m.CreateRecipeComponent),
        canActivate: [AuthGuard],
        title: `${mainTitle} - Create Recipe`,
      },
      {
        path: ':id/edit',
        loadComponent: () => import('./pages/recipe/recipe-edit-page/recipe-edit-page.component').then(m => m.RecipeEditPageComponent),
        canActivate: [AuthGuard],
        title: `${mainTitle} - Edit Recipe`,
      },
      {
        path: ':id',
        loadComponent: () => import('./pages/recipe/recipe-detail/recipe-detail.component').then(m => m.RecipeDetailComponent),
        title: `${mainTitle} - Recipe Details`,
      },
    ],
  },
  {
    path: 'menus',
    children: [
      {
        path: '',
        loadComponent: () => import('./pages/menu/menu-overview/menu-overview.component').then(m => m.MenuOverviewComponent),
        canActivate: [AuthGuard],
        title: `${mainTitle} - Menus Details`,
      },
      {
        path: ':id',
        loadComponent: () => import('./pages/menu/menu-detail-view/menu-detail-view.component').then(m => m.MenuDetailViewComponent),
        canActivate: [AuthGuard],
        title: `${mainTitle} - Menu Details`,
      },
      {
        path: ':id/closed',
        loadComponent: () => import('./pages/menu/menu-display-view/menu-display-view.component').then(m => m.MenuDisplayViewComponent),
        canActivate: [AuthGuard],
        title: `${mainTitle} - Menu Details`,
      },
      {
        path: ':menuId/meal/:mealId',
        loadComponent: () => import('./pages/menu/detail-meal/detail-meal.component').then(m => m.DetailMealComponent),
        canActivate: [AuthGuard],
        title: `${mainTitle} - Meal Details`,
      },
      {
        path: ':menuId/meal/:mealId/edit',
        loadComponent: () => import('./pages/menu/edit-meal/edit-meal.component').then(m => m.EditMealComponent),
        canActivate: [AuthGuard],
        title: `${mainTitle} - Meal Details`,
      },
    ],
  },
  {
    path: 'stashes/:id',
    loadComponent: () => import('./pages/stash/stash.component').then(m => m.StashComponent),
    title: `${mainTitle} - Stashes`,
    canActivate: [AuthGuard],
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/auth/login/login-page').then(m => m.LoginComponent),
    title: `${mainTitle} - Login`,
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/auth/registration/registration').then(m => m.AccountRegistration),
    title: `${mainTitle} - Register`,
  },
  {
    path: 'accounts/verification',
    loadComponent: () => import('./pages/auth/verify-email/verify-email').then(m => m.VerifyEmailComponent),
    title: `${mainTitle} - Verify Email`,
  },
  {
    path: 'forgot-password',
    loadComponent: () => import('./pages/auth/forgot-password/forgot-password-page').then(m => m.ForgotPasswordComponent),
    title: `${mainTitle} - Forgot Password`,
  },
  {
    path: 'reset-password',
    loadComponent: () => import('./pages/auth/reset-password/reset-password-page').then(m => m.ResetPasswordComponent),
    title: `${mainTitle} - Reset Password`,
  },
  {
    path: 'organizations',
    children: [
      {
        path: '',
        loadComponent: () => import('./pages/organization/organization-overview/organization-overview.component').then(m => m.OrganizationOverview),
        canActivate: [AuthGuard],
        title: `${mainTitle} - Organizations`,
      },
      {
        path: ':id',
        loadComponent: () => import('./pages/organization/organization-detail/organization-detail.component').then(m => m.OrganizationDetailview),
        canActivate: [AuthGuard],
        title: `${mainTitle} - Organization Details`,
      },
    ],
  },
  {
    path: 'shopping-lists',
    children: [
      {
        path: '',
        loadComponent: () => import('./pages/shopping-lists/shopping-lists-overview/shopping-lists-overview.component').then(m => m.ShoppingListsOverviewComponent),
        canActivate: [AuthGuard],
        title: `${mainTitle} - Shopping Lists`,
      },
      {
        path: ':id',
        loadComponent: () => import('./pages/shopping-lists/detail-shopping-list/detail-shopping-list.component').then(m => m.DetailShoppingListComponent),
        title: `${mainTitle} - Shopping List Details`,
      },
    ],
  },
  {
    path: 'account',
    loadComponent: () => import('./pages/account/overview/account-overview').then(m => m.AccountOverview),
    canActivate: [AuthGuard],
    title: `${mainTitle} - Account`,
  },
  {
    path: 'account/edit',
    loadComponent: () => import('./pages/account/edit/account-edit.component').then(m => m.AccountEditComponent),
    canActivate: [AuthGuard],
    title: `${mainTitle} - Edit Account`,
  },
  {
    path: 'account/change-password',
    loadComponent: () => import('./pages/account/change-password/change-password.component').then(m => m.ChangePasswordComponent),
    canActivate: [AuthGuard],
    title: `${mainTitle} - Change Password`,
  },
  {
    path: 'ingredients',
    loadComponent: () => import('./pages/ingredient/ingredients-management/ingredients-management.component').then(m => m.IngredientsManagementComponent),
    title: `${mainTitle} - Admin Ingredients`,
  },
];
