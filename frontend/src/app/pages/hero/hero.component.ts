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
      private gtmService: GoogleTagManagerService
  ) {
  }

  ngOnInit() {
    const gtmTag = {
        event: 'Loaded Event',
        pageName: 2
    }
    this.gtmService.pushTag(gtmTag)
    console.log("Test")
  }

}
