import { Component, ChangeDetectionStrategy } from "@angular/core";
import { SimpleButtonComponent } from "../../components/Button/SimpleButton";

@Component({
  selector: "app-hero",
  templateUrl: "./hero.component.html",
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [SimpleButtonComponent],
})
export class HeroComponent {}
