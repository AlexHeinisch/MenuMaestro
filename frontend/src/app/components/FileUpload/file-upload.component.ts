import {
  Component,
  ChangeDetectionStrategy,
  inject,
  output,
} from "@angular/core";
import { ButtonVariant, SimpleButtonComponent } from "../Button/SimpleButton";
import { ErrorService } from "../../globals/error.service";
import { ToastrService } from "ngx-toastr";
import { ImagesApiService, ImageUploadResponseDto } from "../../../generated";

@Component({
  selector: "app-file-upload",
  imports: [SimpleButtonComponent],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: "./file-upload.component.html",
})
export class FileUploadComponent {
  private imagesApi = inject(ImagesApiService);
  private errorService = inject(ErrorService);
  private toastr = inject(ToastrService);

  protected readonly ButtonVariant = ButtonVariant;

  readonly fileUploaded = output<ImageUploadResponseDto>();
  readonly fileRemoved = output<boolean>();

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
    this.handleFiles(event.dataTransfer?.files ?? null);
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.handleFiles(input.files);
  }

  private handleFiles(files: FileList | null): void {
    this.file = files?.[0] ?? null;

    if (this.file !== null) {
      this.imagesApi.uploadImage(this.file, "response").subscribe({
        next: (response) => {
          this.toastr.success("Image uploaded.");
          this.fileUploaded.emit(response.body!);
        },
        error: (err) => {
          this.file = null;
          if (err?.status === 413) {
            this.toastr.error(
              "The image was rejected by the server as it is above the size limit.",
            );
          } else {
            this.errorService.printErrorResponse(err);
          }
        },
      });
    }
  }

  onFileRemoved(): void {
    this.toastr.success("Image removed.");
    this.file = null;
    this.fileRemoved.emit(true);
    this.isDragOver = false;
  }

  formatBytes(bytes: number): string {
    if (bytes === 0) return "0 Bytes";

    const k = 1024;
    const sizes = ["Bytes", "KB", "MB", "GB", "TB"];

    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return (bytes / Math.pow(k, i)).toFixed(2) + " " + sizes[i];
  }
}
