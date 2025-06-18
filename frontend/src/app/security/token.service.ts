import { Injectable, OnInit } from '@angular/core';
import { jwtDecode, JwtPayload } from 'jwt-decode';
import {AuthApiService, OrganizationRoleEnum} from "../../generated";

interface CustomJwtPayload extends JwtPayload {
  roles?: string[];
}

@Injectable({
  providedIn: 'root',
})
export class TokenService {
  constructor(private authAPi: AuthApiService) {}

  logout(): void {
    localStorage.removeItem('authToken');
    localStorage.removeItem('authRoles');
    localStorage.removeItem('addMealToMenuMenuMap');
    localStorage.removeItem('addMealToMenuSelectedMenuName');
    localStorage.removeItem('addMealToMenuSelectedOrganizationId');
    localStorage.removeItem('addMealToMenuSelectedOrganizationName');
  }

  isAuthenticated(): boolean {
    if (this.getToken() !== '' && this.tokenExpired(this.getToken())) {
      this.logout();
    }
    return localStorage.getItem('authToken') != null;
  }

  isAdmin(): boolean {
    const roles = this.getRoles();
    return roles.length > 0 ? roles.includes('ROLE_ADMIN') : false;
  }

  isAdminOrOwner(orgId: number): boolean {
    return (
      this.isAdmin() ||
      [
        OrganizationRoleEnum.Owner.toString().toUpperCase(),
        OrganizationRoleEnum.Admin.toString().toUpperCase(),
      ].includes(this.getPermissionForOrganization(orgId) ?? '')
    );
  }

  isAdminOrOwnerOrPlanner(orgId: number): boolean {
    return (
      this.isAdmin() ||
      [
        OrganizationRoleEnum.Owner.toString().toUpperCase(),
        OrganizationRoleEnum.Admin.toString().toUpperCase(),
        OrganizationRoleEnum.Planner.toString().toUpperCase(),
      ].includes(this.getPermissionForOrganization(orgId) ?? '')
    );
  }

  isAdminOrOwnerOrPlannerOrShopper(orgId: number): boolean {
    return (
      this.isAdmin() ||
      [
        OrganizationRoleEnum.Owner.toString().toUpperCase(),
        OrganizationRoleEnum.Admin.toString().toUpperCase(),
        OrganizationRoleEnum.Planner.toString().toUpperCase(),
        OrganizationRoleEnum.Shopper.toString().toUpperCase(),
      ].includes(this.getPermissionForOrganization(orgId) ?? '')
    );
  }

  getPermissionForOrganization(orgId: number): null | string {
    const roles = this.getRoles();
    const orgRoles = roles
      .filter((value) => value.startsWith(`ORG::${String(orgId)}::`))
      .map((value) => value.split('::')[2]);
    return orgRoles.length > 0 ? orgRoles[0] : null;
  }

  getRoles(): string[] {
    const roles = localStorage.getItem('authRoles');
    return roles ? JSON.parse(roles) : [];
  }

  getToken(): String {
    const token = localStorage.getItem('authToken');
    return token ? token : '';
  }

  getUsername(): string | null {
    const token = localStorage.getItem('authToken');

    if (!token) {
      return null; // Handle missing token gracefully
    }

    try {
      const decodedToken = jwtDecode<CustomJwtPayload>(token);
      const username = decodedToken?.sub;

      if (!username) {
        return null; // Handle missing "sub" field
      }

      return username;
    } catch (error) {
      return null; // Handle invalid token
    }
  }

  saveToken(token: string): void {
    try {
      const decodedToken = jwtDecode<CustomJwtPayload>(token);
      const roles = decodedToken ? decodedToken['roles'] || [] : [];
      localStorage.setItem('authRoles', JSON.stringify(roles));
      localStorage.setItem('authToken', token);
    } catch (error) {
      console.error('Could not decode token');
    }
  }

  private tokenExpired(token: String) {
    const expiry = JSON.parse(atob(token.split('.')[1])).exp;
    return Math.floor(new Date().getTime() / 1000) >= expiry;
  }

  tryRefreshRoles(): void {
    if (this.isAuthenticated()) {
      this.authAPi.refreshRoles('').subscribe({
        next: (data) => {
          this.saveToken(data.accessToken.token);
        },
        error: (err) => {
          console.log(err);
        },
      });
    }
  }
}
