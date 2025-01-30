import { Component, EventEmitter, Output } from '@angular/core';
import { ButtonVariant, SimpleButtonComponent } from '../Button/SimpleButton';
import { EventData } from '@angular/cdk/testing';
import { ImagesApiService } from '../../../generated/image-storage/api/images.service';
import { ImageUploadResponseDto } from '../../../generated/image-storage/model/image-upload-response-dto';
import { CommonModule } from '@angular/common';
import { ErrorService } from '../../globals/error.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-file-upload',
  standalone: true,
  imports: [SimpleButtonComponent, CommonModule],
  templateUrl: './file-upload.component.html',
})
export class FileUploadComponent {
  protected readonly ButtonVariant = ButtonVariant;

  constructor(
    private imagesApi: ImagesApiService,
    private errorService: ErrorService,
    private toastr: ToastrService
  ) {}

  @Output()
  fileUploaded = new EventEmitter<ImageUploadResponseDto>();
  @Output()
  fileRemoved = new EventEmitter<boolean>();

  file: File | null = null;

  isDragOver: boolean = false;

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = true;
  }

  onDragLeave(event: DragEvent): void {
    this.isDragOver = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.onFileSelected({ target: { files: files } });
    }
  }

  onFileSelected(event: any) {
    this.file = event.target.files[0];

    if (this.file !== null) {
      this.imagesApi.uploadImage(this.file, 'response').subscribe({
        next: (response) => {
          this.toastr.success('Image uploaded.');
          this.fileUploaded.emit(response.body!);
        },
        error: (err) => {
          this.file = null;
          if (err?.status === 413) {
            this.toastr.error('The image was rejected by the server as it is above the size limit.');
          } else {
            this.errorService.printErrorResponse(err);
          }
        },
      });
    }
  }

  onFileRemoved(): void {
    this.toastr.success('Image removed.');
    this.file = null;
    this.fileRemoved.emit(true);
    this.isDragOver = false;
  }

  formatBytes(bytes: number): string {
    if (bytes === 0) return '0 Bytes';

    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];

    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i];
  }
}
