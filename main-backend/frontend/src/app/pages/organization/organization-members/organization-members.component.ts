import { Component, Input, OnInit } from '@angular/core';
import { SimpleModalComponent } from '../../../components/Modal/SimpleModalComponent';
import { SimpleButtonComponent } from '../../../components/Button/SimpleButton';
import { SearchInputComponent } from '../../../components/Input/SearchInput';
import { SimpleCardComponent } from '../../../components/Card/Card';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { InputFieldComponent, InputType } from '../../../components/Input/InputField';
import { FormsModule } from '@angular/forms';
import { LoadingSpinnerComponent } from '../../../components/LoadingSpinner/LoadingSpinner';
import {
  AccountsApiService,
  OrganizationMemberDto,
  OrganizationMemberListPaginatedDto,
  OrganizationRoleEnum, OrganizationsApiService,
  OrganizationSummaryDto,
} from '../../../../generated';
import { ButtonVariant } from '../../../components/Button/SimpleButton';
import { TokenService } from '../../../security/token.service';
import { ToastrService } from 'ngx-toastr';
import { ErrorService } from '../../../globals/error.service';
import { PaginationControlsComponent } from '../../../components/Pagination/PaginationControls';
import { CreateMenuModalContentComponent } from '../../menu/menu-overview/components/create-menu-modal-content/create-menu-modal-content.component';

@Component({
  selector: 'app-organization-members',
  standalone: true,
  imports: [
    SimpleButtonComponent,
    CreateMenuModalContentComponent,
    SimpleModalComponent,
    SearchInputComponent,
    SimpleCardComponent,
    CommonModule,
    RouterModule,
    InputFieldComponent,
    FormsModule,
    LoadingSpinnerComponent,
    SimpleCardComponent,
    PaginationControlsComponent,
  ],

  templateUrl: './organization-members.component.html',
})
export class OrganizationMembers implements OnInit {
  @Input() organization: OrganizationSummaryDto | undefined;
  organizationId!: number;

  isInviteMemberModalOpen: boolean = false;
  inviteMemberInput: string = '';
  memberOptions: Array<string> = [];
  ButtonVariant = ButtonVariant;
  memberList: OrganizationMemberListPaginatedDto | undefined;
  members: OrganizationMemberDto[] = [];
  unchangedMembers: OrganizationMemberDto[] = [];

  roleTypes: string[] = [];

  currentPage = 1;
  pageSize = 10;

  isChangeRoleModalOpen: boolean = false;
  changeRoleModalTitle: string = '';
  isRemoveMemberModalOpen: boolean = false;
  removeMemberModalTitle: string = '';
  usernameToRemove: string = '';
  memberToChange: OrganizationMemberDto | undefined;
  oldRole: OrganizationRoleEnum | undefined;

  isLoadingMembers: boolean = true;

  constructor(
    private organizationApiService: OrganizationsApiService,
    private accountsApiService: AccountsApiService,
    protected tokenService: TokenService,
    private toastr: ToastrService,
    private errorService: ErrorService,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.organizationId = Number(this.route.snapshot.paramMap.get('id'));
    this.fetchMembers();
    this.setRoleTypes();
  }

  onInviteMember() {
    if (this.organization && this.organization?.id) {
      this.organizationApiService.inviteMember(this.organization.id, { username: this.inviteMemberInput }).subscribe({
        next: (response) => {
          this.toastr.success('Invitation send to user "' + this.inviteMemberInput + '".');
          this.inviteMemberInput = '';
          this.isInviteMemberModalOpen = false;
          this.fetchMembers();
        },
        error: (err) => {
          this.errorService.printErrorResponse(err);
        },
      });
    }
  }

  fetchMembers(requestedPage: number = 1): void {
    this.currentPage = requestedPage;
    this.isLoadingMembers = true;
    this.organizationApiService
      .getOrganizationMembers(this.organizationId, this.currentPage - 1, this.pageSize)
      .subscribe({
        next: (response) => {
          this.memberList = response;
          this.members = response.content;
          this.unchangedMembers = structuredClone(response.content);
          this.isLoadingMembers = false;
        },
        error: (err) => {
          this.errorService.printErrorResponse(err);
          this.isLoadingMembers = false;
        },
      });
  }

