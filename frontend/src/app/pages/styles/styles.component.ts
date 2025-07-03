import { Component, ViewChild } from '@angular/core';
import { ButtonVariant, SimpleButtonComponent } from '../../components/Button/SimpleButton';
import { SimpleCardComponent } from '../../components/Card/Card';
import { InfoMessageComponent, InfoMessageType } from '../../components/Card/InfoMessage';
import { SimpleModalComponent } from '../../components/Modal/SimpleModalComponent';
import { ComplexModalComponent } from '../../components/Modal/ComplexModalComponent';
import { InputType } from '../../components/Input/InputField';
import { InputFieldComponent } from '../../components/Input/InputField';
import { Router } from '@angular/router';
import { FormsModule, NgForm } from '@angular/forms';
import { SearchInputComponent } from '../../components/Input/SearchInput';
import { PageLayoutComponent } from '../../components/Layout/PageLayout';

@Component({
    selector: 'app-styles',
    imports: [
        SimpleButtonComponent,
        SimpleCardComponent,
        InfoMessageComponent,
        SimpleModalComponent,
        ComplexModalComponent,
        SearchInputComponent,
        InputFieldComponent,
        FormsModule,
        PageLayoutComponent,
    ],
    templateUrl: './styles.component.html'
})
export class StylesComponent {
  ButtonVariant = ButtonVariant;
  InfoMessageType = InfoMessageType;
  InputType = InputType;

  isModalOpen = false;
  modalTitle = '';
  modalBody = '';

  isComplexModalOpen = false;
  complexModalTitle = '';
  complexModalBody = '';

  measurementUnits = ['grams', 'kilograms', 'ounces', 'pounds', 'liters'];
  name: string = '';
  defaultName: string = 'Roxana';
  email: string = '';
  noLabel: string = '';
  password: string = '';
  quantity: number = 5;
  selectedUnit: string = this.measurementUnits[0];
  eventDate: string = '';
  checkboxVal: boolean = true;
  checkboxVal2: boolean = false;

  constructor(private router: Router) {}

  handleClose(): void {
    console.log('Message closed');
  }

  openModal(title: string, body: string): void {
    this.modalTitle = title;
    this.modalBody = body;
    this.isModalOpen = true;
  }

  openComplexModal(title: string, body: string): void {
    this.complexModalTitle = title;
    this.complexModalBody = body;
    this.isComplexModalOpen = true;
  }

  handleModalCancel(): void {
    console.log('Modal cancelled');
    // this.isModalOpen = false; -> already happens because of the setShow.emit(false) in the reusable modal component
    // we would typically reset input fields here if needed; can be omitted entirely if nothing needs to happen on cancel
  }

  handleModalSubmit(): void {
    console.log('Modal submitted!');
    // this.isModalOpen = false; -> already happens because of the setShow.emit(false) in the reusable modal component
    // we would typically call an api/navigate to a route/reset input fields here if needed; can be omitted entirely if nothing needs to happen on submit (e.g. legend)
    this.router.navigate(['/']);
  }

  handleComplexModalCancel(): void {
    console.log('Complex modal cancelled');
    // this.isComplexModalOpen = false; -> already happens because of the setShow.emit(false) in the reusable modal component
  }

  handleComplexModalSubmit(): void {
    console.log('Complex modal submitted!');
    // this.isComplexModalOpen = false; -> already happens because of the setShow.emit(false) in the reusable modal component
  }

  handleComplexModalThirdAction(): void {
    console.log('Third action triggered!');
  }

  onSubmit(form: NgForm): void {
    console.log('is form valid?', form.valid);
    if (form.valid) {
      console.log('Name:', this.name);
      console.log('Default Name:', this.defaultName);
      console.log('Email:', this.email);
      console.log('No Label:', this.noLabel);
      console.log('Password:', this.password);
      console.log('Quantity:', this.quantity);
      console.log('Selected Unit:', this.selectedUnit);
      console.log('Event Date:', this.eventDate);
      console.log('Checkbox Value:', this.checkboxVal);
      console.log('Checkbox Value2:', this.checkboxVal2);

      this.router.navigate(['/']);
    }
  }

  handleSearch(searchTerm: string): void {
    console.log('Searching for:', searchTerm);
  }

  onFruitSelected(selected: unknown) {
    console.log('Selected Fruit:', selected);
  }
}
