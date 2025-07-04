import {CommonModule} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {FooterComponent} from './components/Layout/Footer';
import {HeaderComponent} from './components/Layout/Header';
import {TokenService} from './security/token.service';
import {AnalyticsService} from "./service/analytics.service";

@Component({
    selector: 'app-root',
    imports: [CommonModule, RouterOutlet, HeaderComponent, FooterComponent],
    templateUrl: './app.component.html',
    styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {

    constructor(
        private tokenService: TokenService,
        private analyticsService: AnalyticsService) {
    }

    ngOnInit(): void {
        this.tokenService.tryRefreshRoles();
        this.analyticsService.trackEvent("test", {info: "there is room for future events"})
    }
}
