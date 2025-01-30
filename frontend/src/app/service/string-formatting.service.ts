import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class StringFormattingService {
  constructor() {}

  formatStringInput(stringInput: string | undefined, errorMessage: string | undefined = undefined): string {
    if (errorMessage) return errorMessage;
    if (!stringInput) return 'Unknown Status';

    return stringInput
      .toLowerCase()
      .replace(/_/g, ' ')
      .replace(/\band\b/g, '&')
      .replace(/\b\w/g, (char) => char.toUpperCase()); // capitalize the first letter of each word
  }
}
