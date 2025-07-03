import {Component, OnInit} from '@angular/core';
import { SimpleButtonComponent } from '../../components/Button/SimpleButton';
import {AnalyticsService} from "../../service/analytics.service";
import {GoogleTagManagerService} from 'angular-google-tag-manager'

@Component({
    selector: 'app-hero',
    templateUrl: './hero.component.html',
    imports: [SimpleButtonComponent]
})
export class HeroComponent implements OnInit {
  constructor(
      private analyticsService: AnalyticsService,
      private gtmService: GoogleTagManagerService
  ) {
  }

  ngOnInit() {
    this.analyticsService.trackEvent('Hero Page Loaded', 'What does this do?', 'TEST');
    console.log("Test")
  }

}