  setRoleTypes(): void {
    let array = Object.values(OrganizationRoleEnum);
    array.splice(array.indexOf(OrganizationRoleEnum.Owner), 1);
    array.splice(array.indexOf(OrganizationRoleEnum.Invited), 1);
    this.roleTypes = array;
  }

  getUsername(): string {
    return this.tokenService.getUsername()!;
  }

  openChangeRoleModal(member: OrganizationMemberDto, role: OrganizationRoleEnum): void {
    if (member.username && role) {
      this.oldRole = this.unchangedMembers.find((item) => item.username === member.username)?.role;
      this.changeRoleModalTitle =
        'Are you sure you want to change the role of "' +
        member.username +
        '" to "' +
        role.charAt(0).toUpperCase() +
        role.slice(1) +
        '"?';
      this.memberToChange = member;
    }
    this.isChangeRoleModalOpen = true;
  }

  handleChangeRoleModalSubmit(): void {
    this.organizationApiService
      .changeMemberRole(this.organization?.id!, this.memberToChange?.username!, { role: this.memberToChange?.role! })
      .subscribe({
        next: (response) => {
          this.toastr.success('Successfully changed role of user "' + this.memberToChange?.username! + '".');
          this.changeRoleModalTitle = '';
          this.memberToChange = undefined;
          this.oldRole = undefined;
        },
        error: (err) => {
          this.memberToChange!.role = this.oldRole!;
          this.errorService.printErrorResponse(err);
          this.changeRoleModalTitle = '';
          this.memberToChange = undefined;
          this.oldRole = undefined;
        },
      });
  }

  handleChangeRoleModalCancel(): void {
    this.memberToChange!.role = this.oldRole!;
    this.changeRoleModalTitle = '';
    this.memberToChange = undefined;
    this.oldRole = undefined;
  }

  openRemoveMemberModal(username: string): void {
    if (username) {
      this.removeMemberModalTitle = 'Are you sure you want to remove "' + username + '" from this organization?';
      this.usernameToRemove = username;
    }
    this.isRemoveMemberModalOpen = true;
  }

  handleRemoveMemberModalSubmit(): void {
    this.organizationApiService.removeMember(this.organization?.id!, this.usernameToRemove).subscribe({
      next: (response) => {
        this.toastr.success('Successfully removed user "' + this.usernameToRemove + '" from organization.');
        this.usernameToRemove = '';
        this.fetchMembers();
      },
      error: (err) => {
        this.errorService.printErrorResponse(err);
        this.usernameToRemove = '';
      },
    });
  }

  handleRemoveMemberModalCancel(): void {
    this.usernameToRemove = '';
  }

  openInviteMemberModal(): void {
    this.isInviteMemberModalOpen = true;
  }

  handleInviteMemberModalCancel(): void {
    this.isInviteMemberModalOpen = false;
    this.inviteMemberInput = '';
  }

  handleInviteMemberModalSubmit(): void {
    this.onInviteMember();
  }

  searchUsers(searchTerm: string) {
    this.inviteMemberInput = searchTerm;
    if (searchTerm.trim() === '') {
      this.memberOptions = [];
      return;
    }

    this.accountsApiService.searchAccounts(0, 5, searchTerm, this.organization?.id).subscribe({
      next: (response) => {
        if (response) {
          this.memberOptions = response.content.map((dto) => dto.username);
        } else {
          this.memberOptions = [];
        }
      },
      error: (error) => {
        this.errorService.printErrorResponse(error);
      },
    });
  }
  onSearchUsersSelected(selected: string) {
    this.inviteMemberInput = selected;
    this.memberOptions = [];
  }

  onPageChange(newPage: number): void {
    this.fetchMembers(newPage);
    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    });
  }

  protected readonly InputType = InputType;
  protected readonly OrganizationRoleEnum = OrganizationRoleEnum;
}
