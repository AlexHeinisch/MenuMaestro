import { Component } from '@angular/core';
import { PageLayoutComponent } from '../../../components/Layout/PageLayout';
import { SimpleModalComponent } from '../../../components/Modal/SimpleModalComponent';
import { SimpleButtonComponent } from '../../../components/Button/SimpleButton';
import { SearchInputComponent } from '../../../components/Input/SearchInput';
import { SimpleCardComponent } from '../../../components/Card/Card';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { InputFieldComponent, InputType } from '../../../components/Input/InputField';
import { FormsModule } from '@angular/forms';
import { LoadingSpinnerComponent } from '../../../components/LoadingSpinner/LoadingSpinner';
import {
  OrganizationEditDto,
  OrganizationRoleEnum,
  OrganizationsApiService,
  OrganizationSummaryDto
} from '../../../../generated';
import { ButtonVariant } from '../../../components/Button/SimpleButton';
import { OrganizationMembers } from '../organization-members/organization-members.component';
import { TokenService } from '../../../security/token.service';
import { ErrorService } from '../../../globals/error.service';
import { ToastrService } from 'ngx-toastr';
import { CreateMenuModalContentComponent } from '../../menu/menu-overview/components/create-menu-modal-content/create-menu-modal-content.component';

@Component({
    selector: 'app-organization-detail',
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
        SimpleCardComponent,
        OrganizationMembers,
    ],
    templateUrl: './organization-detail.component.html'
})
export class OrganizationDetailview {
  organization: OrganizationSummaryDto | undefined;
  isLoading: boolean = true;
  isEditModalOpen: boolean = false;
  orgEditDto: OrganizationEditDto = { name: '', description: '' };
  inputType = InputType;
  ButtonVariant = ButtonVariant;

  // Deleting Organizations
  isDeleteOrganizationModalOpen: boolean = false;
  organizationDeleteModalTitle: string = '';

  constructor(
    private route: ActivatedRoute,
    private organizationApiService: OrganizationsApiService,
    protected tokenService: TokenService,
    private toastr: ToastrService,
    private router: Router,
    private errorService: ErrorService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      const numericId = Number(id); // Convert the string id to a number
      if (!isNaN(numericId)) {
        this.fetchOrganizationById(numericId); // Pass the numeric id
      } else {
        console.error('Invalid organization ID:', id);
      }
    } else {
      console.error('Organization ID is missing from the route');
    }
  }

  isAdminOrOwner(): boolean {
    if (!this.organization) {
      return false;
    }
    return (
      this.tokenService.isAdmin() ||
      [OrganizationRoleEnum.Owner.toString().toUpperCase()].includes(
        this.tokenService.getPermissionForOrganization(this.organization.id) ?? ''
      )
    );
  }

  handleDeleteOrganizationModalSubmit(): void {
    this.deleteOrganization();
  }

  deleteOrganization(): void {
    this.organizationApiService.deleteOrganization(this.organization?.id!).subscribe({
      next: () => {
        this.toastr.success('Organization deleted.');
        setTimeout(() => {
          // Use setTimeout to allow modal closing animation to complete
          this.router.navigate(['/organizations']);
        }, 100);
      },
      error: (error) => {
        this.errorService.printErrorResponse(error);
      },
    });
  }

  // Open the delete confirmation modal and stop the routerLink event propagation
  openDeleteModal(): void {
    this.isDeleteOrganizationModalOpen = true;
    this.organizationDeleteModalTitle = 'Are you sure you want to delete "' + this.organization!.name + '"?';
  }

  fetchOrganizationById(id: number): void {
    this.organizationApiService.getOrganizationById(id).subscribe({
      next: (organization) => {
        this.organization = organization;
        this.orgEditDto.name = organization.name;
        this.orgEditDto.description = organization.description;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorService.printErrorResponse(err);
        this.isLoading = false;
      },
    });
  }

  onEdit() {
    if (this.organization !== null && this.orgEditDto !== null && this.organization?.id) {
      this.organizationApiService.editOrganization(this.organization.id, this.orgEditDto).subscribe({
        next: (response) => {
          this.organization = response;
          this.orgEditDto = { name: '', description: '' };
          this.isEditModalOpen = false;
          this.toastr.success('Organization updated.');
        },
        error: (err) => {
          this.errorService.printErrorResponse(err);
        },
      });
    }
  }
  openEditModal(): void {
    this.orgEditDto.name = this.organization?.name ?? '';
    this.orgEditDto.description = this.organization?.description ?? '';
    this.isEditModalOpen = true;
  }

  handleEditModalCancel(): void {
    this.isEditModalOpen = false;
    this.orgEditDto.name = this.organization?.name ?? '';
    this.orgEditDto.description = this.organization?.description ?? '';
  }

  handleEditModalSubmit(): void {
    this.onEdit();
  }

  isOrganizationOwner(): boolean {
    if (!this.organization) {
      return false;
    }
    return (
      this.tokenService.isAdmin() ||
      this.tokenService.getPermissionForOrganization(this.organization.id) ===
        OrganizationRoleEnum.Owner.toString().toUpperCase()
    );
  }
}
