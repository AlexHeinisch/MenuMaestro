import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrganizationCreateDto, OrganizationsApiService } from '../../../../../../generated';
import { InputFieldComponent, InputType } from '../../../../../components/Input/InputField';
import { ButtonVariant, SimpleButtonComponent } from '../../../../../components/Button/SimpleButton';
import { ErrorService } from '../../../../../globals/error.service';
import { ToastrService } from 'ngx-toastr';
import { SimpleModalComponent } from '../../../../../components/Modal/SimpleModalComponent';
import { TokenService } from '../../../../../security/token.service';

@Component({
  selector: 'organization-create',
  standalone: true,
  imports: [CommonModule, FormsModule, SimpleButtonComponent, InputFieldComponent, SimpleModalComponent],
  templateUrl: './organization-create.component.html',
})
export class CreateOrganizationModalContentComponent {
  @Output() organizationCreated = new EventEmitter<void>();
  InputType = InputType;
  ButtonVariant = ButtonVariant;

  organization: OrganizationCreateDto = {
    name: '',
    description: '',
  };

  isCreateOrganizationModalOpen: boolean = false;

  constructor(
    private organizationsApiService: OrganizationsApiService,
    private errorService: ErrorService,
    private toastr: ToastrService,
    private tokenService: TokenService
  ) {}

  onSubmit(): void {
    this.organizationsApiService.createOrganization(this.organization).subscribe({
      next: () => {
        this.toastr.success('Organization created.');
        this.tokenService.tryRefreshRoles();
      },
      error: (error) => {
        this.errorService.printErrorResponse(error);
      },
    });
  }

  resetInputs(): void {
    this.organization = {
      name: '',
      description: '',
    };
  }

  toSelectOption(o: { id: number; name: string }): [number, string] {
    return [o.id, o.name];
  }

  handleCreateOrganizationModalCancel(): void {
    this.resetInputs();
  }

  handleCreateOrganizationModalSubmit(): void {
    this.onSubmit();
    this.resetInputs();

    setTimeout(() => {
      this.organizationCreated.emit();
    }, 100);
  }

  formIsValid(): boolean {
    return this.organization.name?.trim() !== '';
  }
}
