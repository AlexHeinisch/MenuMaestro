import {
  Component,
  OnInit,
  ChangeDetectionStrategy,
  inject,
} from "@angular/core";
import { RouterOutlet } from "@angular/router";
import { FooterComponent } from "./components/Layout/Footer";
import { HeaderComponent } from "./components/Layout/Header";
import { TokenService } from "./security/token.service";
import { AnalyticsService } from "./service/analytics.service";

@Component({
  selector: "app-root",
  imports: [RouterOutlet, HeaderComponent, FooterComponent],
  templateUrl: "./app.component.html",
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: "./app.component.css",
})
export class AppComponent implements OnInit {
  private tokenService = inject(TokenService);
  private analyticsService = inject(AnalyticsService);

  ngOnInit(): void {
    this.tokenService.tryRefreshRoles();
    this.analyticsService.trackEvent("test", {
      info: "there is room for future events",
    });
  }
}
