import { Component, ChangeDetectionStrategy } from "@angular/core";
import { PageLayoutComponent } from "../../components/Layout/PageLayout";

@Component({
  selector: "app-privacy",
  templateUrl: "./privacy.component.html",
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [PageLayoutComponent],
})
export class PrivacyComponent {}
