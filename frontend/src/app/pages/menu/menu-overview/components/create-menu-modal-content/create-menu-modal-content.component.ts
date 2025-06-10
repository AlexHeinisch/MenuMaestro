import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MenuCreateDto } from '../../../../../../generated/menus/model/models';
import { MenusApiService } from '../../../../../../generated/menus/api/menus.service';
import { Router } from '@angular/router';
import {
  OrganizationSummaryDto,
  OrganizationSummaryListPaginatedDto,
} from '../../../../../../generated/organizations/model/models';
import { ButtonVariant, SimpleButtonComponent } from '../../../../../components/Button/SimpleButton';
import { InputFieldComponent, InputType } from '../../../../../components/Input/InputField';
import { SearchInputComponent } from '../../../../../components/Input/SearchInput';
import { OrganizationsApiService } from '../../../../../../generated/organizations';
import { ToastrService } from 'ngx-toastr';
import { ErrorService } from '../../../../../globals/error.service';
import { SimpleModalComponent } from '../../../../../components/Modal/SimpleModalComponent';

@Component({
  selector: 'menu-create',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    SimpleButtonComponent,
    InputFieldComponent,
    SearchInputComponent,
    SimpleModalComponent,
  ],
  templateUrl: './create-menu-modal-content.component.html',
})
export class CreateMenuModalContentComponent implements OnChanges {
  InputType = InputType;
  ButtonVariant = ButtonVariant;

  menu: MenuCreateDto = {
    name: '',
    description: '',
    organizationId: -1,
    numberOfPeople: 1,
  };

  isModalOpen = false;
  modalTitle = 'Create New Menu';

  organizationOptions: OrganizationSummaryDto[] = [];
  organizationOptionsNames: string[] = [];
  selectedOrganizationName: string | null = null;

  @Input() organizations: OrganizationSummaryDto[] = [];

  constructor(
    private menuApiService: MenusApiService,
    private organizationsApiService: OrganizationsApiService,
    private toastr: ToastrService,
    private router: Router,
    private errorService: ErrorService
  ) {
    const navigation = this.router.getCurrentNavigation();
    const state = navigation?.extras.state as { openCreateModal?: boolean };

    if (state?.openCreateModal) {
      this.isModalOpen = true;
    }
  }

  ngOnInit(): void {
    this.searchOrganization('');
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['organizations'] && this.organizations.length > 0) {
      this.menu.organizationId = this.organizations[0].id;
    }
  }

  onSubmit(): void {
    this.menuApiService.createMenu(this.menu).subscribe({
      next: (response) => {
        this.router.navigate([`/menus/${response.id}`]);
        this.toastr.success('Menu created.');
      },
      error: (error) => {
        this.errorService.printErrorResponse(error);
      },
    });
  }

  resetInputs(): void {
    this.menu = {
      name: '',
      description: '',
      organizationId: this.organizations[0].id,
      numberOfPeople: 1,
    };
  }

  searchOrganization(searchTerm: string) {
    this.organizationsApiService.getOrganizations(0, 5, searchTerm).subscribe({
      next: (response: OrganizationSummaryListPaginatedDto) => {
        if (response.content) {
          this.organizationOptions = response.content;
          this.organizationOptionsNames = this.organizationOptions.map((org) => org.name!);
        } else {
          this.organizationOptions = [];
          this.organizationOptionsNames = [];
        }
      },
      error: (error: any) => {
        this.errorService.printErrorResponse(error);
      },
    });
  }

  onOrganizationSelected(selected: string) {
    const selectedOrganization = this.organizationOptions.find((org) => org.name === selected);

    if (selectedOrganization) {
      this.selectedOrganizationName = selectedOrganization.name;
      this.menu.organizationId = selectedOrganization.id;
    }
  }

  toSelectOption(o: { id: number; name: string }): [number, string] {
    return [o.id, o.name];
  }

  handleModalCancel(): void {
    this.resetInputs();
  }

  handleModalSubmit(): void {
    this.onSubmit();
    this.resetInputs();
  }

  formIsValid(): boolean {
    return this.menu.name?.trim() !== '' && this.menu.organizationId > 0 && this.menu.numberOfPeople > 0;
  }
}
