import { Injectable } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { ErrorResponse } from '../../generated/menus';

@Injectable({
  providedIn: 'root',
})
export class ErrorService {
  constructor(private toastr: ToastrService) {}

  printErrorResponse(err: any) {
    console.error(err); // Always log the error for debugging

    if (err.error) {
      if (!err.status || err.status === 0) {
        // Handle unreachable server case
        this.toastr.error('The server is not reachable!', 'Error');
      } else if (err.error.message) {
        // Handle specific error messages
        const hasDetails = Array.isArray(err.error.details) && err.error.details.length > 0;
        const detailsText = hasDetails ? `\nDetails: ${err.error.details.join(', ')}!` : '';
        const errorMessage = `${err.error.message}${detailsText}`;
        this.toastr.error(errorMessage, 'Error');
      } else {
        // Handle unexpected error structure
        this.toastr.error('An unknown error occurred. Please try again.', 'Error');
      }
    } else {
      // Handle errors without the `error` object
      this.toastr.error('An unknown error occurred. Please try again.', 'Error');
    }
  }
}
