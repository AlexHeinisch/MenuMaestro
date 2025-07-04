import { Component, ViewChild } from '@angular/core';
import { PageLayoutComponent } from '../../../components/Layout/PageLayout';
import { SimpleModalComponent } from '../../../components/Modal/SimpleModalComponent';
import { ButtonVariant, SimpleButtonComponent } from '../../../components/Button/SimpleButton';
import { SearchInputComponent } from '../../../components/Input/SearchInput';
import { SimpleCardComponent } from '../../../components/Card/Card';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { InputFieldComponent, InputType } from '../../../components/Input/InputField';
import { FormsModule } from '@angular/forms';
import { LoadingSpinnerComponent } from '../../../components/LoadingSpinner/LoadingSpinner';
import { CreateOrganizationModalContentComponent } from './components/organization-create/organization-create.component';
import { ToastrService } from 'ngx-toastr';
import { TokenService } from '../../../security/token.service';
import { ErrorService } from '../../../globals/error.service';
import { PaginationControlsComponent } from '../../../components/Pagination/PaginationControls';
import { CreateMenuModalContentComponent } from '../../menu/menu-overview/components/create-menu-modal-content/create-menu-modal-content.component';
import {OrganizationsApiService, OrganizationSummaryListPaginatedDto} from "../../../../generated";

@Component({
    selector: 'app-organization-overview',
    imports: [
        PageLayoutComponent,
        CreateMenuModalContentComponent,
        SimpleModalComponent,
        SimpleButtonComponent,
        SearchInputComponent,
        SimpleCardComponent,
        CommonModule,
        RouterModule,
        InputFieldComponent,
        FormsModule,
        LoadingSpinnerComponent,
        CreateOrganizationModalContentComponent,
        PaginationControlsComponent,
    ],
    templateUrl: './organization-overview.component.html'
})
export class OrganizationOverview {
  ButtonVariant = ButtonVariant;
  InputType = InputType;

  organizations: OrganizationSummaryListPaginatedDto | undefined;
  isLoadingOrganizations: boolean = true;

  invitations: OrganizationSummaryListPaginatedDto | undefined;
  isLoadingInvitations: boolean = true;

  currentPage = 1;
  pageSize = 5;
  currentPageInvitations = 1;

  constructor(
    private organizationApiService: OrganizationsApiService,
    private toastr: ToastrService,
    private tokenService: TokenService,
    private errorService: ErrorService
  ) {}

  ngOnInit(): void {
    this.fetchOrganizations();
    this.fetchInvitations();
  }

  fetchOrganizations(requestedPage: number = 1): void {
    this.currentPage = requestedPage;
    this.isLoadingOrganizations = true;
    this.organizationApiService.getOrganizations(this.currentPage - 1, this.pageSize).subscribe({
      next: (organizationPageable) => {
        this.organizations = organizationPageable;
        this.isLoadingOrganizations = false;
      },
      error: (err) => {
        this.isLoadingOrganizations = false;
        this.errorService.printErrorResponse(err);
      },
    });
  }

  fetchInvitations(requestedPage: number = 1): void {
    this.currentPageInvitations = requestedPage;
    this.isLoadingInvitations = true;
    this.organizationApiService.getInvitations(this.currentPageInvitations - 1, this.pageSize).subscribe({
      next: (organizationPageable) => {
        this.invitations = organizationPageable;
        this.isLoadingInvitations = false;
      },
      error: (err) => {
        this.errorService.printErrorResponse(err);
        this.isLoadingInvitations = false;
      },
    });
  }

  onPageChange(newPage: number): void {
    this.fetchOrganizations(newPage);
    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    });
  }

  onPageChangeInvitations(newPage: number): void {
    this.fetchInvitations(newPage);
    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    });
  }

  acceptInvitation(event: MouseEvent, organizationId: number): void {
    this.organizationApiService.respondToInvitation(organizationId, { accept: true }).subscribe({
      next: () => {
        this.toastr.success('Invitation accepted.');
        this.fetchInvitations();
        this.fetchOrganizations();
        this.tokenService.tryRefreshRoles();
      },
      error: (err) => {
        this.errorService.printErrorResponse(err);
      },
    });
  }

  declineInvitation(event: MouseEvent, organizationId: number): void {
    this.organizationApiService.respondToInvitation(organizationId, { accept: false }).subscribe({
      next: () => {
        this.toastr.success('Invitation declined.');
        this.fetchInvitations();
      },
      error: (err) => {
        this.errorService.printErrorResponse(err);
      },
    });
  }

  isPartOfOrganization(orgId: number): boolean {
    return this.tokenService.getPermissionForOrganization(orgId) != null;
  }
}
