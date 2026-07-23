import {
  Component,
  EventEmitter,
  Output,
  ChangeDetectionStrategy,
  inject,
} from "@angular/core";

import { FormsModule } from "@angular/forms";
import {
  OrganizationCreateDto,
  OrganizationsApiService,
} from "../../../../../../generated";
import {
  InputFieldComponent,
  InputType,
} from "../../../../../components/Input/InputField";
import {
  ButtonVariant,
  SimpleButtonComponent,
} from "../../../../../components/Button/SimpleButton";
import { ErrorService } from "../../../../../globals/error.service";
import { ToastrService } from "ngx-toastr";
import { SimpleModalComponent } from "../../../../../components/Modal/SimpleModalComponent";
import { TokenService } from "../../../../../security/token.service";
import { MarkdownEditorComponent } from "../../../../../components/Markdown/MarkdownEditor/markdown-editor.component";

@Component({
  selector: "organization-create",
  imports: [
    FormsModule,
    SimpleButtonComponent,
    InputFieldComponent,
    SimpleModalComponent,
    MarkdownEditorComponent,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: "./organization-create.component.html",
})
export class CreateOrganizationModalContentComponent {
  private organizationsApiService = inject(OrganizationsApiService);
  private errorService = inject(ErrorService);
  private toastr = inject(ToastrService);
  private tokenService = inject(TokenService);

  @Output() organizationCreated = new EventEmitter<void>();
  InputType = InputType;
  ButtonVariant = ButtonVariant;

  organization: OrganizationCreateDto = {
    name: "",
    description: "",
  };

  isCreateOrganizationModalOpen: boolean = false;

  onSubmit(): void {
    this.organizationsApiService
      .createOrganization(this.organization)
      .subscribe({
        next: () => {
          this.toastr.success("Organization created.");
          this.tokenService.tryRefreshRoles();
        },
        error: (error) => {
          this.errorService.printErrorResponse(error);
        },
      });
  }

  resetInputs(): void {
    this.organization = {
      name: "",
      description: "",
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
    return this.organization.name?.trim() !== "";
  }
}
